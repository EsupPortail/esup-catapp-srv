package org.esupportail.catappsrvs.dao;

import fj.F;
import fj.F3;
import fj.P1;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.model.QDomaine;

import javax.persistence.EntityManager;

import static fj.Function.curry;
import static fj.data.Option.none;
import static org.esupportail.catappsrvs.model.Versionned.Version;

public final class DomaineDao extends CrudDao<Domaine> implements IDomaineDao {
    private final P1<ICrudDao<Application>> appDao;

    private DomaineDao(EntityManager entityManager,
                       P1<ICrudDao<Application>> appDao) {
        super(entityManager, new QDomaine("domaine"), Domaine.class);
        this.appDao = appDao;
    }

    public static DomaineDao domaineDao(EntityManager entityManager,
                                        P1<ICrudDao<Application>> appDao) {
        return new DomaineDao(entityManager, appDao);
    }

    @Override
    protected Either<Exception, Domaine> prepareEntity(final Domaine domaine) {
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
                domaine.sousDomaines().map(new F<Domaine, Either<Exception, Domaine>>() {
                    public Either<Exception, Domaine> f(Domaine dom) {
                        return read(dom.code(), Option.<Version>none());
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
                                        .withSousDomaines(ssdoms);
                            }
                        }))));
    }

    @Override
    protected Either<Exception, Domaine> refineEntity(Domaine domaine) {
        return Either.right(domaine
                .withSousDomaines(domaine.sousDomaines())
                .withApplications(domaine.applications()));
    }
}
