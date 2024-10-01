package com.igeeksky.redis;

import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.security.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * Redis Lua 脚本
 *
 * @param <T> 脚本执行返回值类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/13
 */
public class RedisScript<T> {

    /**
     * Lua 脚本内容
     */
    private final byte[] script;

    /**
     * 脚本 SHA1 值
     */
    private String sha1;

    /**
     * 编解码器
     */
    private final Codec<T> codec;

    /**
     * 脚本执行结果类别
     */
    private final ResultType resultType;

    /**
     * 构造函数
     * <p>
     * 默认使用 {@link ResultType#VALUE}，无编解码，脚本执行返回值为 {@code byte[]}
     *
     * @param script Lua 脚本
     */
    public RedisScript(String script) {
        this(script, null, ResultType.VALUE);
    }

    /**
     * 构造函数
     * <p>
     * 默认使用 {@link ResultType#VALUE}，无编解码，脚本执行返回值为 {@code byte[]}
     *
     * @param script Lua 脚本
     * @param codec  编解码器
     */
    public RedisScript(String script, Codec<T> codec) {
        this(script, codec, ResultType.VALUE);
    }

    /**
     * 构造函数
     * <p>
     * 编解码为空
     *
     * @param script     Lua 脚本
     * @param resultType 脚本执行结果类别
     */
    public RedisScript(String script, ResultType resultType) {
        this(script, null, resultType);
    }

    /**
     * 构造函数
     *
     * @param script     Lua 脚本
     * @param codec      编解码器
     * @param resultType 脚本执行结果类别，RedisOperator 根据此参数进行类型转换
     */
    public RedisScript(String script, Codec<T> codec, ResultType resultType) {
        Assert.notNull(script, "script must not be null");
        Assert.notNull(resultType, "resultType must not be null");
        this.script = script.getBytes(StandardCharsets.UTF_8);
        this.sha1 = DigestUtils.sha1(script);
        this.codec = codec;
        this.resultType = resultType;
    }

    /**
     * 获取脚本内容
     *
     * @return {@code byte[]} – 脚本内容
     */
    public byte[] getScript() {
        return script;
    }

    /**
     * 设置脚本 SHA1 值
     *
     * @param sha1 SHA1 值
     */
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    /**
     * 获取脚本 SHA1 值
     *
     * @return {@link String} – SHA1 值
     */
    public String getSha1() {
        return this.sha1;
    }

    /**
     * 获取编解码器
     *
     * @return {@link Codec} – 编解码器
     */
    public Codec<T> getCodec() {
        return codec;
    }

    /**
     * 获取脚本执行返回值类型
     *
     * @return {@link ResultType} – 脚本执行返回值类型
     */
    public ResultType getResultType() {
        return resultType;
    }

}