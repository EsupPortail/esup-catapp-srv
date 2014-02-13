package org.esupportail.catappsrvs.services;

import org.esupportail.catappsrvs.dao.IDomaineDao;
import org.esupportail.catappsrvs.model.Domaine;
import org.springframework.transaction.PlatformTransactionManager;

public final class DomaineSrv extends Crud<Domaine, IDomaineDao> implements IDomaine {
    private DomaineSrv(IDomaineDao dao, PlatformTransactionManager txManager) { super(dao, txManager); }

    public static DomaineSrv domaineSrv(IDomaineDao dao, PlatformTransactionManager txManager) {
        return new DomaineSrv(dao, txManager);
    }
}
