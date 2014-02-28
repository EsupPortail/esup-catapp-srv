package org.esupportail.catappsrvs.dao;

import fj.*;
import fj.data.*;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.model.QDomain;
import org.esupportail.catappsrvs.model.utils.Shows;
import org.esupportail.catappsrvs.utils.logging.Log;

import javax.persistence.EntityManager;

import static fj.Function.curry;
import static fj.P.p;
import static fj.Unit.unit;
import static fj.data.$._;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.Option.some;
import static fj.data.Stream.iterableStream;
import static java.lang.String.format;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;
import static org.esupportail.catappsrvs.model.utils.Shows.shortDomainShow;
import static org.esupportail.catappsrvs.utils.logging.Log.Debug;

public final class DomainDao extends CrudDao<Domain> implements IDomainDao {
    private final P1<ICrudDao<Application>> appDao;

    private static final QDomain dom = QDomain.domain;

    private DomainDao(EntityManager entityManager,
                      P1<ICrudDao<Application>> appDao) {
        super(entityManager, dom, Domain.class);
        this.appDao = appDao;
    }

    public static DomainDao domaineDao(EntityManager entityManager,
                                        P1<ICrudDao<Application>> appDao) {
        return new DomainDao(entityManager, appDao);
    }

    @Override
    Either<Exception, Domain> prepare(final Domain domain) {
        final Either<Exception, Option<Domain>> parent =
                domain.parent().map(new F<Domain, Either<Exception, Option<Domain>>>() {
                    public Either<Exception, Option<Domain>> f(Domain dom) {
                        return read(dom.code())
                                .right()
                                .map(Option.<Domain>some_());
                    }
                })
                .orSome(Either.<Exception, Option<Domain>>right(Option.<Domain>none()));

        final List<Either<Exception, Domain>> subDomains =
                domain.domains().map(new F<Domain, Either<Exception, Domain>>() {
                    public Either<Exception, Domain> f(Domain dom) {
                        return read(dom.code());
                    }
                });

        final List<Either<Exception, Application>> applications =
                domain.applications().map(new F<Application, Either<Exception, Application>>() {
                    public Either<Exception, Application> f(Application application) {
                        return appDao._1().read(application.code());
                    }
                });

        return Either
                .sequenceRight(subDomains)
                .right()
                .apply(Either
                        .sequenceRight(applications)
                        .right()
                        .apply(parent.right().map(curry(new F3<Option<Domain>, List<Application>, List<Domain>, Domain>() {
                            public Domain f(Option<Domain> parent, List<Application> applis, List<Domain> ssdoms) {
                                return domain
                                        .withParent(parent)
                                        .withApplications(applis)
                                        .withDomains(ssdoms);
                            }
                        }))));
    }

