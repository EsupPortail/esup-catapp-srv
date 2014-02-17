package org.esupportail.catappsrvs.web.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.esupportail.catappsrvs.model.Domaine;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
@Value @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true) @Wither
public class JsDom implements JsHasCode<Domaine> {
    String code, libelle, parent;
    String[] domaines, applications;

    @JsonCreator
    public static JsDom jsDom(
            @JsonProperty("code") String code,
            @JsonProperty("wording") String libelle,
            @JsonProperty("parent") String parent,
            @JsonProperty("domains") String[] domaines,
            @JsonProperty("applications") String[] applications) {
        return new JsDom(code, libelle, parent, domaines, applications);
    }
}
