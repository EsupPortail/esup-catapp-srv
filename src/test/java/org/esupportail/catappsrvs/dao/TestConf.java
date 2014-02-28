package org.esupportail.catappsrvs.dao;

import org.esupportail.catappsrvs.dao.config.Daos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Configuration
@Import(Daos.class)
public class TestConf {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer() {{
            setProperties(new Properties() {{
                setProperty("jpa.database.type", "HSQL");
                setProperty("jdbc.connection.driver_class", "org.hsqldb.jdbcDriver");
                setProperty("jdbc.connection.url", "jdbc:hsqldb:mem:catapp");
                setProperty("jdbc.connection.username", "catapp");
                setProperty("jdbc.connection.password", "");
                setProperty("hibernate.show_sql", "false");
                setProperty("hibernate.format_sql", "false");
                setProperty("hibernate.use_sql_comments", "false");
            }});
        }};
    }

}
