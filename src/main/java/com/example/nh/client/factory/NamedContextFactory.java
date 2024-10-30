//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.nh.client.factory;

import com.example.nh.client.factory.NamedContextFactory.Specification;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.aot.AotDetector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.Assert;

public abstract class NamedContextFactory<C extends Specification> implements DisposableBean, ApplicationContextAware {
    private final Map<String, ApplicationContextInitializer<GenericApplicationContext>> applicationContextInitializers;
    private final String propertySourceName;
    private final String propertyName;
    private final Map<String, GenericApplicationContext> contexts;
    private final Map<String, C> configurations;
    private ApplicationContext parent;
    private final Class<?> defaultConfigType;

    public NamedContextFactory(Class<?> defaultConfigType, String propertySourceName, String propertyName) {
        this(defaultConfigType, propertySourceName, propertyName, new HashMap());
    }

    public NamedContextFactory(Class<?> defaultConfigType, String propertySourceName, String propertyName, Map<String, ApplicationContextInitializer<GenericApplicationContext>> applicationContextInitializers) {
        this.contexts = new ConcurrentHashMap();
        this.configurations = new ConcurrentHashMap();
        this.defaultConfigType = defaultConfigType;
        this.propertySourceName = propertySourceName;
        this.propertyName = propertyName;
        this.applicationContextInitializers = applicationContextInitializers;
    }

    public void setApplicationContext(ApplicationContext parent) throws BeansException {
        this.parent = parent;
    }

    public void setConfigurations(List<C> configurations) {
        Iterator var2 = configurations.iterator();

        while(var2.hasNext()) {
            C client = (C) var2.next();
            this.configurations.put(client.getName(), client);
        }

    }

    public void destroy() {
        Collection<GenericApplicationContext> values = this.contexts.values();
        Iterator var2 = values.iterator();

        while(var2.hasNext()) {
            GenericApplicationContext context = (GenericApplicationContext)var2.next();
            context.close();
        }

        this.contexts.clear();
    }

    protected GenericApplicationContext getContext(String name) {
        if (!this.contexts.containsKey(name)) {
            synchronized(this.contexts) {
                if (!this.contexts.containsKey(name)) {
                    this.contexts.put(name, this.createContext(name));
                }
            }
        }

        return (GenericApplicationContext)this.contexts.get(name);
    }

    protected GenericApplicationContext createContext(String name) {
        GenericApplicationContext context = this.buildContext(name);
        if (this.applicationContextInitializers.get(name) != null) {
            ((ApplicationContextInitializer)this.applicationContextInitializers.get(name)).initialize(context);
            context.refresh();
            return context;
        } else {
            this.registerBeans(name, context);
            context.refresh();
            return context;
        }
    }

    public void registerBeans(String name, GenericApplicationContext context) {
        Assert.isInstanceOf(AnnotationConfigRegistry.class, context);
        AnnotationConfigRegistry registry = (AnnotationConfigRegistry)context;
        if (this.configurations.containsKey(name)) {
            Class[] var4 = ((Specification)this.configurations.get(name)).getConfiguration();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Class<?> configuration = var4[var6];
                registry.register(new Class[]{configuration});
            }
        }

        Iterator var10 = this.configurations.entrySet().iterator();

        while(true) {
            Map.Entry entry;
            do {
                if (!var10.hasNext()) {
                    registry.register(new Class[]{PropertyPlaceholderAutoConfiguration.class, this.defaultConfigType});
                    return;
                }

                entry = (Map.Entry)var10.next();
            } while(!((String)entry.getKey()).startsWith("default."));

            Class[] var12 = ((Specification)entry.getValue()).getConfiguration();
            int var13 = var12.length;

            for(int var8 = 0; var8 < var13; ++var8) {
                Class<?> configuration = var12[var8];
                registry.register(new Class[]{configuration});
            }
        }
    }

    public GenericApplicationContext buildContext(String name) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Object context;
        if (this.parent != null) {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            if (this.parent instanceof ConfigurableApplicationContext) {
                beanFactory.setBeanClassLoader(((ConfigurableApplicationContext)this.parent).getBeanFactory().getBeanClassLoader());
            } else {
                beanFactory.setBeanClassLoader(classLoader);
            }

            context = AotDetector.useGeneratedArtifacts() ? new GenericApplicationContext(beanFactory) : new AnnotationConfigApplicationContext(beanFactory);
        } else {
            context = AotDetector.useGeneratedArtifacts() ? new GenericApplicationContext() : new AnnotationConfigApplicationContext();
        }

        ((GenericApplicationContext)context).setClassLoader(classLoader);
        ((GenericApplicationContext)context).getEnvironment().getPropertySources().addFirst(new MapPropertySource(this.propertySourceName, Collections.singletonMap(this.propertyName, name)));
        if (this.parent != null) {
            ((GenericApplicationContext)context).setParent(this.parent);
        }

        ((GenericApplicationContext)context).setDisplayName(this.generateDisplayName(name));
        return (GenericApplicationContext)context;
    }

    protected String generateDisplayName(String name) {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + "-" + name;
    }

    public <T> T getInstance(String name, Class<T> type) {
        GenericApplicationContext context = this.getContext(name);

        try {
            return context.getBean(type);
        } catch (NoSuchBeanDefinitionException var5) {
            return null;
        }
    }

    public <T> T getInstance(String name, ResolvableType type) {
        GenericApplicationContext context = this.getContext(name);
        String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, type);
        String[] var5 = beanNames;
        int var6 = beanNames.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String beanName = var5[var7];
            if (context.isTypeMatch(beanName, type)) {
                return (T) context.getBean(beanName);
            }
        }

        return null;
    }

    public interface Specification {
        String getName();

        Class<?>[] getConfiguration();
    }
}
