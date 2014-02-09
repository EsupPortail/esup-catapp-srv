package org.esupportail.catappsrvs.dao;

import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.model.QDomaine;

import javax.persistence.EntityManager;

public final class DomaineDao extends CrudDao<Domaine> implements IDomaineDao {
    private DomaineDao(EntityManager entityManager) {
        super(entityManager, new QDomaine("domaine"), Domaine.class);
    }

    public static DomaineDao domaineDao(EntityManager entityManager) {
        return new DomaineDao(entityManager);
    }
}
