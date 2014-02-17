package org.esupportail.catappsrvs.services.ldap;

import com.unboundid.ldap.sdk.LDAPInterface;
import fj.data.List;
import lombok.Value;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.User;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Value(staticConstructor = "ldapSrv")
public class LdapSrv implements ILdap {
    LDAPInterface ldap;
    @Override
    public List<CommonTypes.LdapGroup> getGroups(User user) {
        throw new NotImplementedException();
    }
}
