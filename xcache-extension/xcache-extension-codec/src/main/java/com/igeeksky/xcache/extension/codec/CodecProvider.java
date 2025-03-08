package com.igeeksky.xcache.extension.codec;

import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import com.igeeksky.xtool.core.lang.codec.StringKeyCodec;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * 编解码器工厂
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
@SuppressWarnings("unchecked")
public interface CodecProvider {

    /**
     * 根据给定的编解码配置，获取相应的键编解码器
     * 此方法首先判断配置中指定的类型是否为String类型，如果是，则返回默认的 StringKeyCodec 实例，
     * 否则，调用 doGetKeyCodec 方法来处理其他类型的键编解码器的获取
     *
     * @param <K>    缓存键类型
     * @param config 编解码配置
     * @return KeyCodec 的实例，用于处理特定类型的键编解码
     */
    default <K> KeyCodec<K> getKeyCodec(CodecConfig<K> config) {
        // 从配置中提取类型信息
        Class<K> type = config.getType();
        // 检查类型是否为 String，以决定是否返回处理 String 类型的特殊编解码器
        if (String.class.equals(type)) {
            // 返回处理String类型的键编解码器的默认实例
            return (KeyCodec<K>) StringKeyCodec.getInstance();
        }
        // 调用私有方法处理非String类型的键编解码器获取
        return doGetKeyCodec(config);
    }

    /**
     * 根据给定的编解码配置，获取相应的键编解码器
     *
     * @param <K>    缓存键类型
     * @param config 编解码配置
     * @return KeyCodec 的实例，用于处理特定类型的键编解码
     */
    <K> KeyCodec<K> doGetKeyCodec(CodecConfig<K> config);

    /**
     * 根据编解码配置获取相应的编解码器实例
     * <p>
     * 此方法首先判断配置中指定的类型是否为 String 类型，如果是，则返回默认的 StringCodec 实例，
     * 否则，调用 doGetKeyCodec 方法来处理其他类型的编解码器的获取
     *
     * @param <V>    需要编解码的数据类型
     * @param config 编解码配置，包含编解码所需的信息，如类型和配置参数
     * @return Codec 的实例，用于处理特定类型的编解码
     */
    default <V> Codec<V> getCodec(CodecConfig<V> config) {
        // 获取编解码配置中指定的类型
        Class<V> type = config.getType();
        // 如果指定的类型是 String，则创建并返回 StringCodec 实例
        if (String.class.equals(type)) {
            return (Codec<V>) StringCodec.getInstance(config.getCharset());
        }
        // 对于其他类型，交由doGetCodec方法处理
        return doGetCodec(config);
    }

    /**
     * 根据编解码配置获取相应的编解码器实例
     *
     * @param <V>    需要编解码的数据类型
     * @param config 编解码配置，包含编解码所需的信息，如类型和配置参数
     * @return Codec 的实例，用于处理特定类型的编解码
     */
    <V> Codec<V> doGetCodec(CodecConfig<V> config);

    /**
     * 获取集合编解码器
     *
     * @param charset 字符集
     * @param type    集合元素类型
     * @param <T>     集合元素类型
     * @return 集合编解码器
     */
    <T> Codec<Set<T>> getSetCodec(Charset charset, Class<T> type);

    /**
     * 获取列表编解码器
     *
     * @param charset 字符集
     * @param type    列表元素类型
     * @param <T>     列表元素类型
     * @return 列表编解码器
     */
    <T> Codec<Set<T>> getListCodec(Charset charset, Class<T> type);

    /**
     * 获取映射编解码器
     *
     * @param charset 字符集
     * @param keyType 映射键类型
     * @param valType 映射值类型
     * @param <K>     映射键类型
     * @param <V>     映射值类型
     * @return 映射编解码器
     */
    <K, V> Codec<Map<K, V>> getMapCodec(Charset charset, Class<K> keyType, Class<V> valType);

}