package com.igeeksky.xcache.props;


import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * 缓存配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class CacheProps extends AbstractProps {

    /**
     * 缓存名称（不能为空）
     */
    private String name;

    /**
     * 从指定模板中复制配置项
     * <p>
     * 如未指定，采用默认模板：t0
     *
     * @see CacheConstants#DEFAULT_TEMPLATE_ID
     */
    private String templateId;

    public CacheProps() {
    }

    public CacheProps(String name) {
        this.name = name;
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
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}