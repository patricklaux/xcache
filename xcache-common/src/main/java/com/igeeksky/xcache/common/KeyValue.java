package com.igeeksky.xcache.common;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValue)) return false;

        KeyValue<?, ?> keyValue = (KeyValue<?, ?>) o;

        if (getKey() != null ? !getKey().equals(keyValue.getKey()) : keyValue.getKey() != null) return false;
        return getValue() != null ? getValue().equals(keyValue.getValue()) : keyValue.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = getKey() != null ? getKey().hashCode() : 0;
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyValue{" + "key=" + key +
                ", value=" + value +
                ", hasValue=" + hasValue() +
                '}';
    }
}
