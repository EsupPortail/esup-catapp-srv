package org.esupportail.catappsrvs.web.json;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
@Value @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true) @Wither
public class JSDomTree {
    JsDom domain;
    JSDomTree[] subDomains;

    @JsonCreator
    public static JSDomTree jsDomTree(@JsonProperty("domain") JsDom domain,
                                      @JsonProperty("subDomains") JSDomTree[] subDomains) {
        return new JSDomTree(domain, subDomains);
    }
}
