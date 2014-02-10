package org.esupportail.catappsrvs.services;

import org.esupportail.catappsrvs.dao.IApplicationDao;
import org.esupportail.catappsrvs.model.Application;

public final class ApplicationSrv extends Crud<Application, IApplicationDao> implements IApplication {
    private ApplicationSrv(IApplicationDao dao) {
        super(dao);
    }

    public static ApplicationSrv applicationSrv(IApplicationDao dao) {
        return new ApplicationSrv(dao);
    }
}
