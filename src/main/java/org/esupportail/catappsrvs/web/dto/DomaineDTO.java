package org.esupportail.catappsrvs.web.dto;

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
public class DomaineDTO {
    String code, libelle, parent;
    String[] domaines, applications;

    @JsonCreator
    public static DomaineDTO domaineDTO(
            @JsonProperty("code") String code,
            @JsonProperty("libelle") String libelle,
            @JsonProperty("parent") String parent,
            @JsonProperty("domaines") String[] domaines,
            @JsonProperty("applications") String[] applications) {
        return new DomaineDTO(code, libelle, parent, domaines, applications);
    }
}
