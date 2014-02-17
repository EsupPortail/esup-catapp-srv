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
    public IDomaine domaineSrv(IDomaineDao domaineDao, PlatformTransactionManager txManager) {
        return DomaineSrv.domaineSrv(domaineDao, txManager);
    }

    @Bean @Inject
    public IApplication applicationSrv(IApplicationDao applicationDao, PlatformTransactionManager txManager) {
        return ApplicationSrv.applicationSrv(applicationDao, txManager);
    }

    @Bean @Inject
    public ILdap ldapSrv(LDAPInterface ldap) {
        return LdapSrv.ldapSrv(ldap);
    }

    @Configuration
    static class LdapConf {
        @Value("${ldap.toip.servername}")
        private String servername;

        @Value("${ldap.toip.port}")
        private Integer port;

        @Value("${ldap.toip.username}")
        private String username;

        @Value("${ldap.toip.password}")
        private String password;

        @Bean
        public LDAPInterface ldap() {
            return new LDAPThreadLocalConnectionPool(
                    new SingleServerSet(servername, port),
                    new SimpleBindRequest(username, password));
        }
    }
}
