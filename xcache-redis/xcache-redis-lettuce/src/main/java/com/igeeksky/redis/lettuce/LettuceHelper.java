package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.lettuce.config.LettuceGenericConfig;
import com.igeeksky.redis.lettuce.config.LettuceSentinelConfig;
import com.igeeksky.redis.stream.StreamMessage;
import com.igeeksky.redis.stream.*;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.StringUtils;
import io.lettuce.core.*;
import io.lettuce.core.XReadArgs.StreamOffset;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Lettuce 辅助工具
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-03
 */
public abstract class LettuceHelper {

    private LettuceHelper() {
    }

    public static RedisURI redisURI(LettuceGenericConfig config, RedisNode node) {
        String socket = node.getSocket();
        if (socket != null) {
            return process(RedisURI.Builder.socket(socket), config).build();
        }

        return process(RedisURI.builder(), config).withHost(node.getHost()).withPort(node.getPort()).build();
    }

    public static RedisURI sentinelURIBuilder(LettuceSentinelConfig config) {
        RedisURI.Builder builder = process(RedisURI.builder(), config).withSentinelMasterId(config.getMasterId());

        List<RedisURI> sentinels = sentinels(config);
        for (RedisURI sentinel : sentinels) {
            builder.withSentinel(sentinel);
        }

        return builder.build();
    }

    private static List<RedisURI> sentinels(LettuceSentinelConfig config) {
        List<RedisURI> sentinels = new ArrayList<>();
        List<RedisNode> nodes = config.getNodes();
        for (RedisNode node : nodes) {
            String host = node.getHost();
            int port = node.getPort();
            RedisURI sentinelURI = RedisURI.create(host, port);

            String sentinelUsername = StringUtils.trimToNull(config.getSentinelUsername());
            String sentinelPassword = StringUtils.trimToNull(config.getSentinelPassword());

            if (sentinelUsername != null || sentinelPassword != null) {
                RedisCredentials redisCredentials = RedisCredentials.just(sentinelUsername, sentinelPassword);
                sentinelURI.setCredentialsProvider(RedisCredentialsProvider.from(() -> redisCredentials));
            }

            sentinels.add(sentinelURI);
        }
        return sentinels;
    }

    private static RedisURI.Builder process(RedisURI.Builder builder, LettuceGenericConfig config) {
        builder.withDatabase(config.getDatabase())
                .withSsl(config.isSsl())
                .withStartTls(config.isStartTls())
                .withTimeout(Duration.ofMillis(config.getTimeout()))
                .withVerifyPeer(config.getSslVerifyMode());

        String clientName = config.getClientName();
        if (clientName != null) {
            builder.withClientName(clientName);
        }

        String username = config.getUsername();
        String password = config.getPassword();
        if (username != null && password != null) {
            builder.withAuthentication(username, password);
        } else if (username == null && password != null) {
            builder.withPassword(password.toCharArray());
        }

        return builder;
    }

    public static List<StreamMessage> convert(List<io.lettuce.core.StreamMessage<byte[], byte[]>> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return new ArrayList<>(0);
        }
        List<StreamMessage> list = new ArrayList<>(messages.size());
        for (io.lettuce.core.StreamMessage<byte[], byte[]> message : messages) {
            list.add(new StreamMessage(message.getStream(), message.getId(), message.getBody()));
        }
        return list;
    }

    public static XAddArgs convert(AddOptions options) {
        if (options == null || !options.valid()) {
            return null;
        }

        XAddArgs args = new XAddArgs();
        Long maxLen = options.getMaxLen();
        if (maxLen != null) {
            args.maxlen(maxLen);
        }
        Long limit = options.getLimit();
        if (limit != null) {
            args.limit(limit);
        }
        String id = options.getId();
        if (id != null) {
            args.id(id);
        }
        String minId = options.getMinId();
        if (minId != null) {
            args.minId(minId);
        }
        if (options.isApproximateTrimming()) {
            args.approximateTrimming();
        }
        if (options.isExactTrimming()) {
            args.exactTrimming();
        }
        if (options.isNomkstream()) {
            args.nomkstream();
        }

        return args;
    }

    public static XReadArgs convert(ReadOptions options) {
        if (options == null || !options.valid()) {
            return null;
        }
        return createXReadArgs(options.getBlock(), options.getCount());
    }

    public static XReadArgs convert(GroupReadOptions options) {
        if (options == null || !options.valid()) {
            return null;
        }
        return createXReadArgs(options.getBlock(), options.getCount()).noack(options.isNoack());
    }

    private static XReadArgs createXReadArgs(Long block, Long count) {
        XReadArgs args = new XReadArgs();
        if (block != null) {
            args.block(block);
        }
        if (count != null) {
            args.count(count);
        }
        return args;
    }

    public static StreamOffset<byte[]> convert(ReadOffset stream) {
        return StreamOffset.from(stream.getKey(), stream.getOffset());
    }

    @SuppressWarnings("unchecked")
    public static StreamOffset<byte[]>[] convert(ReadOffset[] streams) {
        int len = streams.length;
        var offsets = new StreamOffset[len];
        for (int i = 0; i < len; i++) {
            offsets[i] = StreamOffset.from(streams[i].getKey(), streams[i].getOffset());
        }
        return offsets;
    }

    public static XGroupCreateArgs convert(GroupCreateOptions options) {
        if (options == null || !options.valid()) {
            return null;
        }
        XGroupCreateArgs args = new XGroupCreateArgs();
        if (options.isMkstream()) {
            args.mkstream(true);
        }
        if (options.getEntriesRead() != null) {
            args.entriesRead(options.getEntriesRead());
        }
        return args;
    }

}