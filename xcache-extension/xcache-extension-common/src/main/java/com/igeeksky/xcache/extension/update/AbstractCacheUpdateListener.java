package com.igeeksky.xcache.extension.update;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-16
 */
public abstract class AbstractCacheUpdateListener implements CacheUpdateListener {

    protected final CacheUpdatePolicy<?, ?> updatePolicy;

    public AbstractCacheUpdateListener(CacheUpdatePolicy<?, ?> updatePolicy) {
        this.updatePolicy = updatePolicy;
    }

    @Override
    public void onMessage(byte[] msgBytes) {
        updatePolicy.onMessage(msgBytes);
    }

}
