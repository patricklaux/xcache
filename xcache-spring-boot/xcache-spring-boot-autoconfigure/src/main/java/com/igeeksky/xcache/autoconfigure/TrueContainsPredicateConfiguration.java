package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.autoconfigure.holder.ContainsPredicateProviderHolder;
import com.igeeksky.xcache.extension.contains.TrueContainsPredicateProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-09
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheManagerConfiguration.class)
class TrueContainsPredicateConfiguration {

    public static final String TRUE_CONTAINS_PREDICATE_PROVIDER_ID = "alwaysTruePredicateProvider";

    @Bean
    ContainsPredicateProviderHolder containsPredicateProviderHolder() {
        ContainsPredicateProviderHolder holder = new ContainsPredicateProviderHolder();
        holder.put(TRUE_CONTAINS_PREDICATE_PROVIDER_ID, TrueContainsPredicateProvider.INSTANCE);
        return holder;
    }

}
