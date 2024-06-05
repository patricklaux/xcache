package com.igeeksky.xcache.config.props;

import com.igeeksky.xcache.common.CacheType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author patrick
 * @since 0.0.4 2024/6/3
 */
public abstract class AbstractCacheProps {

    /**
     * 默认字符集：UTF-8
     *
     * @see com.igeeksky.xcache.config.CacheConstants#DEFAULT_CHARSET_NAME
     */
    private String charset;

    /**
     * 默认：BOTH（本地缓存 + 远程缓存）
     *
     * @see com.igeeksky.xcache.config.CacheConstants#DEFAULT_CACHE_TYPE
     */
    private CacheType cacheType;

    /**
     * 本地缓存配置
     */
    private LocalProps local = new LocalProps();

    /**
     * 远程缓存配置
     */
    private RemoteProps remote = new RemoteProps();

    /**
     * 扩展项目配置
     */
    private ExtensionProps extension = new ExtensionProps();

    /**
     * 自定义参数，自定义实现需要用到的配置项
     */
    private Map<String, Object> metadata = new HashMap<>();

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public LocalProps getLocal() {
        return local;
    }

    public void setLocal(LocalProps local) {
        this.local = local;
    }

    public RemoteProps getRemote() {
        return remote;
    }

    public void setRemote(RemoteProps remote) {
        this.remote = remote;
    }

    public ExtensionProps getExtension() {
        return extension;
    }

    public void setExtension(ExtensionProps extension) {
        this.extension = extension;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
