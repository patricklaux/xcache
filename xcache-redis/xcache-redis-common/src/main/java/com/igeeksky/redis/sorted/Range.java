package com.igeeksky.redis.sorted;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/12/8
 */
public class Range {

    private final double min;
    private final double max;

    public Range(double min, double max) {
        this.min = min;
        this.max = max;
    }
}
