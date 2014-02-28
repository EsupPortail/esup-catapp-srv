package org.esupportail.catappsrvs.dao;

import fj.*;
import fj.data.Either;
import fj.data.List;
import org.esupportail.catappsrvs.model.*;

import javax.persistence.EntityManager;

import static fj.Function.curry;
import static fj.data.Either.left;
import static fj.data.Either.right;

public final class ApplicationDao extends CrudDao<Application> implements IApplicationDao {
    private static final QApplication qapp = QApplication.application;

    private final P1<ICrudDao<Domain>> domaineDao;

    private ApplicationDao(EntityManager entityManager,
                           P1<ICrudDao<Domain>> domaineDao) {
        super(entityManager, qapp, Application.class);
        this.domaineDao = domaineDao;
    }

    public static ApplicationDao applicationDao(EntityManager entityManager,
                                                P1<ICrudDao<Domain>> domaineDao) {
        return new ApplicationDao(entityManager, domaineDao);
    }

    @Override
    protected Either<Exception, Application> prepare(final Application application) {
        final List<Either<Exception, Domain>> domains =
                application.domains().map(new F<Domain, Either<Exception, Domain>>() {
                    public Either<Exception, Domain> f(Domain domain) {
                        return domaineDao._1().read(domain.code());
                    }
                });
        return Either
                .sequenceRight(domains)
                .right()
                .map(new F<List<Domain>, Application>() {
                    public Application f(final List<Domain> domains) {
                        return application.withDomains(domains);
                    }
                });
    }

    public Either<Exception, Application> create(Application application) {
        return prepare(application)
                .right()
                .bind(new F<Application, Either<Exception, Application>>() {
                    public Either<Exception, Application> f(Application prepared) {
                        try {
                            final Application persistedApp = prepared.withDomains(List.<Domain>nil());
                            entityManager.persist(persistedApp);
                            final Either<Exception, List<Domain>> updatedDoms =
                                    Either.sequenceRight(prepared.domains().map(new F<Domain, Either<Exception, Domain>>() {
                                        public Either<Exception, Domain> f(Domain domain) {
                                            final List<Application> apps =
                                                    domain.applications().removeAll(Equal.<Application>anyEqual().eq(persistedApp));
                                            return domaineDao._1().update(domain.withApplications(apps.cons(persistedApp)));
                                        }
                                    }));
                            return Either.<Exception, Application>right(persistedApp)
                                    .right()
                                    .apply(updatedDoms.right().map(curry(new F2<List<Domain>, Application, Application>() {
                                        public Application f(List<Domain> ds, Application app) {
                                            return app.withDomains(ds);
                                        }
                                    })));
                        } catch (Exception e) {
                            return left(e);
                        }
                    }
                });
    }

    public Either<Exception, Application> update(Application application) {
        return read(application.code()).right()
                .apply(prepare(application).right()
                        .map(P.<Application, Application>p2())).right()
                .bind(new F<P2<Application, Application>, Either<Exception, Application>>() {
                    public Either<Exception, Application> f(P2<Application, Application> pair) {
                        try {
                            final Application reloaded = pair._2();
                            final Application prepared = pair._1();

                            reloaded.domains().foreach(new Effect<Domain>() {
                                public void e(Domain domain) {
                                    final List<Application> apps =
                                            domain.applications().removeAll(Equal.<Application>anyEqual().eq(reloaded));
                                    entityManager.merge(domain.withApplications(apps));
                                }
                            });

                            final Application merged = entityManager.merge(reloaded
                                    .withCode(prepared.code())
                                    .withTitle(prepared.title())
                                    .withCaption(prepared.caption())
                                    .withDescription(prepared.description())
                                    .withGroup(prepared.group())
                                    .withUrl(prepared.url())
                                    .withActivation(prepared.activation())
                                    .withDomains(List.<Domain>nil()));

                            final Either<Exception, List<Domain>> newDoms =
                                    Either.sequenceRight(prepared.domains().map(new F<Domain, Either<Exception, Domain>>() {
                                        public Either<Exception, Domain> f(Domain domain) {
                                            try {
                                                final List<Application> apps =
                                                        domain.applications().removeAll(Equal.<Application>anyEqual().eq(merged));
                                                return right(entityManager.merge(domain.withApplications(apps.cons(merged))));
                                            } catch (Exception e) {
                                                return left(e);
                                            }
                                        }
                                    }));

                            return Either.<Exception, Application>right(merged)
                                    .right()
                                    .apply(newDoms.right().map(curry(new F2<List<Domain>, Application, Application>() {
                                        public Application f(List<Domain> ds, Application app) {
                                            return app.withDomains(ds);
                                        }
                                    })));
                        } catch (Exception e) {
                            return left(e);
                        }
                    }
                });
    }

    @Override
    protected Either<Exception, Application> refine(Application application) {
        return right(application.withDomains(application.domains())); // force le chargement (hibernate)
    }
}
