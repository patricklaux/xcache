package com.igeeksky.xcache.extension.jackson;

import com.igeeksky.xcache.extension.codec.CodecConfig;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
@SuppressWarnings("unchecked")
public class GenericJacksonCodecProvider implements CodecProvider {

    private static final GenericJacksonCodecProvider INSTANCE = new GenericJacksonCodecProvider();

    public static GenericJacksonCodecProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public <K> KeyCodec<K> doGetKeyCodec(CodecConfig<K> config) {
        return (KeyCodec<K>) GenericJacksonKeyCodec.getInstance();
    }

    @Override
    public <V> Codec<V> doGetCodec(CodecConfig<V> config) {
        return (Codec<V>) GenericJacksonCodec.getInstance();
    }

    @Override
    public <T> Codec<Set<T>> getSetCodec(Charset charset, Class<T> type) {
        return JacksonCodecProvider.getInstance().getSetCodec(charset, type);
    }

    @Override
    public <T> Codec<Set<T>> getListCodec(Charset charset, Class<T> type) {
        return JacksonCodecProvider.getInstance().getListCodec(charset, type);
    }

    @Override
    public <K, V> Codec<Map<K, V>> getMapCodec(Charset charset, Class<K> keyType, Class<V> valType) {
        return JacksonCodecProvider.getInstance().getMapCodec(charset, keyType, valType);
    }

}