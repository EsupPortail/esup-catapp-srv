package org.esupportail.catappsrvs.dao;

import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.QApplication;

import javax.persistence.EntityManager;

public class ApplicationDao extends CrudDao<Application> implements IApplicationDao {
    private ApplicationDao(EntityManager entityManager) {
        super(entityManager, new QApplication("application"), Application.class);
    }

    public static ApplicationDao applicationDao(EntityManager entityManager) {
        return new ApplicationDao(entityManager);
    }
}
