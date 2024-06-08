package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class TemplateProps extends AbstractProps {

    /**
     * 模板唯一标识（不能为空；不能重复）
     */
    private String templateId;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public CacheProps toCacheProps() {
        try {
            CacheProps cacheProps = new CacheProps();
            cacheProps.setCharset(this.getCharset());
            cacheProps.setTemplateId(this.getTemplateId());
            cacheProps.setCacheType(this.getCacheType());

            cacheProps.setLocal(this.getLocal().clone());
            cacheProps.setRemote(this.getRemote().clone());
            cacheProps.setExtension(this.getExtension().clone());
            cacheProps.setMetadata(new HashMap<>(this.getMetadata()));
            return cacheProps;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("clone operation is not support.", e);
        }
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
