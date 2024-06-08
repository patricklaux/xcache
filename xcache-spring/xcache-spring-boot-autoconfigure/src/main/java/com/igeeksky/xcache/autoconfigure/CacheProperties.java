package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.props.CacheProps;
import com.igeeksky.xcache.props.TemplateProps;
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

    private String application;

    /**
     * 缓存配置：模板
     * <p>
     * 至少需要配置一个 template-id: t0 的模板
     */
    private List<TemplateProps> templates;

    /**
     * 缓存配置：仅需配置缓存名称及与指定模板的差异项。
     * <p>
     * 对于代码注解中出现的缓存名称，但在此处未配置的缓存，默认采用 t0模板。
     */
    private List<CacheProps> caches;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<TemplateProps> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateProps> templates) {
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
        joiner.add("\"application\":\"" + application + "\"");
        if (templates != null) {
            joiner.add("\"templates\":" + templates);
        }
        if (caches != null) {
            joiner.add("\"caches\":" + caches);
        }
        return joiner.toString();
    }
}
