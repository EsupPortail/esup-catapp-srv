package org.esupportail.catappsrvs.web.config;

import fj.F;
import fj.data.Array;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static fj.data.Array.array;
import static fj.data.Array.single;
import static fj.data.Option.fromString;
import static java.util.Arrays.asList;

@Configuration
public class PropsConf {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer(
            @Value("#{systemProperties['config.location']?:''}") final String configLocation) {
        final Array<Resource> resources =
                single((Resource) new ClassPathResource("/properties/defaults.properties"));

        return new PropertySourcesPlaceholderConfigurer() {{
            setLocations(resources
                    .append(fromString(configLocation)
                            .map(new F<String, Resource>() {
                                public Resource f(String loc) {
                                    return new FileSystemResource(loc);
                                }
                            })
                            .toArray())
                    .array(Resource[].class));
            setIgnoreUnresolvablePlaceholders(true);
        }};
    }
}
