package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/9
 */
public class CompressProps {

    /**
     * 压缩级别
     * <p>
     * 默认值： -1，可选值：[-1,9]
     * <p>
     * -1 系统默认压缩比；0 不压缩； 1 最快速度；9 最高压缩比
     * <p>
     * 注意：<p>
     * 此配置对于 DeflaterCompressor 有效，对于 GzipCompressor 无效。
     * <p>
     */
    private Integer level;

    /**
     * 默认值： false
     * <p>
     * 如果为 true，则无 ZLIB header 和 checksum 字段。 <p>
     * <p>
     * 注意：<p>
     * 此配置对于 DeflaterCompressor 有效，对于 GzipCompressor 无效。
     */
    private Boolean nowrap;

    /**
     * CompressorProvider - id
     * <p>
     * 默认值： deflater
     */
    private String provider;

    /**
     * 扩展参数
     */
    private final Map<String, Object> params = new HashMap<>();

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Boolean getNowrap() {
        return nowrap;
    }

    public void setNowrap(Boolean nowrap) {
        this.nowrap = nowrap;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params.putAll(params);
        }
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}