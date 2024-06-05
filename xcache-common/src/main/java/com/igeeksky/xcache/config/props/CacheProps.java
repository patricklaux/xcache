package com.igeeksky.xcache.config.props;


import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;

/**
 * 缓存配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class CacheProps extends AbstractCacheProps implements Cloneable {

    /**
     * 缓存名称（不能为空）
     */
    private String name;

    /**
     * 从指定模板中复制配置项
     * <p>
     * 如未指定，采用默认模板：t0
     *
     * @see com.igeeksky.xcache.config.CacheConstants#DEFAULT_TEMPLATE_ID
     */
    private String templateId;

    public CacheProps() {
    }

    public CacheProps(String name, String templateId) {
        this.name = name;
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public CacheProps clone() {
        try {
            CacheProps clone = (CacheProps) super.clone();
            clone.setLocal(this.getLocal().clone());
            clone.setRemote(this.getRemote().clone());
            clone.setExtension(this.getExtension().clone());
            clone.setMetadata(new HashMap<>(this.getMetadata()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("clone operation is not support.", e);
        }
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
