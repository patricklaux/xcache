package com.igeeksky.xcache.autoconfigure.jackson;

import com.igeeksky.xcache.autoconfigure.CacheManagerConfiguration;
import com.igeeksky.xcache.autoconfigure.holder.KeyConvertorProviderHolder;
import com.igeeksky.xcache.autoconfigure.holder.SerializerProviderHolder;
import com.igeeksky.xcache.extension.jackson.JacksonKeyConvertorProvider;
import com.igeeksky.xcache.extension.jackson.JacksonSerializerProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheManagerConfiguration.class)
public class JacksonAutoConfiguration {

    public static final String JACKSON_CONVERTOR_PROVIDER_ID = "jacksonKeyConvertorProvider";
    public static final String JACKSON_SERIALIZER_PROVIDER_ID = "jacksonSerializerProvider";

    @Bean
    KeyConvertorProviderHolder keyConvertorProviderHolder() {
        KeyConvertorProviderHolder holder = new KeyConvertorProviderHolder();
        holder.put(JACKSON_CONVERTOR_PROVIDER_ID, JacksonKeyConvertorProvider.INSTANCE);
        return holder;
    }

    @Bean
    SerializerProviderHolder serializerProviderHolder() {
        SerializerProviderHolder holder = new SerializerProviderHolder();
        holder.put(JACKSON_SERIALIZER_PROVIDER_ID, JacksonSerializerProvider.INSTANCE);
        return holder;
    }

}