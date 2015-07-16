package org.esupportail.catappsrvs.dao;

import fj.*;
import fj.data.*;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.model.QDomain;

import javax.persistence.EntityManager;

import static fj.P.p;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.Option.some;
import static fj.data.Stream.iterableStream;
import static java.lang.String.format;
import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;
import static org.esupportail.catappsrvs.utils.logging.Log.Debug;

public final class DomainDao extends CrudDao<Domain> implements IDomainDao {
    private final P1<ICrudDao<Application>> appDao;

    private static final QDomain dom = QDomain.domain;

    private DomainDao(EntityManager entityManager,
                      F0<ICrudDao<Application>> appDao) {
        super(entityManager, dom, Domain.class);
        this.appDao = P.lazy(appDao).hardMemo();
    }

    public static DomainDao of(EntityManager entityManager,
                               F0<ICrudDao<Application>> appDao) {
        return new DomainDao(entityManager, appDao);
    }

    @Override
    Either<Exception, Domain> prepare(final Domain domain) {
        final Either<Exception, Option<Domain>> parent =
            domain.parent()
                  .map(dom1 -> read(dom1.code()).right().map(Option.some_()))
                  .orSome(Either.right(Option.none()));

        final List<Either<Exception, Domain>> subDomains =
            domain.domains().map(dom1 -> read(dom1.code()));

        final List<Either<Exception, Application>> applications =
            domain.applications().map(application -> appDao._1().read(application.code()));

        return Either
            .sequenceRight(subDomains)
            .right()
            .apply(Either
                       .sequenceRight(applications)
                       .right()
                       .apply(parent.right().map(par -> apps -> ssdoms -> domain
                           .withParent(par)
                           .withApplications(apps)
                           .withDomains(ssdoms))));
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
            .bind(prepared -> {
                try {
                    final Domain persistedDom =
                        prepared.withDomains(List.nil()).withApplications(List.nil());

                    entityManager.persist(persistedDom);

                    final Either<Exception, List<Domain>> updatedDoms =
                        Either.sequenceRight(prepared.domains().map(domain1 -> {
                            try {
                                return right(entityManager.merge(domain1.withParent(some(persistedDom))));
                            } catch (Exception e) {
                                return left(e);
                            }
                        }));

                    final Either<Exception, List<Application>> updatedApps =
                        Either.sequenceRight(prepared.applications().map(app -> {
                            final List<Domain> doms =
                                app.domains().removeAll(Domain.eq.eq(persistedDom));
                            return appDao._1().update(app.withDomains(doms.cons(persistedDom)));
                        }));

                    return updatedApps
                        .right()
                        .apply(updatedDoms.right()
                                          .apply(Either.<Exception, Domain>right(persistedDom)
                                                       .right()
                                                       .map(dom -> ds -> apps -> dom
                                                           .withDomains(ds)
                                                           .withApplications(apps))));
                } catch (Exception e) {
                    return left(e);
                }
            });
    }

    @Override
    public Either<Exception, Domain> update(final Domain domain) {
        return read(domain.code())
            .right()
            .apply(prepare(domain)
                       .right()
                       .map(P.p2()))
            .right()
            .bind(pair -> {
                try {
                    final Domain reloaded = pair._2();
                    final Domain prepared = pair._1();

                    reloaded.applications().foreachDoEffect(app -> {
                        final List<Domain> doms =
                            app.domains().removeAll(Domain.eq.eq(reloaded));
                        appDao._1().update(app.withDomains(doms));
                    });
                    reloaded.domains().foreachDoEffect(dom1 -> entityManager
                        .merge(dom1.withParent(reloaded.parent())));

                    final Domain merged = entityManager
                        .merge(reloaded.withCode(prepared.code())
                                       .withCaption(prepared.caption())
                                       .withParent(prepared.parent())
                                       .withDomains(List.nil())
                                       .withApplications(List.nil()));

                    final Either<Exception, List<Domain>> updatedDoms =
                        Either.sequenceRight(prepared.domains().map(prepDom -> {
                            try {
                                return right(entityManager.merge(prepDom.withParent(some(merged))));
                            } catch (Exception e) {
                                return left(e);
                            }
                        }));

                    final Either<Exception, List<Application>> updatedApps =
                        Either.sequenceRight(prepared.applications().map(app -> {
                            final List<Domain> doms =
                                app.domains().removeAll(Domain.eq.eq(merged));
                            return appDao._1().update(app.withDomains(doms.cons(merged)));
                        }));

                    return Either
                        .<Exception, Domain>right(merged)
                        .right()
                        .apply(updatedDoms
                                   .right()
                                   .apply(updatedApps.right()
                                                     .map(as -> ds -> d -> d
                                                         .withDomains(ds).withApplications(as))));
                } catch (Exception e) {
                    return left(e);
                }
            });
    }

    @Override
    public Either<Exception, Tree<Option<Domain>>> findDomaines(Code code, final List<LdapGroup> groups) {
        final F<Either<Exception, Domain>, Tree<Either<Exception, Domain>>> treeFunc =
            Tree.unfoldTree(maybeNode -> {
                final Either<Exception, Domain> newNode =
                    maybeNode.right().bind(dom1 -> {
                        try {
                            final List<Application> apps =
                                dom1.applications().filter(app -> groups.exists(LdapGroup.eq.eq(app.group())));
                            final List<Domain> doms = dom1.domains();
                            return right(dom1.withDomains(doms).withApplications(apps));
                        } catch (Exception e) {
                            return left(e);
                        }
                    });

                final Stream<Either<Exception, Domain>> children = newNode.<Stream<Either<Exception, Domain>>>
                    either(__ -> Stream.nil(),
                           d -> iterableStream(d.domains()).map(Either.right_()));

                return p(newNode, p(children));
            });

        final Either<Exception, Tree<Domain>> appsFilteredTree = sequenceRightTree(treeFunc.f(read(code)));

        final Either<Exception, Tree<Option<Domain>>> result =
            appsFilteredTree.right().map(tdoms -> Tree.bottomUp(tdoms, P2
                .tuple(dom1 -> subDoms -> dom1.applications().isEmpty() && subDoms.isEmpty()
                                          ? Option.<Domain>none()
                                          : some(dom1.withDomains(subDoms.toList().map(d -> d.some()))))));

        Debug._(this, "findDomaines", code, groups)
             .effect(() -> "\n" +
                           result.either(__ -> "",
                                         Tree.show2D(Show.optionShow(shortDomainShow)).showS_()) +
                           "\n");

        return result;
    }

    private static <B, X> Either<X, Tree<B>> sequenceRightTree(final Tree<Either<X, B>> tree) {
        final Stream<Tree<Either<X, B>>> subForest = tree.subForest()._1();
        return subForest.isEmpty() ?
               tree.root().right().map(Tree::leaf) :
               tree.root().right().bind(b -> subForest
                   .foldLeft(ei -> t -> ei.right().bind(bs1 -> sequenceRightTree(t).right().map(bs2 -> Tree
                                 .node(bs1.root(), bs1.subForest()._1().snoc(bs2)))),
                             Either.right(Tree.node(b, Stream.<Tree<B>>nil()))));
    }

    private static final Show<Domain> shortDomainShow =
        Show.showS(domain -> format("Domain(%s)", domain.code()));
}
