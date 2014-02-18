package org.esupportail.catappsrvs.services.ldap;

import com.unboundid.ldap.sdk.*;
import fj.F;
import fj.P1;
import fj.data.Either;
import fj.data.List;
import lombok.Value;
import org.esupportail.catappsrvs.model.CommonTypes;
import org.esupportail.catappsrvs.model.User;

import static fj.data.Either.iif;
import static fj.data.Either.left;
import static fj.data.Either.right;
import static fj.data.List.iterableList;
import static fj.data.Option.fromNull;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup.ldapGroup;
import static org.esupportail.catappsrvs.model.User.Uid;

@Value(staticConstructor = "ldapSrv")
public class LdapSrv implements ILdap {
    String baseDn;
    LDAPInterface ldap;

    @Override
    public Either<Exception, List<LdapGroup>> getGroups(final User user) {
        final Filter uidFilter = Filter.createEqualityFilter("uid", user.uid.value);
        try {
            final SearchRequest request =
                    new SearchRequest(baseDn, SearchScope.ONE, uidFilter, "memberOf");
            final java.util.List<SearchResultEntry> results =
                    ldap.search(request).getSearchEntries();
            return iif(!results.isEmpty(),
                    new P1<List<LdapGroup>>() {
                        public List<LdapGroup> _1() {
                            return iterableList(results.get(0).getAttributes())
                                    .map(new F<Attribute, LdapGroup>() {
                                        public LdapGroup f(Attribute attribute) {
                                            return ldapGroup(attribute.getValue());
                                        }
                                    });
                        }
                    },
                    new P1<Exception>() {
                        public Exception _1() {
                            return new Exception("Aucune entrée dans le ldap ne correspond à " + user);
                        }
                    }
            );
        } catch (LDAPException e) {
            return left((Exception) e);
        }
    }
}
