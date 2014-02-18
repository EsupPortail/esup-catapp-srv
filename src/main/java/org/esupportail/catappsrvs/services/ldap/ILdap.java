package org.esupportail.catappsrvs.services.ldap;

import fj.data.Either;
import fj.data.List;
import org.esupportail.catappsrvs.model.User;

import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;

public interface ILdap {
    Either<Exception, List<LdapGroup>> getGroups(User user);
}
