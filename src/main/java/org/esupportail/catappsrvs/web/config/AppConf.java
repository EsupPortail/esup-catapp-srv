package org.esupportail.catappsrvs.web.config;

import org.esupportail.catappsrvs.dao.config.DaoConf;
import org.esupportail.catappsrvs.web.DomaineResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DaoConf.class)
public class AppConf {

    @Bean
    public DomaineResource domaineResource() { return DomaineResource._; }

    @Bean
    public Object testObj() { return new Object() {
        public String toString() {
            return "WARZUP !!!!!!!!!!!!!!!!!!";
        }
    }; }
}
