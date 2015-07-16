package org.esupportail.catappsrvs.dao.config;

import org.esupportail.catappsrvs.dao.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
@Import(Conf.class)
public class Daos {

    @PersistenceContext
    EntityManager entityManager;

    @Bean
    IDomainDao domaineDao() {
        return DomainDao.of(entityManager, this::applicationDao);
    }

    @Bean
    IApplicationDao applicationDao() {
        return ApplicationDao.of(entityManager, this::domaineDao);
    }
}
