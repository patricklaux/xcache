package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.props.CacheProps;
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
     * 应用名称（必填）
     */
    private String app;

    /**
     * 模板配置（必填）
     * <p>
     * 列表类型，可配置多个模板.
     */
    private List<Template> templates;

    /**
     * 缓存配置列表（可为空）
     * <p>
     * caches 会从 templates 获取指定 template-id 的模板并复制其中的全部选项，因此仅需配置缓存名称及与指定模板的差异项.
     * <p>
     * 如使用 t0 模板且无差异项，则无需配置. <p>
     * 如使用其它模板，但与模板无差异，则仅需配置缓存名称和模板ID.
     */
    private List<CacheProps> caches;

    /**
     * 默认构造函数
     */
    public CacheProperties() {
    }

    /**
     * 应用名称（必填）
     *
     * @return String – 应用名称
     */
    public String getApp() {
        return app;
    }

    /**
     * 设置应用名称
     *
     * @param app 应用名称
     */
    public void setApp(String app) {
        this.app = app;
    }

    /**
     * 获取模板配置列表
     *
     * @return {@code List<Template>} – 模板配置列表
     */
    public List<Template> getTemplates() {
        return templates;
    }

    /**
     * 设置模板配置列表
     * <p>
     * 列表类型，可配置多个模板
     *
     * @param templates 模板配置列表
     */
    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    /**
     * 获取缓存配置列表
     *
     * @return {@code List<CacheProps>} – 缓存配置列表
     */
    public List<CacheProps> getCaches() {
        return caches;
    }

    /**
     * 设置缓存配置列表
     * <p>
     * 如使用 t0 模板且无差异项，则无需配置. <p>
     * 如使用其它模板，但与模板无差异，则仅需配置缓存名称和模板ID.
     *
     * @param caches 缓存配置列表
     */
    public void setCaches(List<CacheProps> caches) {
        this.caches = caches;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        joiner.add("\"application\":\"" + app + "\"");
        if (templates != null) {
            joiner.add("\"templates\":" + templates);
        }
        if (caches != null) {
            joiner.add("\"caches\":" + caches);
        }
        return joiner.toString();
    }

}