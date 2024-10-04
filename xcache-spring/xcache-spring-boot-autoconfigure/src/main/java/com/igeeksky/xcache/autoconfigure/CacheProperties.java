package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.props.CacheProps;
import com.igeeksky.xcache.props.StatProps;
import com.igeeksky.xcache.props.Template;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.StringJoiner;

/**
 * Xcache 配置项
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
@Configuration
@ConfigurationProperties(prefix = "xcache")
public class CacheProperties {

    /**
     * 分组名称（必填）
     */
    private String group;

    /**
     * 模板配置（必填）
     * <p>
     * 列表类型，可配置多个模板.
     */
    private List<Template> template;

    /**
     * 缓存配置列表
     * <p>
     * caches 会从 templates 获取指定 template-id 的模板并复制其中的全部选项，因此仅需配置缓存名称及与指定模板的差异项.
     * <p>
     * 如使用 t0 模板且无差异项，则无需配置. <p>
     * 如使用其它模板，但与模板无差异，则仅需配置缓存名称和模板ID.
     */
    private List<CacheProps> cache;

    /**
     * 日志方式缓存指标统计配置
     */
    private StatProps stat;

    /**
     * 默认构造函数
     */
    public CacheProperties() {
    }

    /**
     * 分组名称（必填）
     *
     * @return {@link String} – 分组名称
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置分组名称
     *
     * @param group 分组名称
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 获取模板配置列表
     *
     * @return {@link List<Template>} – 模板配置列表
     */
    public List<Template> getTemplate() {
        return template;
    }

    /**
     * 设置模板配置列表
     * <p>
     * 列表类型，可配置多个模板
     *
     * @param template 模板配置列表
     */
    public void setTemplate(List<Template> template) {
        this.template = template;
    }

    /**
     * 获取缓存配置列表
     *
     * @return {@link List<CacheProps>} – 缓存配置列表
     */
    public List<CacheProps> getCache() {
        return cache;
    }

    /**
     * 设置缓存配置列表
     * <p>
     * 如使用 t0 模板且无差异项，则无需配置. <p>
     * 如使用其它模板，但与模板无差异，则仅需配置缓存名称和模板ID.
     *
     * @param cache 缓存配置列表
     */
    public void setCache(List<CacheProps> cache) {
        this.cache = cache;
    }

    /**
     * 获取日志方式缓存指标统计配置
     *
     * @return {@link StatProps} – 日志方式缓存指标统计配置
     */
    public StatProps getStat() {
        return stat;
    }

    /**
     * 设置日志方式缓存指标统计配置
     *
     * @param stat 日志方式缓存指标统计配置
     */
    public void setStat(StatProps stat) {
        this.stat = stat;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        joiner.add("\"group\":\"" + group + "\"");
        if (template != null) {
            joiner.add("\"template\":" + template);
        }
        if (cache != null) {
            joiner.add("\"cache\":" + cache);
        }
        if (stat != null) {
            joiner.add("\"stat\":" + stat);
        }
        return joiner.toString();
    }

}