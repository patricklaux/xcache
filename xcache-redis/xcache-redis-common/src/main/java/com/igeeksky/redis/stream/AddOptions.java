package com.igeeksky.redis.stream;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * Stream：消息添加选项
 * <p>
 * 命令格式：{@code XADD key [NOMKSTREAM] [<MAXLEN | MINID> [= | ~] threshold [LIMIT count]] <* | id> field value [field value ...]}
 *
 * @author Patrick.Lau
 * @see <a href="https://redis.io/docs/latest/commands/xadd/">XADD</a>
 * @since 1.0.0 2024/7/19
 */
public class AddOptions {

    private final String id;

    private final Long maxLen;

    private final boolean approximateTrimming;

    private final boolean exactTrimming;

    private final boolean nomkstream;

    private final String minId;

    private final Long limit;

    /**
     * 私有构造器
     *
     * @param builder {@link Builder}
     */
    private AddOptions(Builder builder) {
        this.id = builder.id;
        this.maxLen = builder.maxLen;
        this.approximateTrimming = builder.approximateTrimming;
        this.exactTrimming = builder.exactTrimming;
        this.nomkstream = builder.nomkstream;
        this.minId = builder.minId;
        this.limit = builder.limit;
    }

    /**
     * 验证选项是否有效
     *
     * @return {@code boolean} – {@code true}：有效；{@code false}：无效
     */
    public boolean valid() {
        return id != null || maxLen != null || minId != null || limit != null
                || approximateTrimming || exactTrimming || nomkstream;
    }

    /**
     * 获取：流消息的 ID
     *
     * @return {@link String} – 流消息的 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取：流的最大长度
     *
     * @return {@link Long} – 流的最大长度
     */
    public Long getMaxLen() {
        return maxLen;
    }

    /**
     * 获取：是否近似修剪
     *
     * @return {@code boolean} –  {@code true}：使用 {@code ~} 标志近似修剪；{@code false}：不使用 {@code ~} 标志
     */
    public boolean isApproximateTrimming() {
        return approximateTrimming;
    }

    /**
     * 获取：是否精确修剪
     *
     * @return {@code boolean} –  {@code true}：使用 {@code =} 标志精确修剪；{@code false}：不使用 {@code =} 标志
     */
    public boolean isExactTrimming() {
        return exactTrimming;
    }

    /**
     * 获取：是否不创建流
     *
     * @return {@code boolean} –  {@code true}：如果流不存在，不创建流；{@code false}：如果流不存在，创建流
     */
    public boolean isNomkstream() {
        return nomkstream;
    }

    /**
     * 获取：裁剪的最小 ID
     *
     * @return {@link String} – 裁剪流的最小 ID
     */
    public String getMinId() {
        return minId;
    }

    /**
     * 获取：裁剪的最大数量
     *
     * @return {@link Long} – 裁剪的最大数量
     */
    public Long getLimit() {
        return limit;
    }

    /**
     * 创建 {@link Builder} 对象
     *
     * @return {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建 {@link AddOptions} 对象
     */
    public static class Builder {
        private String id;
        private Long maxLen;
        private boolean approximateTrimming;
        private boolean exactTrimming;
        private boolean nomkstream;
        private String minId;
        private Long limit;

        /**
         * 私有构造器
         */
        private Builder() {
        }

        /**
         * 指定流消息的 ID
         * <p>
         * 一般留空即可。如果使用自定义的 ID，则必须保证该 ID 全局单调递增。
         *
         * @param id 流消息的 ID
         * @return {@code this}
         */
        public Builder id(String id) {
            Assert.notNull(id, "id must not be null");

            this.id = id;
            return this;
        }

        /**
         * 指定流的最大长度
         * <p>
         * 如果流达到最大长度，则会自动修剪（精确 {@code =} 或 近似 {@code ~}）
         *
         * @param maxLen 流的最大长度
         * @return {@code this}
         */
        public Builder maxLen(long maxLen) {
            Assert.isTrue(maxLen > 0, "maxLen must be greater than 0");

            this.maxLen = maxLen;
            return this;
        }

        /**
         * 设为近似修剪（{@code ~} 标志）
         * <p>
         * 默认 {@code false}，调用此方法后，则设为 {@code true}
         *
         * @return {@code this}
         */
        public Builder approximateTrimming() {
            this.approximateTrimming = true;
            return this;
        }

        /**
         * 设为精确修剪（ {@code =} 标志）。
         * <p>
         * 默认 {@code false}，调用此方法后，则设为 {@code true}
         *
         * @return {@code this}
         */
        public Builder exactTrimming() {
            this.exactTrimming = true;
            return this;
        }

        /**
         * 是否不创建流
         * <p>
         * {@code false}：如果流不存在，创建流
         * {@code true}：如果流不存在，不创建流
         * <p>
         * 默认 {@code false}，调用此方法后，则设为 {@code true}
         *
         * @return {@code this}
         * @see <a href="https://redis.io/docs/latest/commands/xadd/">XADD</a>
         */
        public Builder nomkstream() {
            this.nomkstream = true;
            return this;
        }

        /**
         * 指定裁剪流的最小 ID
         * <p>
         * 删除小于此 minId 的消息。
         *
         * @param minId 最小 ID
         * @return {@code this}
         */
        public Builder minId(String minId) {
            Assert.notNull(minId, "minId must not be null");

            this.minId = minId;
            return this;
        }

        /**
         * 单次裁剪的最大数量
         * <p>
         * 此值仅适用于近似裁剪，因此同时设定 {@code approximateTrimming} 为 {@code true}。
         * <p>
         * 如未指定，RedisServer 将使用 100 作为默认值。<br>
         * 如设为 0，表示无限制。
         *
         * @param limit 最大数量
         * @return {@code this}
         */
        public Builder limit(long limit) {
            Assert.isTrue(limit >= 0, "limit must be greater than or equal to 0");

            this.limit = limit;
            this.approximateTrimming();
            return this;
        }

        /**
         * 构建 {@link AddOptions} 对象
         *
         * @return {@link AddOptions}
         */
        public AddOptions build() {
            return new AddOptions(this);
        }

    }

}