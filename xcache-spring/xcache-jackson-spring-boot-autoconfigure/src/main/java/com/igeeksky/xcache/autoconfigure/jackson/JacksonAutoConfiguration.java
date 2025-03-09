package com.igeeksky.xcache.autoconfigure.jackson;

import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.register.CodecProviderRegister;
import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.extension.jackson.GenericJacksonCodecProvider;
import com.igeeksky.xcache.extension.jackson.JacksonCodecProvider;
import com.igeeksky.xcache.props.CacheConstants;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Jackson 自动配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheAutoConfiguration.class)
@SuppressWarnings("unused")
public class JacksonAutoConfiguration {

    /**
     * 注册 Jackson CodecProvider（ID: jackson, jackson-spring）
     *
     * @return {@link CodecProviderRegister} – CodecProvider 注册器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    CodecProviderRegister jacksonCodecProviderRegister() {
        CodecProviderRegister register = new CodecProviderRegister();
        register.put(CacheConstants.JACKSON_CODEC, SingletonSupplier.of(JacksonCodecProvider::getInstance));
        register.put(CacheConstants.JACKSON_SPRING_CODEC, SingletonSupplier.of(GenericJacksonCodecProvider::getInstance));
        return register;
    }

}