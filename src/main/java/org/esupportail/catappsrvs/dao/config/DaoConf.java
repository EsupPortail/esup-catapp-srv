package org.esupportail.catappsrvs.dao.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DaoConf {

    private final String[] ENTITIES_PACKAGES = new String[] { "org.esupportail.catappsrvs.model" };

    @Value("${jpa.database.type}")
    private String databaseType;

    @Value("#{systemProperties[generateDdl]?:false}")
    private boolean generateDdl;

    @Value("${hibernate.show_sql}")
    private boolean showSql;

    @Value("${hibernate.format_sql}")
    private boolean formatSql;

    @Value("${hibernate.use_sql_comments}")
    private boolean useSqlComments;

    @Inject DataSource dataSource;

    @Bean
    public BeanPostProcessor beanPostProcessor() {
        return new PersistenceAnnotationBeanPostProcessor();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory());
        return transactionManager;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaProperties(jpaProperties());
        factory.setJpaVendorAdapter(vendorAdapter());
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(ENTITIES_PACKAGES);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }


    private Properties jpaProperties() {
        Properties props = new Properties();
        props.put("hibernate.cache.provider_class",
                "org.hibernate.cache.NoCacheProvider");
        props.put("hibernate.cache.use_query_cache", false);
        props.put("hibernate.cache.use_second_level_cache", false);
        props.put("hibernate.show_sql", showSql);
        props.put("hibernate.format_sql", formatSql);
        props.put("hibernate.use_sql_comments", useSqlComments);
        props.put("hibernate.temp.use_jdbc_metadata_defaults", false);

        props.put("hibernate.hbm2ddl.auto", "create-drop");

        return props;
    }

    private JpaVendorAdapter vendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(generateDdl);
        adapter.setDatabase(Database.valueOf(databaseType));
        return adapter;
    }

    @Profile({"JNDI", "!JDBC"})
    @Configuration
    static class JndiDataSourceConfig {
        @Value("${jndi.datasource}")
        private String jndiDatasourceName;

        @Bean(name = "JNDIDataSource")
        public DataSource jndiDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource(jndiDatasourceName);
        }
    }


    @Profile({"JDBC", "!JNDI"})
    @Configuration
    static class JdbcDataSourceConfig {
        @Value("${jdbc.connection.driver_class}")
        private String databaseDriverClass;

        @Value("${jdbc.connection.url}")
        private String databaseUrl;

        @Value("${jdbc.connection.username}")
        private String databaseUsername;

        @Value("${jdbc.connection.password}")
        private String databasePassword;

        @Bean(name="JDBCDataSource", destroyMethod="close")
        public DataSource jdbcDataSource() {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(databaseDriverClass);
            dataSource.setUrl(databaseUrl);
            dataSource.setUsername(databaseUsername);
            dataSource.setPassword(databasePassword);
            dataSource.setMaxActive(100);
            dataSource.setMaxIdle(30);
            dataSource.setMaxWait(100);
            return dataSource;
        }
    }
}
