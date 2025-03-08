package com.igeeksky.xcache.common;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * 关闭行为
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public enum ShutdownBehavior {

    /**
     * 取消刷新任务
     * <p>
     * 取消任务队列中尚未开始执行的刷新任务，但等待已在运行的刷新任务完成
     */
    CANCEL,

    /**
     * 中断刷新任务
     * <p>
     * 取消任务队列中尚未开始执行的刷新任务，且中断已在运行的全部刷新任务
     */
    INTERRUPT,

    /**
     * 不做任何处理
     * <p>
     * 任务队列中的任务是否能成功完成，取决于数据源链接、缓存系统连接，与及应用系统何时关闭。
     */
    IGNORE,

    /**
     * 等待任务完成
     * <p>
     * 最大等待时长为设置的 shutdown_timeout 选项
     */
    AWAIT;

    /**
     * 根据名称获取枚举对象
     *
     * @param name 枚举名称
     * @return {@link ShutdownBehavior} – 枚举对象
     * @throws IllegalArgumentException 如果名称无效
     */
    public static ShutdownBehavior of(String name) {
        String trimmed = StringUtils.trimToNull(name);
        if (trimmed == null) {
            throw new IllegalArgumentException("name must not be null or empty.");
        }
        return ShutdownBehavior.valueOf(name.toUpperCase());
    }

}
