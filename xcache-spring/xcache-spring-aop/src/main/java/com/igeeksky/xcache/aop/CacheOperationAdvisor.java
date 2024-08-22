package com.igeeksky.xcache.aop;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheOperationAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    public static final String CACHE_ADVISOR_BEAN_NAME = "com.igeeksky.xcache.aop.CacheOperationAdvisor";

    private String[] basePackages;

    private final CacheOperationSource source;

    public CacheOperationAdvisor(CacheOperationSource source, CacheInterceptor advice) {
        this.source = source;
        this.setAdvice(advice);
        advice.setCacheOperationSource(source);
    }

    @Override
    public Pointcut getPointcut() {
        return new CacheMethodPointcut(basePackages, source);
    }

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }

}
