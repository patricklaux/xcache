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
     * 如缓存未配置 template-id，则默认采用 id 为 t0 的模板，因此建议将其中一个模板的 id 配置为 t0.
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