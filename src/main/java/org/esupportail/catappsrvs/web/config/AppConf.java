package org.esupportail.catappsrvs.web.config;

import org.esupportail.catappsrvs.services.IApplication;
import org.esupportail.catappsrvs.services.IDomaine;
import org.esupportail.catappsrvs.services.config.Services;
import org.esupportail.catappsrvs.web.ApplicationResource;
import org.esupportail.catappsrvs.web.DomaineResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@Configuration
@Import(Services.class)
public class AppConf {

    @Bean @Inject
    public DomaineResource domaineResource(IDomaine domaineSrv) {
        return DomaineResource.domaineResource(domaineSrv);
    }

    @Bean @Inject
    public ApplicationResource applicationResource(IApplication applicationSrv) {
        return ApplicationResource.applicationResource(applicationSrv);
    }
}

