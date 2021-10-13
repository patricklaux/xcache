package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.SPI;

/**
 * @author Patrick.Lau
 * @date 2021-07-27
 */
@SPI
public interface RedisWriterProvider {

    RedisStringWriter getRedisStringWriter();

    RedisHashWriter getRedisHashWriter();

}
