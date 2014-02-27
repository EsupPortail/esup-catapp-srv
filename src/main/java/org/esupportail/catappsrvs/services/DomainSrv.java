package org.esupportail.catappsrvs.services;

import fj.F;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import fj.data.Tree;
import org.esupportail.catappsrvs.dao.IDomainDao;
import org.esupportail.catappsrvs.model.Domain;
import org.esupportail.catappsrvs.model.User;
import org.esupportail.catappsrvs.services.ldap.ILdap;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import static org.esupportail.catappsrvs.model.CommonTypes.Code;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;

public final class DomainSrv extends Crud<Domain, IDomainDao> implements IDomain {
    private final ILdap ldap;

    private DomainSrv(IDomainDao dao,
                      PlatformTransactionManager txManager,
                      ILdap ldap) {
        super(dao, txManager);
        this.ldap = ldap;
    }

    public static DomainSrv domaineSrv(IDomainDao dao,
                                        PlatformTransactionManager txManager,
                                        ILdap ldap) {
        return new DomainSrv(dao, txManager, ldap);
    }

    @Override
    public Either<Exception, Tree<Option<Domain>>> findDomaines(final Code code, final User user) {
        return inTransaction(readTemplate, new TransactionCallback<Either<Exception, Tree<Option<Domain>>>>() {
            public Either<Exception, Tree<Option<Domain>>> doInTransaction(TransactionStatus status) {
                return ldap
                        .getGroups(user)
                        .right()
                        .bind(new F<List<LdapGroup>, Either<Exception, Tree<Option<Domain>>>>() {
                            public Either<Exception, Tree<Option<Domain>>> f(List<LdapGroup> groups) {
                                return dao.findDomaines(code, groups);
                            }
                        });
            }
        });


    }
}
