package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 压缩器配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/9
 */
public class CompressProps {

    private Integer level;

    private Boolean nowrap;

    private String provider;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * 压缩级别
     * <p>
     * 默认值： -1，可选值：[-1,9]
     * <p>
     * -1 系统默认压缩比；0 不压缩； 1 最快速度；9 最高压缩比
     * <p>
     * 注意：<p>
     * 此配置对于 DeflaterCompressor 有效，对于 GzipCompressor 无效。
     *
     * @return {@code Integer} - 压缩级别
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * 设置：压缩级别
     *
     * @param level 压缩级别
     */
    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * 是否不携带封装信息
     * <p>
     * 默认值： false
     * <p>
     * 如果为 true，则无 ZLIB header 和 checksum 字段。
     * <p>
     * 注意：<br>
     * 此配置对于 DeflaterCompressor 有效，对于 GzipCompressor 无效。
     *
     * @return {@code Boolean} - 是否不携带封装信息
     */
    public Boolean getNowrap() {
        return nowrap;
    }

    /**
     * 设置：是否不携带完整封装信息
     *
     * @param nowrap 是否不携带封装信息
     */
    public void setNowrap(Boolean nowrap) {
        this.nowrap = nowrap;
    }

    /**
     * CompressorProviderId
     * <p>
     * 默认值：none <br>
     * {@link CacheConstants#DEFAULT_VALUE_COMPRESSOR}
     * <p>
     * 可选值： deflate，gzip，none
     *
     * @return {@code String} - CompressorProviderId
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置：CompressorProviderId
     *
     * @param provider CompressorProviderId
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 扩展参数
     * <p>
     * 自定义扩展实现时，如需用到额外的未定义参数，可在此配置。
     * <p>
     * 如使用 xcache 内置实现，则无需此配置。<br>
     * 如不使用，请删除，否则会导致 SpringBoot 读取配置错误而启动失败。
     *
     * @return {@code Map<String, Object>} - 扩展参数
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 设置：扩展参数
     *
     * @param params 扩展参数
     */
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