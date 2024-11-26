package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * 缓存模板配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class Template extends AbstractProps {

    private String id;

    /**
     * 模板配置唯一标识，不能重复 (必填)
     * <p>
     * 建议将其中一个模板的 id 配置为 t0 ！！！<p>
     * 因为当缓存未显式指定 template-id 属性，会默认读取 id 为 t0 的模板。<p>
     * 也就是说，如无 id 为 t0 的模板，则所有缓存均需显式指定 template-id 属性。<p>
     *
     * @return {@link String} – 模板配置唯一标识
     */
    public String getId() {
        return id;
    }

    /**
     * 设置模板配置唯一标识，不能重复
     *
     * @param id 模板配置唯一标识
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}