package org.esupportail.catappsrvs.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static java.util.Arrays.asList;

@Configuration
public class PropsConf {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer(
            @Value("#{systemProperties['config.location']?:''}") String configLocation) {
        final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = new Resource[] {
                new ClassPathResource("/properties/defaults.properties")
        };

        if (!configLocation.isEmpty()) {
            final List<Resource> resourceList = asList(resources);
            resourceList.add(new FileSystemResource(configLocation));
            resources = resourceList.toArray(new Resource[resourceList.size()]);
        }

        pspc.setLocations(resources);
        pspc.setIgnoreUnresolvablePlaceholders(true);

        return pspc;
    }
}
