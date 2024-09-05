package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.autoconfigure.holder.CodecProviderHolder;
import com.igeeksky.xcache.extension.jackson.GenericJacksonCodecProvider;
import com.igeeksky.xcache.props.CacheConstants;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 适配 Spring CacheManager
 * <p>
 * 如希望使用 Spring 缓存注解，则需要配置此 Bean
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/5
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheAutoConfiguration.class)
@ConditionalOnClass(name = {"com.igeeksky.xcache.extension.jackson.GenericJacksonCodecProvider"})
public class GenericJacksonCodecAutoConfiguration {

    public static final String JACKSON_SPRING_CODEC_PROVIDER_ID = CacheConstants.JACKSON_SPRING_CODEC;

    @Bean
    CodecProviderHolder genericJacksonCodecProviderHolder() {
        CodecProviderHolder holder = new CodecProviderHolder();
        holder.put(JACKSON_SPRING_CODEC_PROVIDER_ID, GenericJacksonCodecProvider.getInstance());
        return holder;
    }

}