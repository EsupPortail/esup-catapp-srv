package org.esupportail.catappsrvs.web.config.jerseyspring;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.jvnet.hk2.spring.bridge.api.SpringBridge;
import org.jvnet.hk2.spring.bridge.api.SpringIntoHK2Bridge;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom ComponentProvider class.
 *
 * Responsible for 1) bootstrapping Jersey 2 Spring integration and
 * 2) making Jersey skip JAX-RS Spring component life-cycle management and leave it to us.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 * @see <a href="https://github.com/jersey/jersey/blob/master/ext/spring3/src/main/java/org/glassfish/jersey/server/spring/SpringWebApplicationInitializer.java">here</a>
 */
public class SpringComponentProvider implements ComponentProvider {

    private static final Logger LOGGER = Logger.getLogger(SpringComponentProvider.class.getName());
    private static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "applicationContext.xml";
    private static final String PARAM_CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private volatile ServiceLocator locator;
    private volatile ApplicationContext ctx;

    @Override
    public void initialize(ServiceLocator locator) {
        this.locator = locator;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("CTX_LOOKUP_STARTED");
        }

        ServletContext sc = locator.getService(ServletContext.class);

        if(sc != null) {
            // servlet container
            ctx = WebApplicationContextUtils.getWebApplicationContext(sc);
        } else {
            // non-servlet container
            ApplicationHandler applicationHandler = locator.getService(ApplicationHandler.class);
            String contextConfigLocation = (String)applicationHandler.getConfiguration().getProperty(PARAM_CONTEXT_CONFIG_LOCATION);
            if(contextConfigLocation == null) {
                contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;
            }
            ctx = new ClassPathXmlApplicationContext(contextConfigLocation, "jersey-spring-applicationContext.xml");
        }
        if(ctx == null) {
            LOGGER.severe("CTX_LOOKUP_FAILED");
            return;
        }
        LOGGER.config("CTX_LOOKUP_SUCESSFUL");

        // initialize HK2 spring-bridge
        SpringBridge.getSpringBridge().initializeSpringBridge(locator);
        SpringIntoHK2Bridge springBridge = locator.getService(SpringIntoHK2Bridge.class);
        springBridge.bridgeSpringBeanFactory(ctx);

        // register Spring @Autowired annotation handler with HK2 ServiceLocator
        //ServiceLocatorUtilities.addOneConstant(locator, new AutowiredInjectResolver(ctx));
        ServiceLocatorUtilities.addOneConstant(locator, ctx, "SpringContext", ApplicationContext.class);
        LOGGER.config("SPRING_COMPONENT_PROVIDER_INITIALIZED");
    }

    // detect JAX-RS classes that are also Spring @Components.
    // register these with HK2 ServiceLocator to manage their lifecycle using Spring.
    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {

        if (ctx == null) {
            return false;
        }

        if(component.isAnnotationPresent(Component.class)) {
            DynamicConfiguration c = Injections.getConfiguration(locator);
            String[] beanNames = ctx.getBeanNamesForType(component);
            if(beanNames == null || beanNames.length != 1) {
                LOGGER.severe("NONE_OR_MULTIPLE_BEANS_AVAILABLE(component)");
                return false;
            }
            String beanName = beanNames[0];

            ServiceBindingBuilder<?> bb =
                Injections.newFactoryBinder(new SpringManagedBeanFactory(ctx, locator, beanName));
            bb.to(component);
            Injections.addBinding(bb, c);
            c.commit();

            LOGGER.config("LocalizationMessages.BEAN_REGISTERED(beanName)");
            return true;
        }
        return false;
    }

    @Override
    public void done() {
    }


    private static class SpringManagedBeanFactory implements Factory<Object> {
        private final ApplicationContext ctx;
        private final ServiceLocator locator;
        private final String beanName;

        private SpringManagedBeanFactory(ApplicationContext ctx, ServiceLocator locator, String beanName) {
            this.ctx = ctx;
            this.locator = locator;
            this.beanName = beanName;
        }

        @Override
        public Object provide() {
            Object bean = ctx.getBean(beanName);
            locator.inject(bean);
            return bean;
        }

        @Override
        public void dispose(Object instance) {
        }
    }
}

