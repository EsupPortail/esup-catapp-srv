package org.esupportail.catappsrvs.dao;

import fj.F;
import fj.Show;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.QApplication;

import javax.persistence.EntityManager;

import static java.lang.String.format;

public final class ApplicationDao extends CrudDao<Application> implements IApplicationDao {
    private ApplicationDao(EntityManager entityManager) {
        super(entityManager, new QApplication("application"), Application.class, Show.showS(new F<Application, String>() {
            public String f(Application application) {
                return format("");
            }
        }));
    }

    public static ApplicationDao applicationDao(EntityManager entityManager) {
        return new ApplicationDao(entityManager);
    }
}
