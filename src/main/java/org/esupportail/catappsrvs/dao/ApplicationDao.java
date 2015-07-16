package org.esupportail.catappsrvs.dao;

import fj.F0;
import fj.P;
import fj.P1;
import fj.data.Either;
import fj.data.List;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.model.QApplication;

import javax.persistence.EntityManager;

import static fj.data.Either.left;
import static fj.data.Either.right;

public final class ApplicationDao extends CrudDao<Application> implements IApplicationDao {
    private static final QApplication qapp = QApplication.application;

    private final P1<ICrudDao<Domain>> domaineDao;

    private ApplicationDao(EntityManager entityManager,
                           F0<ICrudDao<Domain>> domaineDao) {
        super(entityManager, qapp, Application.class);
        this.domaineDao = P.lazy(domaineDao).hardMemo();
    }

    public static ApplicationDao of(EntityManager entityManager,
                                    F0<ICrudDao<Domain>> domaineDao) {
        return new ApplicationDao(entityManager, domaineDao);
    }

    @Override
    protected Either<Exception, Application> prepare(final Application application) {
        final List<Either<Exception, Domain>> domains =
            application.domains().map(domain -> domaineDao._1().read(domain.code()));
        return Either
            .sequenceRight(domains)
            .right()
            .map(application::withDomains);
    }

    public Either<Exception, Application> create(Application application) {
        return prepare(application)
            .right()
            .bind(prepared -> {
                try {
                    final Application persistedApp = prepared.withDomains(List.nil());

                    entityManager.persist(persistedApp);

                    final Either<Exception, List<Domain>> updatedDoms =
                        Either.sequenceRight(prepared.domains().map(domain -> {
                            final List<Application> apps =
                                domain.applications().removeAll(Application.eq.eq(persistedApp));
                            return domaineDao._1().update(domain.withApplications(apps.cons(persistedApp)));
                        }));

                    return Either.<Exception, Application>right(persistedApp)
                                 .right()
                                 .apply(updatedDoms.right().map(ds -> app -> app.withDomains(ds)));
                } catch (Exception e) {
                    return left(e);
                }
            });
    }

    public Either<Exception, Application> update(Application application) {
        return read(application.code())
            .right()
            .apply(prepare(application)
                       .right()
                       .map(P.p2()))
            .right()
            .bind(pair -> {
                try {
                    final Application reloaded = pair._2();
                    final Application prepared = pair._1();

                    reloaded.domains().foreachDoEffect(domain -> {
                        final List<Application> apps =
                            domain.applications().removeAll(Application.eq.eq(reloaded));
                        entityManager.merge(domain.withApplications(apps));
                    });

                    final Application merged =
                        entityManager.merge(reloaded.withCode(prepared.code())
                                                    .withTitle(prepared.title())
                                                    .withCaption(prepared.caption())
                                                    .withDescription(prepared.description())
                                                    .withGroup(prepared.group())
                                                    .withUrl(prepared.url())
                                                    .withActivation(prepared.activation())
                                                    .withDomains(List.nil()));

                    final Either<Exception, List<Domain>> newDoms =
                        Either.sequenceRight(prepared.domains().map(domain -> {
                            try {
                                final List<Application> apps =
                                    domain.applications().removeAll(Application.eq.eq(merged));
                                return right(entityManager.merge(domain.withApplications(apps.cons(merged))));
                            } catch (Exception e) {
                                return left(e);
                            }
                        }));

                    return Either.<Exception, Application>right(merged)
                                 .right()
                                 .apply(newDoms.right().map(ds -> app -> app.withDomains(ds)));
                } catch (Exception e) {
                    return left(e);
                }
            });
    }

    @Override
    protected Either<Exception, Application> refine(Application application) {
        return right(application.withDomains(application.domains())); // force le chargement (hibernate)
    }
}
