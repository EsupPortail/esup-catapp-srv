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
public class ApplicationDTO implements IDTO<Application> {
    String code, titre, libelle, url, accessibilite, description, groupe;
    String[] domaines;

    @JsonCreator
    public static ApplicationDTO applicationDTO(@JsonProperty("code") String code,
                                                @JsonProperty("titre") String titre,
                                                @JsonProperty("libelle") String libelle,
                                                @JsonProperty("url") String url,
                                                @JsonProperty("accessibilite") String accessibilite,
                                                @JsonProperty("description") String description,
                                                @JsonProperty("groupe") String groupe,
                                                @JsonProperty("domaines") String[] domaines) {
        return new ApplicationDTO(code, titre, libelle, url, accessibilite, description, groupe, domaines);
    }
}
