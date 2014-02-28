package org.esupportail.catappsrvs.model.utils;

import fj.F;
import fj.Show;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.esupportail.catappsrvs.model.Domain;

import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Shows {
    public static final Show<Domain> shortDomainShow = Show.showS(new F<Domain, String>() {
        public String f(Domain domain) {
            return format("Domain(%s)", domain.code());
        }
    });

}
