package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.stream.*;
import com.igeeksky.xtool.core.io.IOUtils;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import io.lettuce.core.Consumer;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.XGroupCreateArgs;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.sync.RedisStreamCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public class LettuceStreamOperator implements RedisStreamOperator {

    private final StatefulConnection<byte[], byte[]> connection;
    private final RedisStreamCommands<byte[], byte[]> commands;

    public LettuceStreamOperator(StatefulConnection<byte[], byte[]> connection,
                                 RedisStreamCommands<byte[], byte[]> commands) {
        this.connection = connection;
        this.commands = commands;
    }

    @Override
    public String xadd(byte[] key, Map<byte[], byte[]> body) {
        return commands.xadd(key, body);
    }

    @Override
    public String xadd(byte[] key, AddOptions options, Map<byte[], byte[]> body) {
        XAddArgs args = LettuceHelper.convert(options);
        if (args == null) {
            return commands.xadd(key, body);
        }
        return commands.xadd(key, args, body);
    }

    @Override
    public List<StreamMessage> xread(ReadOffset... streams) {
        return this.xread(null, streams);
    }

    @Override
    public List<StreamMessage> xread(ReadOptions options, ReadOffset... streams) {
        if (ArrayUtils.isEmpty(streams)) {
            return new ArrayList<>(0);
        }

        XReadArgs xreadArgs = LettuceHelper.convert(options);
        if (xreadArgs == null) {
            return LettuceHelper.convert(commands.xread(LettuceHelper.convert(streams)));
        }
        return LettuceHelper.convert(commands.xread(xreadArgs, LettuceHelper.convert(streams)));
    }

    @Override
    public String xgroupCreate(ReadOffset streamOffset, byte[] group) {
        return commands.xgroupCreate(LettuceHelper.convert(streamOffset), group);
    }

    @Override
    public String xgroupCreate(ReadOffset offset, byte[] group, GroupCreateOptions options) {
        XGroupCreateArgs args = LettuceHelper.convert(options);
        if (args == null) {
            return commands.xgroupCreate(LettuceHelper.convert(offset), group);
        }
        return commands.xgroupCreate(LettuceHelper.convert(offset), group, args);
    }

    @Override
    public List<StreamMessage> xreadgroup(byte[] group, byte[] consumer, ReadOffset... offsets) {
        return this.xreadgroup(group, consumer, null, offsets);
    }

    @Override
    public List<StreamMessage> xreadgroup(byte[] group, byte[] consumer, GroupReadOptions options, ReadOffset... offsets) {
        if (ArrayUtils.isEmpty(offsets)) {
            return new ArrayList<>(0);
        }

        Consumer<byte[]> consumer1 = Consumer.from(group, consumer);
        XReadArgs args = LettuceHelper.convert(options);
        if (args == null) {
            return LettuceHelper.convert(commands.xreadgroup(consumer1, LettuceHelper.convert(offsets)));
        }
        return LettuceHelper.convert(commands.xreadgroup(consumer1, args, LettuceHelper.convert(offsets)));
    }

    @Override
    public Long xack(byte[] key, byte[] group, String... messageIds) {
        return commands.xack(key, group, messageIds);
    }

    @Override
    public Long xdel(byte[] key, String... messageIds) {
        return commands.xdel(key, messageIds);
    }

    @Override
    public boolean isOpen() {
        return connection.isOpen();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.connection);
    }

}