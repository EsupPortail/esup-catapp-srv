package org.esupportail.catappsrvs.services.ldap;

import com.unboundid.ldap.sdk.*;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import lombok.Value;
import org.esupportail.catappsrvs.model.User;

import static fj.data.Array.array;
import static fj.data.Either.iif;
import static fj.data.Either.left;
import static fj.data.List.iterableList;
import static org.esupportail.catappsrvs.model.CommonTypes.LdapGroup;

@Value(staticConstructor = "of")
public class LdapSrv implements ILdap {
    String baseDn;
    String searchAttribute;
    LDAPInterface ldap;

    @Override
    public Either<Exception, List<LdapGroup>> getGroups(final User user) {
        final Filter uidFilter = Filter.createEqualityFilter("uid", user.uid().value());
        try {
            final SearchRequest request =
                    new SearchRequest(baseDn, SearchScope.ONE, uidFilter, searchAttribute);

            return Option.fromNull(ldap.searchForEntry(request))

                    .bind(entry -> Option.fromNull(entry.getAttribute(searchAttribute)))

                    .map(attribute -> array(attribute.getValues())
                            .map(v -> LdapGroup.of(v.toLowerCase()))
                            .toList())

                    .toEither(() -> new Exception("Aucune entrée dans le ldap ne correspond à " + user.uid().value()));
        } catch (LDAPException e) {
            return left(e);
        }
    }
}
