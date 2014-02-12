package org.esupportail.catappsrvs.services.config;

import org.esupportail.catappsrvs.dao.IApplicationDao;
import org.esupportail.catappsrvs.dao.IDomaineDao;
import org.esupportail.catappsrvs.dao.config.Daos;
import org.esupportail.catappsrvs.services.ApplicationSrv;
import org.esupportail.catappsrvs.services.DomaineSrv;
import org.esupportail.catappsrvs.services.IApplication;
import org.esupportail.catappsrvs.services.IDomaine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@Configuration
@Import(Daos.class)
public class Services {

    @Bean @Inject
    public IDomaine domaineSrv(IDomaineDao domaineDao) {
        return DomaineSrv.domaineSrv(domaineDao);
    }

    @Bean @Inject
    public IApplication applicationSrv(IApplicationDao applicationDao) {
        return ApplicationSrv.applicationSrv(applicationDao);
    }
}
