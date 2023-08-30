package com.igeeksky.xcache.common;


import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-25
 */
public enum CacheLevel {

    /**
     * L0为系统预置的无操作缓存
     */
    L0,

    /**
     * L1~L9为用户自配置缓存
     */
    L1,

    L2,

    L3,

    L4,

    L5,

    L6,

    L7,

    L8,

    L9;

    private static final Map<String, CacheLevel> SUPPORTED_CACHE_LEVEL;

    static {
        Map<String, CacheLevel> cacheLevelMap = new LinkedHashMap<>();
        CacheLevel[] cacheLevels = CacheLevel.values();
        for (CacheLevel cacheLevel : cacheLevels) {
            cacheLevelMap.put(cacheLevel.name().toUpperCase(), cacheLevel);
        }
        SUPPORTED_CACHE_LEVEL = Collections.unmodifiableMap(cacheLevelMap);
    }

    public static CacheLevel getByName(String name) {
        if (StringUtils.hasText(name)) {
            return null;
        }
        String upperCase = name.toUpperCase();
        return SUPPORTED_CACHE_LEVEL.get(upperCase);
    }

}
