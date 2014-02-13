package org.esupportail.catappsrvs.web.run;

import org.esupportail.catappsrvs.web.config.AppConf;
import org.esupportail.catappsrvs.web.config.PropsConf;
import org.springframework.beans.BeansException;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.util.Log4jConfigListener;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

public final class WebApp implements WebApplicationInitializer {
    @Override
    public void onStartup(javax.servlet.ServletContext context) {
        try {
            final AnnotationConfigWebApplicationContext root = new AnnotationConfigWebApplicationContext();
            root.register(PropsConf.class);
            root.refresh();
            final PropertySourcesPlaceholderConfigurer props =
                    root.getBean("propertyConfigurer", PropertySourcesPlaceholderConfigurer.class);
            final String dataSourceBeanName =
                    (String) props.getAppliedPropertySources().get("localProperties").getProperty("datasource.bean");
            root.getEnvironment().setActiveProfiles(dataSourceBeanName);
            root.register(AppConf.class);
            root.refresh();

            // ######### Listeners
            context.addListener(new ContextLoaderListener(root));
            context.addListener(new RequestContextListener());
            context.addListener(new Log4jConfigListener());

            // ######### Servlets
            final ServletRegistration.Dynamic restServlet =
                    context.addServlet("catapp", "org.glassfish.jersey.servlet.ServletContainer");
            restServlet.setInitParameter("javax.ws.rs.Application", "org.esupportail.catappsrvs.web.run.RestService");
            restServlet.setLoadOnStartup(1);
            restServlet.addMapping("/*");

        } catch (Throwable e) { // généralement, on attrape jamais Throwable, mais là nous sommes au démarrage...
            e.printStackTrace();
            System.exit(1); // ... si un problème survient à ce moment, on arrête tout (jvm comprise)
        }
    }

}
