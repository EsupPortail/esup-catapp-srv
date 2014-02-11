package org.esupportail.catappsrvs.services;

import org.esupportail.catappsrvs.dao.IDomaineDao;
import org.esupportail.catappsrvs.model.Domaine;

public final class DomaineSrv extends Crud<Domaine, IDomaineDao> implements IDomaine {
    private DomaineSrv(IDomaineDao dao) { super(dao); }

    public static DomaineSrv domaineSrv(IDomaineDao dao) {
        return new DomaineSrv(dao);
    }
}
