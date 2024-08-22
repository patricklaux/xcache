package com.igeeksky.xcache.extension.codec;

import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import com.igeeksky.xtool.core.lang.codec.StringKeyCodec;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
@SuppressWarnings("unchecked")
public interface CodecProvider {

    /**
     * @param config 编解码配置
     * @return 编解码器（String ---- type）
     */
    default <K> KeyCodec<K> getKeyCodec(CodecConfig<K> config) {
        Class<K> type = config.getType();
        if (String.class.equals(type)) {
            return (KeyCodec<K>) StringKeyCodec.getInstance();
        }
        return doGetKeyCodec(config);
    }

    <K> KeyCodec<K> doGetKeyCodec(CodecConfig<K> config);

    /**
     * @param config 编解码配置
     * @return 编解码器（String ---- type）
     */
    default <V> Codec<V> getCodec(CodecConfig<V> config) {
        Class<V> type = config.getType();
        if (String.class.equals(type)) {
            return (Codec<V>) StringCodec.getInstance(config.getCharset());
        }
        return doGetCodec(config);
    }

    <V> Codec<V> doGetCodec(CodecConfig<V> config);

    <T> Codec<Set<T>> getSetCodec(Charset charset, Class<T> type);

    <T> Codec<Set<T>> getListCodec(Charset charset, Class<T> type);

    <K, V> Codec<Map<K, V>> getMapCodec(Charset charset, Class<K> keyType, Class<V> valType);
}