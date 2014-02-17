package org.esupportail.catappsrvs.web.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.esupportail.catappsrvs.model.Application;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;

@Value @AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = ANY)
@Accessors(fluent = true) @Wither
public class JsApp implements JsHasCode<Application> {
    String code, titre, libelle, url, description, groupe;
    Acces acces;
    String[] domaines;

    @JsonCreator
    public static JsApp jsApp(@JsonProperty("code") String code,
                              @JsonProperty("title") String titre,
                              @JsonProperty("wording") String libelle,
                              @JsonProperty("url") String url,
                              @JsonProperty("accessibility") Acces accessibilite,
                              @JsonProperty("description") String description,
                              @JsonProperty("group") String groupe,
                              @JsonProperty("domains") String[] domaines) {
        return new JsApp(code, titre, libelle, url, description, groupe, accessibilite, domaines);
    }

    public static enum Acces { Unaccessible, Accessible }
}
