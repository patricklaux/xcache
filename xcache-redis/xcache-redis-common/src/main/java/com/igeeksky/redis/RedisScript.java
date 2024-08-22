package com.igeeksky.redis;

import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.security.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/13
 */
public class RedisScript<T> {

    private final byte[] script;

    private String sha1;

    private final Codec<T> codec;

    private final ResultType resultType;

    public RedisScript(String script) {
        this(script, null, ResultType.VALUE);
    }

    public RedisScript(String script, Codec<T> codec) {
        this(script, codec, ResultType.VALUE);
    }

    public RedisScript(String script, ResultType resultType) {
        this(script, null, resultType);
    }

    public RedisScript(String script, Codec<T> codec, ResultType resultType) {
        Assert.notNull(script, "script must not be null");
        Assert.notNull(resultType, "resultType must not be null");
        this.script = script.getBytes(StandardCharsets.UTF_8);
        this.sha1 = DigestUtils.sha1(script);
        this.codec = codec;
        this.resultType = resultType;
    }

    public byte[] getScript() {
        return script;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha1() {
        return this.sha1;
    }

    public Codec<T> getCodec() {
        return codec;
    }

    public ResultType getResultType() {
        return resultType;
    }

}