package org.esupportail.catappsrvs.web.json;

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
    String code, title, caption, url, description, group;
    JsActivation activation;
    String[] domains;

    @JsonCreator
    public static JsApp jsApp(@JsonProperty("code") String code,
                              @JsonProperty("title") String title,
                              @JsonProperty("caption") String caption,
                              @JsonProperty("url") String url,
                              @JsonProperty("activation") JsActivation activation,
                              @JsonProperty("description") String description,
                              @JsonProperty("group") String group,
                              @JsonProperty("domains") String[] domains) {
        return new JsApp(code, title, caption, url, description, group, activation, domains);
    }

    public enum JsActivation {Deactivated, Activated}
}
