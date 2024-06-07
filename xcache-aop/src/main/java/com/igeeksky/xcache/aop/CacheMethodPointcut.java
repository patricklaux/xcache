package com.igeeksky.xcache.aop;

import com.igeeksky.xtool.core.collection.CollectionUtils;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
public class CacheMethodPointcut extends StaticMethodMatcherPointcut {

    private final CacheOperationSource source;

    public CacheMethodPointcut(String[] basePackages, CacheOperationSource source) {
        this.source = source;
        this.setClassFilter(new CacheOperationSourceClassFilter(basePackages));
    }

    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        return (source != null && CollectionUtils.isNotEmpty(source.getCacheOperations(method, targetClass)));
    }

    /**
     * 判断目标类是否属于指定的包：如果是，则可能是缓存代理对象；如果否，则非缓存代理对象<p/>
     * 即只有 @EnableCache 的 basePackages 指定的包内的缓存注解才生效。
     */
    private static class CacheOperationSourceClassFilter implements ClassFilter {

        private final String[] basePackages;

        public CacheOperationSourceClassFilter(String[] basePackages) {
            this.basePackages = basePackages;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            String name = clazz.getName();
            // exclude
            if (name.startsWith("java") || name.startsWith("org.springframework")) {
                return false;
            }
            if (name.contains("$$EnhancerBySpringCGLIB$$") || name.contains("$$FastClassBySpringCGLIB$$")) {
                return false;
            }
            // include
            for (String pkg : basePackages) {
                if (name.startsWith(pkg)) {
                    return true;
                }
            }
            return false;
        }
    }

}