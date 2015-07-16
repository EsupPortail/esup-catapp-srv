package org.esupportail.catappsrvs.services.config;

import com.unboundid.ldap.sdk.*;
import org.esupportail.catappsrvs.dao.IApplicationDao;
import org.esupportail.catappsrvs.dao.IDomainDao;
import org.esupportail.catappsrvs.dao.config.Daos;
import org.esupportail.catappsrvs.services.ApplicationSrv;
import org.esupportail.catappsrvs.services.DomainSrv;
import org.esupportail.catappsrvs.services.IApplication;
import org.esupportail.catappsrvs.services.IDomain;
import org.esupportail.catappsrvs.services.ldap.ILdap;
import org.esupportail.catappsrvs.services.ldap.LdapSrv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;

@Configuration
@Import(Daos.class)
public class Services {

    @Bean @Inject
    public IDomain domaineSrv(IDomainDao domaineDao,
                               PlatformTransactionManager txManager,
                               ILdap ldap) {
        return DomainSrv.of(domaineDao, txManager, ldap);
    }

    @Bean @Inject
    public IApplication applicationSrv(IApplicationDao applicationDao,
                                       PlatformTransactionManager txManager) {
        return ApplicationSrv.of(applicationDao, txManager);
    }

    @Configuration
    static class LdapConf {
        @Value("${ldap.server}")
        private String server;

        @Value("${ldap.port}")
        private Integer port;

        @Value("${ldap.username}")
        private String username;

        @Value("${ldap.password}")
        private String password;

        @Value("${ldap.baseDn}")
        private String baseDn;

        @Value("${ldap.searchAttribute}")
        private String searchAttribute;

        @Bean
        public ILdap ldapSrv() {
            return LdapSrv.of(baseDn, searchAttribute, ldap());
        }

        private LDAPInterface ldap() {
            return new LDAPThreadLocalConnectionPool(
                    new SingleServerSet(server, port),
                    new SimpleBindRequest(username, password));
        }
    }
}
