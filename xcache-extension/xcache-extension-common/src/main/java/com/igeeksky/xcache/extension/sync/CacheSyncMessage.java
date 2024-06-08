package com.igeeksky.xcache.extension.sync;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheSyncMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = -9034710794861743946L;

    public static final int TYPE_REMOVE = 0;
    public static final int TYPE_CLEAR = 1;

    /**
     * service id
     */
    private String sid;

    /**
     * operation type
     */
    private int type;

    private Set<String> keys;

    public CacheSyncMessage() {
    }

    public CacheSyncMessage(String sid, int type) {
        this.sid = sid;
        this.type = type;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public CacheSyncMessage setKeys(Set<String> keys) {
        this.keys = keys;
        return this;
    }

    public CacheSyncMessage addKey(String key) {
        if (this.keys == null) {
            this.keys = new LinkedHashSet<>();
        }
        this.keys.add(key);
        return this;
    }

}
