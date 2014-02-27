package org.esupportail.catappsrvs.web.config;

import org.esupportail.catappsrvs.services.IApplication;
import org.esupportail.catappsrvs.services.IDomain;
import org.esupportail.catappsrvs.services.config.Services;
import org.esupportail.catappsrvs.web.ApplicationResource;
import org.esupportail.catappsrvs.web.DomainResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@Configuration
@Import(Services.class)
public class AppConf {

    @Bean @Inject
    public DomainResource domaineResource(IDomain domaineSrv) {
        return DomainResource.domaineResource(domaineSrv);
    }

    @Bean @Inject
    public ApplicationResource applicationResource(IApplication applicationSrv) {
        return ApplicationResource.applicationResource(applicationSrv);
    }
}

