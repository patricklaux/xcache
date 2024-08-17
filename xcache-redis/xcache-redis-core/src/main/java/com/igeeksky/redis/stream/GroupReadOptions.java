package com.igeeksky.redis.stream;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public class GroupReadOptions {

    private final Long block;

    private final Long count;

    private final boolean noack;

    private GroupReadOptions(Long block, Long count, boolean noack) {
        this.block = block;
        this.count = count;
        this.noack = noack;
    }

    public boolean valid() {
        return block != null || count != null || noack;
    }

    public Long getBlock() {
        return block;
    }

    public Long getCount() {
        return count;
    }

    public boolean isNoack() {
        return noack;
    }

    public static GroupReadOptions block(Long block) {
        return new GroupReadOptions(block, null, false);
    }

    public static GroupReadOptions count(Long count) {
        return new GroupReadOptions(null, count, false);
    }

    public static GroupReadOptions noack() {
        return new GroupReadOptions(null, null, true);
    }

    public static GroupReadOptions newArgs(Long block, Long count) {
        return new GroupReadOptions(block, count, false);
    }

    public static GroupReadOptions newArgs(Long block, Long count, boolean noack) {
        return new GroupReadOptions(block, count, noack);
    }

}