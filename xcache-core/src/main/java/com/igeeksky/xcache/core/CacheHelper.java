package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 缓存工具类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2025/3/14
 */
public final class CacheHelper {

    /**
     * 缓存工具类
     */
    private CacheHelper() {
    }

    /**
     * 移除已命中的 key
     *
     * @param cloneKeys 键集
     * @param result    缓存查询结果
     * @param <V>       缓存值类型
     */
    public static <V> void removeHitKeys(Set<String> cloneKeys, Map<String, CacheValue<V>> result) {
        if (Maps.isEmpty(result)) {
            return;
        }
        for (Map.Entry<String, CacheValue<V>> entry : result.entrySet()) {
            if (entry.getValue() != null) {
                cloneKeys.remove(entry.getKey());
            }
        }
    }

    /**
     * 创建新的 {@link HashMap} 作为返回给用户的结果集，并将二级缓存数据保存到一级缓存。
     * <p>
     * 1. 创建新的 {@link HashMap} 作为返回给用户的结果集。<br>
     * 2. 如果一级缓存数据不为空，则将一级缓存数据添加到结果集。<br>
     * 3. 如果二级缓存数据不为空，则将二级缓存数据添加到结果集，并将二级缓存数据保存到一级缓存。
     *
     * @param firstAll   一级缓存数据
     * @param secondAll  二级缓存数据
     * @param firstStore 一级缓存
     * @param <V>        缓存值类型
     * @return 结果集
     */
    public static <V> Map<String, CacheValue<V>> mergeResult(Map<String, CacheValue<V>> firstAll,
                                                             Map<String, CacheValue<V>> secondAll,
                                                             Store<V> firstStore) {
        int size = 0;
        if (firstAll != null) size += firstAll.size();
        if (secondAll != null) size += secondAll.size();
        if (size == 0) return Collections.emptyMap();

        Map<String, CacheValue<V>> finalResult = HashMap.newHashMap(size);
        addToResult(finalResult, firstAll);
        Map<String, V> saveToLower = addToResultAndCollect(finalResult, secondAll);
        if (Maps.isNotEmpty(saveToLower)) {
            firstStore.putAllAsync(saveToLower);
        }
        return finalResult;
    }

    /**
     * 创建新的 {@link HashMap} 作为返回给用户的结果集，并将三级缓存数据保存到一级缓存和二级缓存。
     * <p>
     * 1. 创建新的 {@link HashMap} 作为返回给用户的结果集。<br>
     * 2. 如果一级缓存数据不为空，则将一级缓存数据添加到结果集。<br>
     * 3. 如果二级缓存数据不为空，则将二级缓存数据添加到结果集。<br>
     * 3. 如果三级缓存数据不为空，则将三级缓存数据添加到结果集，并将三级缓存数据保存到一级缓存和二级缓存。
     *
     * @param firstAll    一级缓存数据
     * @param secondAll   二级缓存数据
     * @param thirdAll    三级缓存数据
     * @param firstStore  一级缓存
     * @param secondStore 二级缓存
     * @param <V>         缓存值类型
     * @return 结果集
     */
    public static <V> Map<String, CacheValue<V>> mergeResult(Map<String, CacheValue<V>> firstAll,
                                                             Map<String, CacheValue<V>> secondAll,
                                                             Map<String, CacheValue<V>> thirdAll,
                                                             Store<V> firstStore,
                                                             Store<V> secondStore) {
        int size = 0;
        if (firstAll != null) size += firstAll.size();
        if (secondAll != null) size += secondAll.size();
        if (thirdAll != null) size += thirdAll.size();
        if (size == 0) return Collections.emptyMap();

        Map<String, CacheValue<V>> finalResult = HashMap.newHashMap(size);
        addToResult(finalResult, firstAll);
        addToResult(finalResult, secondAll);
        Map<String, V> saveToLower = addToResultAndCollect(finalResult, thirdAll);
        if (Maps.isNotEmpty(saveToLower)) {
            secondStore.putAllAsync(saveToLower)
                    .thenCompose(ignored -> firstStore.putAllAsync(saveToLower));
        }
        return finalResult;
    }

    private static <V> void addToResult(Map<String, CacheValue<V>> finalResult, Map<String, CacheValue<V>> result) {
        if (result == null) {
            return;
        }
        for (Map.Entry<String, CacheValue<V>> entry : result.entrySet()) {
            CacheValue<V> cacheValue = entry.getValue();
            if (cacheValue != null) {
                finalResult.put(entry.getKey(), cacheValue);
            }
        }
    }

    private static <V> Map<String, V> addToResultAndCollect(Map<String, CacheValue<V>> finalResult,
                                                            Map<String, CacheValue<V>> result) {
        if (result == null) {
            return Collections.emptyMap();
        }
        Map<String, V> saveToLower = HashMap.newHashMap(result.size());
        for (Map.Entry<String, CacheValue<V>> entry : result.entrySet()) {
            CacheValue<V> cacheValue = entry.getValue();
            String key = entry.getKey();
            if (cacheValue != null) {
                saveToLower.put(key, cacheValue.getValue());
                finalResult.put(key, cacheValue);
            }
        }
        return saveToLower;
    }

}
