package com.vctek.orderservice.promotionengine.ruleengine.infrastructure;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;

@Component
public class PostRuleEngineInitializeBeanPostProcessor implements BeanPostProcessor, Ordered {

    private ConfigurableListableBeanFactory beanFactory;
    private Map<String, Class> ruleGlobalsAwareBeans = Maps.newHashMap();
    private Map<String, Method> ruleGlobalsRetrievalMethods = Maps.newHashMap();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();
        Method[] var8 = methods;
        int var7 = methods.length;

        for (int var6 = 0; var6 < var7; ++var6) {
            Method method = var8[var6];
            if (method.isAnnotationPresent(GetRuleEngineGlobalByName.class)) {
                this.ruleGlobalsAwareBeans.put(beanName, beanClass);
                this.ruleGlobalsRetrievalMethods.put(beanName, method);
                break;
            }
        }

        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class origBeanClass = this.ruleGlobalsAwareBeans.get(beanName);
        if (Objects.nonNull(origBeanClass)) {
            Method ruleGlobalsRetrievalMethod = this.ruleGlobalsRetrievalMethods.get(beanName);
            return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    ClassUtils.getAllInterfacesForClass(origBeanClass), (proxy, method1, args)
                            -> this.getRuleGlobalBean(bean, method1, ruleGlobalsRetrievalMethod.getName(), args));
        }

        return bean;
    }

    private Object getRuleGlobalBean(Object bean, Method proxyMethod, String ruleGlobalsGetMethodName, Object... args) {
        return proxyMethod.getName().equals(ruleGlobalsGetMethodName) && this.hasOneStringArg(args) ? this.beanFactory.getBean((String) args[0]) : ReflectionUtils.invokeMethod(proxyMethod, bean, args);
    }

    protected boolean hasOneStringArg(Object... args) {
        return ArrayUtils.isNotEmpty(args) && args.length == 1 && args[0] instanceof String;
    }

    public int getOrder() {
        return 2147483647;
    }

    @Autowired
    public void setBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public Map<String, Class> getRuleGlobalsAwareBeans() {
        return ruleGlobalsAwareBeans;
    }

    public Map<String, Method> getRuleGlobalsRetrievalMethods() {
        return ruleGlobalsRetrievalMethods;
    }
}
