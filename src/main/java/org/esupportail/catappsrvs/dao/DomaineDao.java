package org.esupportail.catappsrvs.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import fj.*;
import fj.data.*;
import org.esupportail.catappsrvs.model.*;

import javax.persistence.EntityManager;

import static fj.Function.curry;
import static fj.P.p;
import static fj.data.$._;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.List.iterableList;
import static fj.data.Option.some;
import static fj.data.Stream.iterableStream;
import static org.esupportail.catappsrvs.model.CommonTypes.Code.*;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;
import static org.esupportail.catappsrvs.model.Versionned.Version;

public final class DomaineDao extends CrudDao<Domaine> implements IDomaineDao {
    private final P1<ICrudDao<Application>> appDao;

    private static final QDomaine dom = QDomaine.domaine;
    private static final QDomaine otherDom = new QDomaine("otherDom");
    private static final QApplication app = QApplication.application;
    private static final QApplication otherApp = new QApplication("otherApp");

    private DomaineDao(EntityManager entityManager,
                       P1<ICrudDao<Application>> appDao) {
        super(entityManager, dom, Domaine.class);
        this.appDao = appDao;
    }

    public static DomaineDao domaineDao(EntityManager entityManager,
                                        P1<ICrudDao<Application>> appDao) {
        return new DomaineDao(entityManager, appDao);
    }

    @Override
    Either<Exception, Domaine> prepare(final Domaine domaine) {
        final Either<Exception, Option<Domaine>> parent =
                domaine.parent().map(new F<Domaine, Either<Exception, Option<Domaine>>>() {
                    public Either<Exception, Option<Domaine>> f(Domaine dom) {
                        return read(dom.code(), Option.<Version>none())
                                .right()
                                .map(Option.<Domaine>some_());
                    }
                })
                .orSome(Either.<Exception, Option<Domaine>>right(Option.<Domaine>none()));

        final List<Either<Exception, Domaine>> sousDomaines =
                domaine.domaines().map(new F<Domaine, Either<Exception, Domaine>>() {
                    public Either<Exception, Domaine> f(Domaine dom) {
                        return read(dom.code(), Option.<Version>none())
                                .right()
                                .map(new F<Domaine, Domaine>() {
                                    public Domaine f(Domaine dm) {
                                        return dm.withParent(some(domaine));
                                    }
                                });
                    }
                });

        final List<Either<Exception, Application>> applications =
                domaine.applications().map(new F<Application, Either<Exception, Application>>() {
                    public Either<Exception, Application> f(Application application) {
                        return appDao._1().read(application.code(), Option.<Version>none());
                    }
                });

        return Either
                .sequenceRight(sousDomaines)
                .right()
                .apply(Either
                        .sequenceRight(applications)
                        .right()
                        .apply(parent.right().map(curry(new F3<Option<Domaine>, List<Application>, List<Domaine>, Domaine>() {
                            public Domaine f(Option<Domaine> parent, List<Application> applis, List<Domaine> ssdoms) {
                                return domaine
                                        .withParent(parent)
                                        .withApplications(applis)
                                        .withDomaines(ssdoms);
                            }
                        }))));
    }

