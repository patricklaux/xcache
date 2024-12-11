package com.igeeksky.redis.sorted;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/12/8
 */
public class ScoredValue {

    private final byte[] value;
    private final double score;

    public static ScoredValue just(byte[] value, double score) {
        return new ScoredValue(value, score);
    }

    private ScoredValue(byte[] value, double score) {
        Objects.requireNonNull(value, "value must not be null");
        this.value = value;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScoredValue that = (ScoredValue) o;
        return Double.compare(score, that.score) == 0 && Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(value);
        result = 31 * result + Double.hashCode(score);
        return result;
    }

}
