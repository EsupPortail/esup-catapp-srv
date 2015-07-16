package org.esupportail.catappsrvs.services;

import org.esupportail.catappsrvs.dao.IApplicationDao;
import org.esupportail.catappsrvs.model.Application;
import org.springframework.transaction.PlatformTransactionManager;

public final class ApplicationSrv extends Crud<Application, IApplicationDao> implements IApplication {
    private ApplicationSrv(IApplicationDao dao, PlatformTransactionManager txManager) {
        super(dao, txManager);
    }

    public static ApplicationSrv of(IApplicationDao dao, PlatformTransactionManager txManager) {
        return new ApplicationSrv(dao, txManager);
    }
}
