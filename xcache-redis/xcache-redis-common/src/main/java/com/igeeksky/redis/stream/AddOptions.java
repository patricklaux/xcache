package com.igeeksky.redis.stream;

/**
 * @author Patrick.Lau
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

    private AddOptions(Builder builder) {
        this.id = builder.id;
        this.maxLen = builder.maxLen;
        this.approximateTrimming = builder.approximateTrimming;
        this.exactTrimming = builder.exactTrimming;
        this.nomkstream = builder.nomkstream;
        this.minId = builder.minId;
        this.limit = builder.limit;
    }

    public boolean valid() {
        return id != null || maxLen != null || minId != null || limit != null
                || approximateTrimming || exactTrimming || nomkstream;
    }

    public String getId() {
        return id;
    }

    public Long getMaxLen() {
        return maxLen;
    }

    public boolean isApproximateTrimming() {
        return approximateTrimming;
    }

    public boolean isExactTrimming() {
        return exactTrimming;
    }

    public boolean isNomkstream() {
        return nomkstream;
    }

    public String getMinId() {
        return minId;
    }

    public Long getLimit() {
        return limit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Long maxLen;
        private boolean approximateTrimming;
        private boolean exactTrimming;
        private boolean nomkstream;
        private String minId;
        private Long limit;

        /**
         * 指定流消息的 ID
         * <p>
         * 一般留空即可。如果使用自定义的 ID，则必须保证该 ID 全局单调递增。
         *
         * @param id 流消息的 ID
         * @return {@code this}
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * 指定流消息的最大长度
         * <p>
         * 如果流消息达到最大长度，则会自动修剪（精确 {@code =} 或 近似 {@code ~}）
         *
         * @param maxLen 流消息的最大长度
         * @return {@code this}
         */
        public Builder maxLen(long maxLen) {
            this.maxLen = maxLen;
            return this;
        }

        /**
         * 是否使用 {@code ~} 标志对流长度进行近似修剪。
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
         * 是否使用 {@code =} 标志对流长度进行精确修剪。
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
         * {@code false}：如果流不存在，创建流
         * {@code true}：如果流不存在，不创建流
         * <p>
         * 默认 {@code false}，调用此方法后，则设为 {@code true}
         *
         * @return {@code this}
         */
        public Builder nomkstream() {
            this.nomkstream = true;
            return this;
        }

        /**
         * 指定裁剪流的最小 ID
         * <p>
         * 删除流中 ID 小于此 minId 的消息。<p>
         * 如果流中原来有比此 minID 更小的消息，则删除后流中的最小 ID 为此 minId；<p>
         * 如果流中原来无比此 minID 更小的消息，则依然是原来最旧的 ID
         *
         * @param minId 最小 ID
         * @return {@code this}
         */
        public Builder minId(String minId) {
            this.minId = minId;
            return this;
        }

        /**
         * 指定裁剪的最大数量，并同时指定 approximateTrimming 为 {@code true}
         * <p>
         * 因为只有当使用 {@code ~} 标志时，此配置才有意义
         *
         * @param limit 最大数量
         * @return {@code this}
         */
        public Builder limit(long limit) {
            this.limit = limit;
            this.approximateTrimming();
            return this;
        }

        public AddOptions build() {
            return new AddOptions(this);
        }

    }

}