    @Override
    Either<Exception, Domain> refine(Domain domain) {
        try {
            return right(domain
                    .withDomains(domain.domains())
                    .withApplications(domain.applications())); // on force le chargement par hibernate
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    public Either<Exception, Domain> create(Domain domain) {
        return prepare(domain)
                .right()
                .bind(new F<Domain, Either<Exception, Domain>>() {
                    public Either<Exception, Domain> f(Domain prepared) {
                        try {
                            final Domain persistedDom = prepared
                                    .withDomains(List.<Domain>nil())
                                    .withApplications(List.<Application>nil());
                            entityManager.persist(persistedDom);
                            final Either<Exception, List<Domain>> updatedDoms =
                                    Either.sequenceRight(prepared.domains().map(new F<Domain, Either<Exception, Domain>>() {
                                        public Either<Exception, Domain> f(Domain domain) {
                                            try {
                                                return right(entityManager.merge(domain.withParent(some(persistedDom))));
                                            } catch (Exception e) {
                                                return left(e);
                                            }
                                        }
                                    }));
                            final Either<Exception, List<Application>> updatedApps =
                                    Either.sequenceRight(prepared.applications().map(new F<Application, Either<Exception, Application>>() {
                                        public Either<Exception, Application> f(Application app) {
                                                final List<Domain> doms =
                                                        app.domains().removeAll(Equal.<Domain>anyEqual().eq(persistedDom));
                                                return appDao._1().update(app.withDomains(doms.cons(persistedDom)));
                                        }
                                    }));
                            return updatedApps.right()
                                    .apply(updatedDoms.right()
                                            .apply(Either.<Exception, Domain>right(persistedDom).right()
                                                    .map(curry(new F3<Domain, List<Domain>, List<Application>, Domain>() {
                                                        public Domain f(Domain dom, List<Domain> ds, List<Application> apps) {
                                                            return dom.withDomains(ds).withApplications(apps);
                                                        }
                                    }))));
                        } catch (Exception e) {
                            return left(e);
                        }
                    }
                });
    }

    @Override
    public Either<Exception, Domain> update(final Domain domain) {
        return read(domain.code()).right()
                .apply(prepare(domain).right()
                        .map(P.<Domain, Domain>p2())).right()
                .bind(new F<P2<Domain, Domain>, Either<Exception, Domain>>() {
                    public Either<Exception, Domain> f(P2<Domain, Domain> pair) {
                        try {
                            final Domain reloaded = pair._2();
                            final Domain prepared = pair._1();

                            reloaded.applications().foreach(new Effect<Application>() {
                                public void e(Application app) {
                                    final List<Domain> doms =
                                            app.domains().removeAll(Equal.<Domain>anyEqual().eq(reloaded));
                                    appDao._1().update(app.withDomains(doms));
                                }
                            });
                            reloaded.domains().foreach(new Effect<Domain>() {
                                public void e(Domain dom) {
                                    entityManager.merge(dom.withParent(reloaded.parent()));
                                }
                            });

                            final Domain merged = entityManager.merge(reloaded
                                    .withCode(prepared.code())
                                    .withCaption(prepared.caption())
                                    .withParent(prepared.parent())
                                    .withDomains(List.<Domain>nil())
                                    .withApplications(List.<Application>nil()));

                            final Either<Exception, List<Domain>> updatedDoms =
                                    Either.sequenceRight(prepared.domains().map(new F<Domain, Either<Exception, Domain>>() {
                                        public Either<Exception, Domain> f(Domain prepDom) {
                                            try {
                                                return right(entityManager.merge(prepDom.withParent(some(merged))));
                                            } catch (Exception e) {
                                                return left(e);
                                            }
                                        }
                                    }));
                            final Either<Exception, List<Application>> updatedApps =
                                    Either.sequenceRight(prepared.applications().map(new F<Application, Either<Exception, Application>>() {
                                        public Either<Exception, Application> f(Application app) {
                                            final List<Domain> doms =
                                                    app.domains().removeAll(Equal.<Domain>anyEqual().eq(merged));
                                            return appDao._1().update(app.withDomains(doms.cons(merged)));
                                        }
                                    }));

                            return Either.<Exception, Domain>right(merged).right()
                                    .apply(updatedDoms.right()
                                           .apply(updatedApps.right()
                                                   .map(curry(new F3<List<Application>, List<Domain>, Domain, Domain>() {
                                                       public Domain f(List<Application> as, List<Domain> ds, Domain d) {
                                                           return d.withDomains(ds).withApplications(as);
                                                       }
                                                   }))));
                        } catch (Exception e) {
                            return left(e);
                        }
                    }
                });
    }

    @Override
    public Either<Exception, Tree<Option<Domain>>> findDomaines(Code code, final List<LdapGroup> groups) {
        final F<Either<Exception, Domain>, Tree<Either<Exception, Domain>>> treeFunc =
                Tree.unfoldTree(new F<Either<Exception, Domain>,
                        P2<Either<Exception, Domain>, P1<Stream<Either<Exception, Domain>>>>>() {
                    public P2<Either<Exception, Domain>,
                            P1<Stream<Either<Exception, Domain>>>> f(Either<Exception, Domain> maybeNode) {
                        final Either<Exception, Domain> newNode =
                                maybeNode.right().bind(new F<Domain, Either<Exception, Domain>>() {
                                    public Either<Exception, Domain> f(Domain dom) {
                                        try {
                                            final List<Application> apps = dom.applications().filter(new F<Application, Boolean>() {
                                                public Boolean f(Application app) {
                                                    return groups.exists(Equal.<LdapGroup>anyEqual().eq(app.group()));
                                                }
                                            });
                                            final List<Domain> doms = dom.domains();
                                            return right(dom.withDomains(doms).withApplications(apps));
                                        } catch (Exception e) {
                                            return left(e);
                                        }
                                    }
                                });

                        final Stream<Either<Exception, Domain>> children = newNode.either(
                                _(Stream.<Either<Exception, Domain>>nil()).<Exception>constant(),
                                new F<Domain, Stream<Either<Exception, Domain>>>() {
                                    public Stream<Either<Exception, Domain>> f(Domain d) {
                                        return iterableStream(d.domains()).map(Either.<Exception, Domain>right_());
                                    }
                                });

                        return p(newNode, p(children));
                    }
                });

        final Either<Exception, Tree<Domain>> appsFilteredTree = sequenceRightTree(treeFunc.f(read(code)));

        final Either<Exception, Tree<Option<Domain>>> result = appsFilteredTree.right().map(new F<Tree<Domain>, Tree<Option<Domain>>>() {
            public Tree<Option<Domain>> f(Tree<Domain> tdoms) {
                return Tree.bottomUp(tdoms, new F<P2<Domain, Stream<Option<Domain>>>, Option<Domain>>() {
                    public Option<Domain> f(P2<Domain, Stream<Option<Domain>>> pair) {
                        final Domain dom = pair._1();
                        final Stream<Option<Domain>> subDoms = pair._2().filter(Option.<Domain>isSome_());

                        return dom.applications().isEmpty() && subDoms.isEmpty()
                                ? Option.<Domain>none()
                                : some(dom.withDomains(subDoms.toList().map(new F<Option<Domain>, Domain>() {
                            public Domain f(Option<Domain> d) {
                                return d.some();
                            }
                        })));
                    }
                });
            }
        });

        Debug._(this, "findDomaines", code, groups).effect(new P1<String>() {
            public String _1() {
                return "\n" +
                        result.either(
                                _("").<Exception>constant(),
                                Tree.show2D(Show.optionShow(shortDomainShow)).showS_()) +
                        "\n";
            }
        });

        return result;
    }

    private static <B, X> Either<X, Tree<B>> sequenceRightTree(final Tree<Either<X, B>> tree) {
        final Stream<Tree<Either<X, B>>> subForest = tree.subForest()._1();
        return subForest.isEmpty() ?
                tree.root().right().map(new F<B, Tree<B>>() {
                    public Tree<B> f(B b) {
                        return Tree.leaf(b);
                    }
                }) :
                tree.root().right().bind(new F<B, Either<X, Tree<B>>>() {
                    public Either<X, Tree<B>> f(final B b) {
                        return subForest.foldLeft(
                                new F2<Either<X, Tree<B>>, Tree<Either<X, B>>, Either<X, Tree<B>>>() {
                                    public Either<X, Tree<B>> f(Either<X, Tree<B>> ei, final Tree<Either<X, B>> t) {
                                        return ei.right().bind(new F<Tree<B>, Either<X, Tree<B>>>() {
                                            public Either<X, Tree<B>> f(final Tree<B> bs1) {
                                                return sequenceRightTree(t).right().map(new F<Tree<B>, Tree<B>>() {
                                                    public Tree<B> f(Tree<B> bs2) {
                                                        return Tree.node(bs1.root(), bs1.subForest()._1().snoc(bs2));
                                                    }
                                                });
                                            }
                                        });
                                    }
                                },
                                Either.<X, Tree<B>>right(Tree.node(b, Stream.<Tree<B>>nil())));
                    }
                });
    }
}
