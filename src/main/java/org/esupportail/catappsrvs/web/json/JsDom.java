package org.esupportail.catappsrvs.web.json;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.esupportail.catappsrvs.model.Domain;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
@Value @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true) @Wither
public class JsDom implements JsHasCode<Domain> {
    String code, caption, parent;
    String[] domains, applications;

    @JsonCreator
    public static JsDom jsDom(
            @JsonProperty("code") String code,
            @JsonProperty("caption") String caption,
            @JsonProperty("parent") String parent,
            @JsonProperty("domains") String[] domains,
            @JsonProperty("applications") String[] applications) {
        return new JsDom(code, caption, parent, domains, applications);
    }
}
