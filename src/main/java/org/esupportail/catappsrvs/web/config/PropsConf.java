package org.esupportail.catappsrvs.web.config;

import fj.Function;
import fj.data.Array;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import static fj.data.Array.single;
import static fj.data.Option.fromString;

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
                                         .map(Function.<String, Resource>vary(FileSystemResource::new))
                                         .toArray())
                             .array(Resource[].class));
            setIgnoreUnresolvablePlaceholders(true);
        }};
    }
}
