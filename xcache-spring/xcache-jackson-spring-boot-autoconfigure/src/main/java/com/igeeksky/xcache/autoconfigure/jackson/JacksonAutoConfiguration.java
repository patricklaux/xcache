package com.igeeksky.xcache.autoconfigure.jackson;

import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.holder.CodecProviderHolder;
import com.igeeksky.xcache.extension.jackson.GenericJacksonCodecProvider;
import com.igeeksky.xcache.extension.jackson.JacksonCodecProvider;
import com.igeeksky.xcache.props.CacheConstants;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class JacksonAutoConfiguration {

    public static final String JACKSON_CODEC_PROVIDER_ID = CacheConstants.JACKSON_CODEC;
    public static final String JACKSON_SPRING_CODEC_PROVIDER_ID = CacheConstants.JACKSON_SPRING_CODEC;

    @Bean
    CodecProviderHolder serializerProviderHolder() {
        CodecProviderHolder holder = new CodecProviderHolder();
        holder.put(JACKSON_CODEC_PROVIDER_ID, JacksonCodecProvider.getInstance());
        holder.put(JACKSON_SPRING_CODEC_PROVIDER_ID, GenericJacksonCodecProvider.getInstance());
        return holder;
    }

}