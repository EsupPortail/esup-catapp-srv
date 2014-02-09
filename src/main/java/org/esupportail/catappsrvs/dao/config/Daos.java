package org.esupportail.catappsrvs.dao.config;

import org.esupportail.catappsrvs.dao.ApplicationDao;
import org.esupportail.catappsrvs.dao.DomaineDao;
import org.esupportail.catappsrvs.dao.IApplicationDao;
import org.esupportail.catappsrvs.dao.IDomaineDao;
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
    IDomaineDao domaineDao() {
        return DomaineDao.domaineDao(entityManager);
    }

    @Bean
    IApplicationDao applicationDao() {
        return ApplicationDao.applicationDao(entityManager);
    }
}
