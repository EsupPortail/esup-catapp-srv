package org.esupportail.catappsrvs.dao.config;

import fj.P1;
import fj.data.Either;
import org.esupportail.catappsrvs.dao.*;
import org.esupportail.catappsrvs.model.Application;
import org.esupportail.catappsrvs.model.Domaine;
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
        return DomaineDao.domaineDao(entityManager, new P1<ICrudDao<Application>>() {
            public ICrudDao<Application> _1() {
                return applicationDao();
            }
        });
    }

    @Bean
    IApplicationDao applicationDao() {
        return ApplicationDao.applicationDao(entityManager, new P1<ICrudDao<Domaine>>() {
            public ICrudDao<Domaine> _1() {
                return domaineDao();
            }
        });
    }
}
