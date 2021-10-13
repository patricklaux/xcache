package com.igeeksky.xcache.extension.update;

import com.igeeksky.xcache.common.CacheLevel;
import com.igeeksky.xcache.event.CacheEventType;

import java.io.Serializable;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-05
 */
public class CacheUpdateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private CacheEventType type;

    private String cacheId;

    private String store;

    private CacheLevel level;

    private String event;

    public CacheEventType getType() {
        return type;
    }

    public void setType(CacheEventType type) {
        this.type = type;
    }

    public String getCacheId() {
        return cacheId;
    }

    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public CacheLevel getLevel() {
        return level;
    }

    public void setLevel(CacheLevel level) {
        this.level = level;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "{\"type\":\"" +
                getType() +
                "\", \"cacheId\":\"" +
                getCacheId() +
                "\", \"store\":\"" +
                getStore() +
                "\", \"level\":" +
                getLevel() +
                ", \"event\":" +
                getEvent() +
                "}";
    }
}
