package org.esupportail.catappsrvs.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.path.PathBuilder;
import fj.Equal;
import fj.F;
import fj.P1;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.model.QApplication;
import org.esupportail.catappsrvs.model.QDomaine;

import javax.persistence.EntityManager;

import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.List.iterableList;
import static org.esupportail.catappsrvs.model.Versionned.Version;

public final class ApplicationDao extends CrudDao<Application> implements IApplicationDao {
    private final P1<ICrudDao<Domaine>> domaineDao;

    private static final QDomaine dom = QDomaine.domaine;
    private static final QDomaine otherDom = new QDomaine("otherDom");

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
    protected Either<Exception, Application> prepare(final Application application) {
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

    @Override
    protected Either<Exception, Application> persist(final Application application) {
        final List<Domaine> oldDoms = application.domaines();
        try {
            final Application realApp =
                    application.withDomaines(List.<Domaine>nil());

            entityManager.persist(realApp);

            final Either<Exception, List<Domaine>> newDoms =
                    Either.sequenceRight(oldDoms.map(new F<Domaine, Either<Exception, Domaine>>() {
                        public Either<Exception, Domaine> f(Domaine dom) {
                            final List<Application> domOldApps =
                                    dom.applications().removeAll(Equal.<Application>anyEqual().eq(application));
                            try {
                                final Domaine newDom =
                                        entityManager.merge(dom.withApplications(domOldApps.snoc(realApp)));
                                entityManager.flush();
                                return right(newDom);
                            } catch (Exception e) {
                                return left(e);
                            }
                        }
                    }));

            return newDoms.right().bind(new F<List<Domaine>, Either<Exception, Application>>() {
                public Either<Exception, Application> f(List<Domaine> doms) {
                    try {
                        final Application newApp = entityManager.merge(realApp.withDomaines(doms));
                        entityManager.flush();
                        return right(newApp);
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
    protected Either<Exception, Application> refine(Application application) {
        final PathBuilder<Domaine> domPath = new PathBuilder<>(Domaine.class, dom.getMetadata());

        final java.util.List<Domaine> domaines = new JPAQuery(entityManager)
                .from(otherDom).where(otherDom.version.eq(new JPASubQuery()
                        .from(dom).where(dom.code.eq(otherDom.code)).unique(lastVersion(domPath)))
                        .and(otherDom.applications.contains(application)))
                .list(otherDom);

        return right(application.withDomaines(iterableList(domaines))); // force le chargement (hibernate)
    }
}
