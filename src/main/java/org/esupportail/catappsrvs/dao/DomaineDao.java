package org.esupportail.catappsrvs.dao;

import fj.*;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import org.esupportail.catappsrvs.model.*;

import javax.persistence.EntityManager;

import static fj.Function.curry;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.Option.some;
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
    protected Either<Exception, Domaine> prePersist(final Domaine domaine) {
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
                                        .withSousDomaines(ssdoms);
                            }
                        }))));
    }

    @Override
    protected Either<Exception, Domaine> persist(final Domaine domaine) {
        final List<Domaine> oldDoms = domaine.sousDomaines();
        final List<Application> oldApps = domaine.applications();

        try {
            final Domaine realDomaine = domaine
                    .withSousDomaines(List.<Domaine>nil())
                    .withApplications(List.<Application>nil());

            entityManager.persist(realDomaine);

            final Either<Exception, List<Domaine>> newSousDoms =
                    Either.sequenceRight(oldDoms.map(new F<Domaine, Either<Exception, Domaine>>() {
                        public Either<Exception, Domaine> f(Domaine dom) {
                            final Domaine subDom = Domaine.domaine(
                                            dom.version(),
                                            dom.code(),
                                            dom.libelle(),
                                            some(realDomaine),
                                            dom.sousDomaines(),
                                            dom.applications());
                            return update(subDom);
                        }
                    }));

            final Either<Exception, List<Application>> newApps =
                    Either.sequenceRight(oldApps.map(new F<Application, Either<Exception, Application>>() {
                        public Either<Exception, Application> f(Application app) {
                            return appDao._1()
                                    .create(app.withVersion(app.version().plus(1)));
                        }
                    }));

            newSousDoms.right()
                    .apply(newApps.right()
                            .map(curry(new F2<List<Application>, List<Domaine>, Domaine>() {
                                public Domaine f(List<Application> apps, List<Domaine> doms) {
                                    return realDomaine.withSousDomaines(doms).withApplications(apps);
                                }
                            }))).right()
                    .foreach(new Effect<Domaine>() {
                        public void e(Domaine dom) {
                            entityManager.merge(dom);
                        }
                    });

            return right(realDomaine);
        } catch (Exception e) {
            return left(e);
        }
    }

    @Override
    protected Either<Exception, Domaine> postPersist(Domaine domaine) {
        return Either.right(domaine
                .withSousDomaines(domaine.sousDomaines())
                .withApplications(domaine.applications()));
    }
}
