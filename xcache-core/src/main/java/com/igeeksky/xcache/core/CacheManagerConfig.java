package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheConfigException;
import com.igeeksky.xcache.props.CacheProps;
import com.igeeksky.xcache.props.Template;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/8
 */
public class CacheManagerConfig {

    private final String app;

    private final Map<String, CacheProps> caches;

    private final Map<String, Template> templates;

    private final ComponentManager componentManager;

    private CacheManagerConfig(Builder builder) {
        this.app = builder.app;
        this.componentManager = builder.componentManager;
        this.templates = builder.templates;
        this.caches = builder.caches;
    }

    public String getApp() {
        return app;
    }

    public Map<String, CacheProps> getCaches() {
        return caches;
    }

    public Map<String, Template> getTemplates() {
        return templates;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String app;

        private ComponentManager componentManager;

        private final Map<String, Template> templates = new HashMap<>();

        private final Map<String, CacheProps> caches = new HashMap<>();

        public Builder app(String app) {
            this.app = app;
            return this;
        }

        public Builder componentManager(ComponentManager componentManager) {
            requireTrue(componentManager != null, "Cache-config: componentManager must not be null");
            this.componentManager = componentManager;
            return this;
        }

        public Builder templates(List<Template> templates) {
            if (CollectionUtils.isNotEmpty(templates)) {
                templates.forEach(this::template);
            }
            return this;
        }

        public Builder template(Template template) {
            requireTrue(template != null, "Cache-config: template must not be null");
            String id = StringUtils.trimToNull(template.getId());
            requireTrue(id != null, "Cache-config: id must not be null or empty.");
            requireTrue(this.templates.put(id, template) == null, "Cache-config: duplicate template: " + id);
            return this;
        }

        public Builder caches(List<CacheProps> caches) {
            if (CollectionUtils.isNotEmpty(caches)) {
                caches.forEach(this::cache);
            }
            return this;
        }

        public Builder cache(CacheProps cacheProps) {
            requireTrue(cacheProps != null, "Cache-config: cacheProps must not be null");

            String name = StringUtils.trimToNull(cacheProps.getName());
            requireTrue(name != null, "Cache-config: name must not be null or empty.");
            requireTrue(this.caches.put(name, cacheProps) == null, "Cache-config: duplicate cache: " + name);
            return this;
        }

        public CacheManagerConfig build() {
            requireTrue(app != null, "Cache-config: app must not be null");
            requireTrue(!templates.isEmpty(), "Cache-config: templates must not be empty.");
            return new CacheManagerConfig(this);
        }

        private static void requireTrue(boolean expression, String errMsg) {
            if (!expression) {
                throw new CacheConfigException(errMsg);
            }
        }

    }

}