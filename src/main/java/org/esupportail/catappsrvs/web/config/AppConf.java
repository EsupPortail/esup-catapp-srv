package org.esupportail.catappsrvs.web.config;

import org.esupportail.catappsrvs.services.config.Services;
import org.esupportail.catappsrvs.web.DomaineResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.esupportail.catappsrvs.web.DomaineResource.DomainResource;

@Configuration
@Import(Services.class)
public class AppConf {

    @Bean
    public DomaineResource domaineResource() { return DomainResource; }

    @Bean
    public Object testObj() { return new Object() {
        public String toString() {
            return "WARZUP !!!!!!!!!!!!!!!!!!";
        }
    }; }
}
