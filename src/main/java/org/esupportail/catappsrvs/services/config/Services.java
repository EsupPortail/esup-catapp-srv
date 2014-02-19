package org.esupportail.catappsrvs.services.config;

import com.unboundid.ldap.sdk.*;
import org.esupportail.catappsrvs.dao.IApplicationDao;
import org.esupportail.catappsrvs.dao.IDomaineDao;
import org.esupportail.catappsrvs.dao.config.Daos;
import org.esupportail.catappsrvs.services.ApplicationSrv;
import org.esupportail.catappsrvs.services.DomaineSrv;
import org.esupportail.catappsrvs.services.IApplication;
import org.esupportail.catappsrvs.services.IDomaine;
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
    public IDomaine domaineSrv(IDomaineDao domaineDao,
                               PlatformTransactionManager txManager,
                               ILdap ldap) {
        return DomaineSrv.domaineSrv(domaineDao, txManager, ldap);
    }

    @Bean @Inject
    public IApplication applicationSrv(IApplicationDao applicationDao,
                                       PlatformTransactionManager txManager) {
        return ApplicationSrv.applicationSrv(applicationDao, txManager);
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

        @Bean
        public ILdap ldapSrv() {
            return LdapSrv.ldapSrv(baseDn, ldap());
        }

        private LDAPInterface ldap() {
            return new LDAPThreadLocalConnectionPool(
                    new SingleServerSet(server, port),
                    new SimpleBindRequest(username, password));
        }
    }
}
