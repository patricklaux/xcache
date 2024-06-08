package com.igeeksky.xcache.redis;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisPubSubListener {

    /**
     * Message received from a channel subscription.
     *
     * @param channel Channel.
     * @param message Message.
     */
    void message(String channel, byte[] message);

    /**
     * Message received from a pattern subscription.
     *
     * @param pattern Pattern
     * @param channel Channel
     * @param message Message
     */
    void message(String pattern, String channel, byte[] message);

    /**
     * Subscribed to a channel.
     *
     * @param channel Channel
     * @param count   Subscription count.
     */
    void subscribed(String channel, long count);

    /**
     * Subscribed to a pattern.
     *
     * @param pattern Pattern.
     * @param count   Subscription count.
     */
    void psubscribed(String pattern, long count);

    /**
     * Unsubscribed from a channel.
     *
     * @param channel Channel
     * @param count   Subscription count.
     */
    void unsubscribed(String channel, long count);

    /**
     * Unsubscribed from a pattern.
     *
     * @param pattern Channel
     * @param count   Subscription count.
     */
    void punsubscribed(String pattern, long count);

}
