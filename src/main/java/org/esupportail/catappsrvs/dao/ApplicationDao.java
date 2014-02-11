package org.esupportail.catappsrvs.dao;

import fj.F;
import fj.P1;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.model.QApplication;

import javax.persistence.EntityManager;

import static org.esupportail.catappsrvs.model.Versionned.Version;

public final class ApplicationDao extends CrudDao<Application> implements IApplicationDao {
    private final P1<ICrudDao<Domaine>> domaineDao;

    private ApplicationDao(EntityManager entityManager,
                           P1<ICrudDao<Domaine>> domaineDao) {
        super(entityManager, new QApplication("application"), Application.class);
        this.domaineDao = domaineDao;
    }

    public static ApplicationDao applicationDao(EntityManager entityManager,
                                                P1<ICrudDao<Domaine>> domaineDao) {
        return new ApplicationDao(entityManager, domaineDao);
    }

    @Override
    protected Either<Exception, Application> completeEntity(final Application application) {
        final List<Either<Exception, Domaine>> domaines =
                application.domaines().map(new F<Domaine, Either<Exception, Domaine>>() {
                    public Either<Exception, Domaine> f(Domaine domaine) {
                        return domaineDao._1().read(domaine.code(), Option.<Version>none());
                    }
                });
        return Either
                .sequenceRight(domaines)
                .right()
                .map(new F<List<Domaine>, Application>() {
                    public Application f(List<Domaine> domaines) {
                        return application.withDomaines(domaines);
                    }
                });
    }
}
