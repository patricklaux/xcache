package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * 缓存模板配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class Template extends AbstractProps {

    /**
     * 模板唯一标识（不能为空；不能重复）
     */
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}