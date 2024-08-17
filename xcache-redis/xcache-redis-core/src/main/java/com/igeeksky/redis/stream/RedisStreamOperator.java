package com.igeeksky.redis.stream;

import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public interface RedisStreamOperator extends AutoCloseable {

    String xadd(byte[] key, Map<byte[], byte[]> body);

    String xadd(byte[] key, AddOptions args, Map<byte[], byte[]> body);

    List<StreamMessage> xread(ReadOffset... streams);

    List<StreamMessage> xread(ReadOptions args, ReadOffset... streams);

    String xgroupCreate(ReadOffset streamOffset, byte[] group);

    String xgroupCreate(ReadOffset streamOffset, byte[] group, GroupCreateOptions args);

    List<StreamMessage> xreadgroup(byte[] group, byte[] consumer, ReadOffset... streams);

    List<StreamMessage> xreadgroup(byte[] group, byte[] consumer, GroupReadOptions args, ReadOffset... streams);

    Long xack(byte[] key, byte[] group, String... messageIds);

    Long xdel(byte[] key, String... messageIds);

    boolean isOpen();
}