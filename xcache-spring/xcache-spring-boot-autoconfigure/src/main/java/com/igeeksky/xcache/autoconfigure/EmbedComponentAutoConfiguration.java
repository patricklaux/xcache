package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.autoconfigure.holder.CacheLockProviderHolder;
import com.igeeksky.xcache.autoconfigure.holder.CodecProviderHolder;
import com.igeeksky.xcache.autoconfigure.holder.CompressorProviderHolder;
import com.igeeksky.xcache.autoconfigure.holder.ContainsPredicateProviderHolder;
import com.igeeksky.xcache.extension.codec.JdkCodecProvider;
import com.igeeksky.xcache.extension.compress.DeflaterCompressorProvider;
import com.igeeksky.xcache.extension.compress.GzipCompressorProvider;
import com.igeeksky.xcache.extension.contains.EmbedContainsPredicateProvider;
import com.igeeksky.xcache.extension.lock.EmbedCacheLockProvider;
import com.igeeksky.xcache.props.CacheConstants;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 内嵌基础组件自动配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/8/31
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({CacheAutoConfiguration.class})
public class EmbedComponentAutoConfiguration {

    /**
     * deflate 和 gzip 压缩器注册
     *
     * @return CompressorProviderHolder
     */
    @Bean
    CompressorProviderHolder embedCompressorHolder() {
        CompressorProviderHolder holder = new CompressorProviderHolder();
        holder.put(CacheConstants.DEFLATER_COMPRESSOR, DeflaterCompressorProvider.getInstance());
        holder.put(CacheConstants.GZIP_COMPRESSOR, GzipCompressorProvider.getInstance());
        return holder;
    }

    /**
     * 内嵌缓存锁注册
     *
     * @return CacheLockProviderHolder
     */
    @Bean
    CacheLockProviderHolder embedCacheLockHolder() {
        CacheLockProviderHolder holder = new CacheLockProviderHolder();
        holder.put(CacheConstants.EMBED_CACHE_LOCK, EmbedCacheLockProvider.getInstance());
        return holder;
    }

    /**
     * 内嵌缓存包含判断注册
     *
     * @return ContainsPredicateProviderHolder
     */
    @Bean
    ContainsPredicateProviderHolder embedContainsPredicateHolder() {
        ContainsPredicateProviderHolder holder = new ContainsPredicateProviderHolder();
        holder.put(CacheConstants.EMBED_CONTAINS_PREDICATE, EmbedContainsPredicateProvider.getInstance());
        return holder;
    }

    /**
     * JDK 序列化注册
     *
     * @return CodecProviderHolder
     */
    @Bean
    CodecProviderHolder jdkCodecHolder() {
        CodecProviderHolder holder = new CodecProviderHolder();
        holder.put(CacheConstants.JDK_CODEC, JdkCodecProvider.getInstance());
        return holder;
    }

}
