package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.props.CacheProps;
import com.igeeksky.xcache.props.Template;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.StringJoiner;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
@Configuration
@ConfigurationProperties(prefix = "xcache")
public class CacheProperties {

    /**
     * 应用名称
     */
    private String app;

    /**
     * 缓存配置：模板
     * <p>
     * 当某个缓存未配置 template-id，默认采用 id 为 t0 的模板，因此建议将其中一个模板的 id 配置为 t0.
     * <p>
     */
    private List<Template> templates;

    /**
     * 缓存配置
     * <p>
     * caches 会从 templates 获取与配置的 template-id 对应的模板，
     * 并复制其中的全部选项，因此仅需配置缓存名称及与指定模板的差异项。
     */
    private List<CacheProps> caches;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    public List<CacheProps> getCaches() {
        return caches;
    }

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