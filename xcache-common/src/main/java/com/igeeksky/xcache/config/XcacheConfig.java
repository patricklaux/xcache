package com.igeeksky.xcache.config;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-05
 */
public class XcacheConfig {

    private String name;

    private Charset charset;

    private String application;

    private boolean enableLocal = true;

    private boolean enableRemote = true;

    private boolean enableCacheProxy = true;

    private Local local = new Local();

    private Remote remote = new Remote();

    public static class Local {

    }

    public static class Remote {

    }
}
