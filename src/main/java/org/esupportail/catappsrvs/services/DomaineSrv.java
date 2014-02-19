package org.esupportail.catappsrvs.services;

import fj.F;
import fj.data.Either;
import fj.data.List;
import fj.data.Tree;
import org.esupportail.catappsrvs.dao.IDomaineDao;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.Domaine;
import org.esupportail.catappsrvs.model.User;
import org.esupportail.catappsrvs.services.ldap.ILdap;
import org.springframework.transaction.PlatformTransactionManager;

import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;

public final class DomaineSrv extends Crud<Domaine, IDomaineDao> implements IDomaine {
    private final ILdap ldap;

    private DomaineSrv(IDomaineDao dao,
                       PlatformTransactionManager txManager,
                       ILdap ldap) {
        super(dao, txManager);
        this.ldap = ldap;
    }

    public static DomaineSrv domaineSrv(IDomaineDao dao,
                                        PlatformTransactionManager txManager,
                                        ILdap ldap) {
        return new DomaineSrv(dao, txManager, ldap);
    }

    @Override
    public Either<Exception, Tree<Domaine>> findDomaines(User user) {
        return ldap
                .getGroups(user)
                .right()
                .bind(new F<List<LdapGroup>, Either<Exception, Tree<Domaine>>>() {
                    public Either<Exception, Tree<Domaine>> f(List<LdapGroup> groups) {
                        return dao.findDomaines(groups);
                    }
                });
    }
}
