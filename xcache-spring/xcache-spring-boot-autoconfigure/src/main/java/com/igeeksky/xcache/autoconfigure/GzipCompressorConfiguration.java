package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.autoconfigure.holder.CompressorProviderHolder;
import com.igeeksky.xcache.extension.compress.GzipCompressorProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-09
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheManagerConfiguration.class)
class GzipCompressorConfiguration {

    public static final String GZIP_COMPRESSOR_PROVIDER_ID = "gzipCompressorProvider";

    @Bean
    CompressorProviderHolder compressorProviderHolder() {
        CompressorProviderHolder holder = new CompressorProviderHolder();
        holder.put(GZIP_COMPRESSOR_PROVIDER_ID, GzipCompressorProvider.INSTANCE);
        return holder;
    }

}
