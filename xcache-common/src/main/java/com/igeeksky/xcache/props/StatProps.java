package com.igeeksky.xcache.props;

/**
 * 日志方式缓存指标统计配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/25
 */
public class StatProps {

    /**
     * 缓存统计的时间间隔（可为空）
     */
    private Long period;

    /**
     * 缓存统计的时间间隔（可为空）
     * <p>
     * 默认值：60000 单位：毫秒
     * <p>
     * 如果采用内嵌的缓存统计（写入日志），可以通过此配置调整统计周期。
     * 打印日志由 {@code com.igeeksky.xcache.extension.stat.LogStatMessagePublisher} 完成，日志级别为 INFO。<p>
     * 用户可以通过调整该类的日志配置，以控制缓存统计日志是否输出及输出方式。
     * 譬如输出到独立的统计日志文件，或者通过插件输出到 MQ，然后采集计算实现全局统计。
     * <p>
     * {@link CacheConstants#DEFAULT_STAT_PERIOD}
     *
     * @return Long – 缓存统计的时间间隔
     */
    public Long getPeriod() {
        return period;
    }

    /**
     * 设置缓存统计的时间间隔
     *
     * @param period 缓存统计的时间间隔
     */
    public void setPeriod(Long period) {
        this.period = period;
    }

}
