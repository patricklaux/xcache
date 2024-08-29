package com.igeeksky.xcache.extension.codec;

import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.JdkCodec;
import com.igeeksky.xtool.core.lang.codec.JdkKeyCodec;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * JDK编解码实现类提供者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class JdkCodecProvider implements CodecProvider {

    private static final JdkCodecProvider INSTANCE = new JdkCodecProvider();

    public static JdkCodecProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public <K> KeyCodec<K> doGetKeyCodec(CodecConfig<K> config) {
        return JdkKeyCodec.getInstance(config.getCharset());
    }

    @Override
    public <V> Codec<V> doGetCodec(CodecConfig<V> config) {
        return JdkCodec.getInstance();
    }

    @Override
    public <T> Codec<Set<T>> getSetCodec(Charset charset, Class<T> type) {
        return JdkCodec.getInstance();
    }

    @Override
    public <T> Codec<Set<T>> getListCodec(Charset charset, Class<T> type) {
        return JdkCodec.getInstance();
    }

    @Override
    public <K, V> Codec<Map<K, V>> getMapCodec(Charset charset, Class<K> keyType, Class<V> valType) {
        return JdkCodec.getInstance();
    }

}