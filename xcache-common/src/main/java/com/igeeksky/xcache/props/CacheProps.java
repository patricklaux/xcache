package com.igeeksky.xcache.props;


import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * 缓存配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class CacheProps extends AbstractProps {

    private String name;

    private String templateId;

    /**
     * 默认构造函数
     */
    public CacheProps() {
        super();
    }

    /**
     * 构造函数
     *
     * @param name 缓存名称（不能为空）
     */
    public CacheProps(String name) {
        super();
        this.name = name;
    }

    /**
     * 缓存名称（不能为空）
     * <p>
     * 用于唯一标识缓存对象实例
     *
     * @return {@link String} – 缓存名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置缓存名称
     *
     * @param name 缓存名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 要使用的模板的 id
     * <p>
     * 用于从指定模板复制配置项，然后再用缓存个性化配置项覆盖模板配置项
     * <p>
     * 默认值：t0
     * <p>
     * {@link CacheConstants#DEFAULT_TEMPLATE_ID}
     *
     * @return {@link String} – 模板ID
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * 设置要使用的模板的 id
     *
     * @param templateId 模板ID
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * 缓存配置项的 JSON 字符串
     *
     * @return {@link String} – JSON 字符串
     */
    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}