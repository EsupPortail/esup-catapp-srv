package org.esupportail.catappsrvs.services;

import fj.data.Either;
import fj.data.Option;
import fj.data.Tree;
import org.esupportail.catappsrvs.dao.IDomainDao;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.model.User;
import org.esupportail.catappsrvs.services.ldap.ILdap;
import org.springframework.transaction.PlatformTransactionManager;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;

public final class DomainSrv extends Crud<Domain, IDomainDao> implements IDomain {
    private final ILdap ldap;

    private DomainSrv(IDomainDao dao,
                      PlatformTransactionManager txManager,
                      ILdap ldap) {
        super(dao, txManager);
        this.ldap = ldap;
    }

    public static DomainSrv of(IDomainDao dao,
                               PlatformTransactionManager txManager,
                               ILdap ldap) {
        return new DomainSrv(dao, txManager, ldap);
    }

    @Override
    public Either<Exception, Tree<Option<Domain>>> findDomaines(final Code code, final User user) {
        return inTransaction(readTemplate, status -> ldap
            .getGroups(user)
            .right()
            .bind(groups -> dao.findDomaines(code, groups)));
    }
}