    @Override
    Either<Exception, Domaine> persist(final Domaine domaine) {
        final List<Domaine> oldDoms = domaine.domaines();
        final List<Application> oldApps = domaine.applications();

        try {
            final Domaine realDomaine = domaine
                    .withDomaines(List.<Domaine>nil())
                    .withApplications(List.<Application>nil());

            entityManager.persist(realDomaine);

            final Either<Exception, List<Domaine>> newSousDoms =
                    Either.sequenceRight(oldDoms.map(new F<Domaine, Either<Exception, Domaine>>() {
                        public Either<Exception, Domaine> f(Domaine dom) {
                            return update(dom.withNullPk().withParent(some(realDomaine)));
                        }
                    }));

            final Either<Exception, List<Application>> newApps =
                    Either.sequenceRight(oldApps.map(new F<Application, Either<Exception, Application>>() {
                        public Either<Exception, Application> f(Application app) {
                            final List<Domaine> appOldDoms =
                                    app.domaines().removeAll(Equal.<Domaine>anyEqual().eq(domaine));
                            return appDao._1().update(app
                                    .withNullPk()
                                    .withDomaines(appOldDoms.snoc(realDomaine)));
                        }
                    }));

            return newSousDoms.right()
                    .apply(newApps.right()
                            .map(curry(new F2<List<Application>, List<Domaine>, Domaine>() {
                                public Domaine f(List<Application> apps, List<Domaine> doms) {
                                    return realDomaine
                                            .withDomaines(doms)
                                            .withApplications(apps);
                                }
                            })))
                    .right()
                    .bind(new F<Domaine, Either<Exception, Domaine>>() {
                        public Either<Exception, Domaine> f(Domaine d) {
                            try {
                                final Domaine newDom = entityManager.merge(d);
                                entityManager.flush();
                                return right(newDom);
                            } catch (Exception e) {
                                return left(e);
                            }
                        }
                    });
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    Either<Exception, Domaine> refine(Domaine domaine) {
        try {
            final java.util.List<Domaine> sousDoms =
                    lastDomsQuery(domaine).list(otherDom);

            final java.util.List<Application> applications =
                    lastAppsQuery(domaine).list(otherApp);

            return right(domaine
                    .withDomaines(iterableList(sousDoms))
                    .withApplications(iterableList(applications)));
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public Either<Exception, Tree<Domaine>> findDomaines(final List<LdapGroup> groups) {
        final F<Either<Exception, Domaine>, Tree<Either<Exception, Domaine>>> treeFunc =
                Tree.unfoldTree(new F<Either<Exception, Domaine>,
                        P2<Either<Exception, Domaine>, P1<Stream<Either<Exception, Domaine>>>>>() {
                    public P2<Either<Exception, Domaine>,
                            P1<Stream<Either<Exception, Domaine>>>> f(Either<Exception, Domaine> maybeNode) {
                        final Either<Exception, Domaine> newNode =
                                maybeNode.right().bind(new F<Domaine, Either<Exception, Domaine>>() {
                                    public Either<Exception, Domaine> f(Domaine dom) {
                                        try {
                                            final List<Application> apps =
                                                    iterableList(lastAppsQuery(dom)
                                                            .where(otherApp.groupe.in(groups.toCollection()))
                                                            .list(otherApp));
                                            return right(dom.withApplications(apps));
                                        } catch (Exception e) {
                                            return left(e);
                                        }
                                    }
                                });

                        final Stream<Either<Exception, Domaine>> children = newNode.either(
                                _(Stream.<Either<Exception, Domaine>>nil()).<Exception>constant(),
                                new F<Domaine, Stream<Either<Exception, Domaine>>>() {
                                    public Stream<Either<Exception, Domaine>> f(Domaine d) {
                                        return iterableStream(d.domaines()).map(Either.<Exception, Domaine>right_());
                                    }
                                });

                        return p(newNode, p(children));
                    }
                });

        final Either<Exception, Tree<Domaine>> appsFilteredTree =
                sequenceRightTree(treeFunc.f(read(code("ROOT"), Option.<Version>none())));

        return appsFilteredTree.right().map(new F<Tree<Domaine>, Tree<Domaine>>() {
            public Tree<Domaine> f(Tree<Domaine> tdoms) {
                return Tree.bottomUp(tdoms, new F<P2<Domaine, Stream<Domaine>>, Domaine>() {
                    public Domaine f(P2<Domaine, Stream<Domaine>> pair) {
                        return pair._1().withDomaines(pair._2().filter(new F<Domaine, Boolean>() {
                            public Boolean f(Domaine dom) {
                                return dom.applications().isNotEmpty();
                            }
                        }).toList());
                    }
                });
            }
        });
    }

    private JPAQuery lastAppsQuery(Domaine domaine) {
        return new JPAQuery(entityManager)
                .from(otherApp).where(otherApp.version.eq(new JPASubQuery()
                        .from(app).where(app.code.eq(otherApp.code)).unique(lastVersion(Application.class)))
                        .and(otherApp.domaines.contains(domaine)));
    }

    private JPAQuery lastDomsQuery(Domaine domaine) {
        return from(otherDom).where(otherDom.version.eq(new JPASubQuery()
                .from(dom).where(dom.code.eq(otherDom.code)).unique(lastVersion(Domaine.class)))
                .and(otherDom.parent.eq(domaine)));
    }

    private static <B, X> Either<X, Tree<B>> sequenceRightTree(final Tree<Either<X, B>> tree) {
        final Stream<Tree<Either<X, B>>> subForest = tree.subForest()._1();
        return subForest.isEmpty() ?
                tree.root().right().map(new F<B, Tree<B>>() {
                    public Tree<B> f(B b) {
                        return Tree.leaf(b);
                    }
                }) :
                subForest.head().root().right().bind(new F<B, Either<X, Tree<B>>>() {
                    public Either<X, Tree<B>> f(B b) {
                        return sequenceRightTree(Tree.node(Either.<X, B>right(b), subForest.tail()));
                    }
                });
    }
}
