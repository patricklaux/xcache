package com.igeeksky.xcache.common;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.1 2017-03-02 21:37:52
 */
public class KeyValue<K, V> {

    private final K key;

    private final V value;

    public KeyValue() {
        this(null, null);
    }

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public boolean hasValue() {
        return null != value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValue<?, ?> keyValue)) return false;

        return Objects.equals(getKey(), keyValue.getKey()) && Objects.equals(getValue(), keyValue.getValue());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getKey());
        result = 31 * result + Objects.hashCode(getValue());
        return result;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
