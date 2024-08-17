package com.igeeksky.redis.lettuce;

import io.lettuce.core.*;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceBaseTest {

    // ClientResources ---start
    private final int ioThreadPoolSize = 4;
    private final int computationThreadPoolSize = 4;
    // ClientResources ---end

    // Standalone ---start
    private final String clientName = null;
    private final String username = "";
    private final String password = "";
    private final String host = "localhost";
    private final int port = 6379;
    private final int database = 0;
    // Standalone ---end

    // Sentinel ---start
    private String master;
    private String nodes;
    private long timeout;
    private boolean ssl;
    private boolean startTls;
    private boolean verifyPeer;
    private SslVerifyMode sslVerifyMode;

    // Sentinel ---end

    // ClientOptions ---start

    // ClientOptions ---end

    @Test
    void testStandalone() {
        // 创建 RedisURI
        RedisURI.Builder uriBuilder = RedisURI.Builder
                .redis(host, port)
                .withClientName(clientName)
                .withDatabase(database)
                .withAuthentication(username, password)
                .withTimeout(Duration.ofMillis(timeout))
                .withSsl(ssl)
                .withStartTls(startTls)
                .withVerifyPeer(verifyPeer)
                .withVerifyPeer(sslVerifyMode);

        RedisURI redisURI = uriBuilder.build();
        ClientOptions clientOptions = ClientOptions.builder()
                .pingBeforeActivateConnection(false)
                .autoReconnect(false)
                //.decodeBufferPolicy()
                .suspendReconnectOnProtocolFailure(false)
                .requestQueueSize(Integer.MAX_VALUE)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.DEFAULT)
                .protocolVersion(ProtocolVersion.newestSupported())
                .scriptCharset(StandardCharsets.UTF_8)
                .socketOptions(SocketOptions.create())
                .sslOptions(SslOptions.builder().build())
                .timeoutOptions(TimeoutOptions.create())
                .publishOnScheduler(false)
                .build();

        // 创建 ClientResources
        ClientResources res = clientResources();
        RedisClient redisClient = RedisClient.create(res, redisURI);
        redisClient.setOptions(clientOptions);
    }

    @Test
    void testSentinel() {
        // 创建 ClientResources
        ClientResources res = clientResources();

        // 创建 RedisURI
        RedisURI.Builder uriBuilder = RedisURI.Builder
                .sentinel(host, port, master)
                .withAuthentication(username, password)     // 需判断用户名是否为空
                .withClientName(clientName)
                .withDatabase(database)
                .withSsl(ssl)
                .withStartTls(startTls)
                .withTimeout(Duration.ofMillis(timeout))
                .withVerifyPeer(verifyPeer)
                .withVerifyPeer(sslVerifyMode);
        String[] array = nodes.split(",");
        for (String node : array) {
            String[] split = node.split(":");
            uriBuilder.withSentinel(split[0], Integer.parseInt(split[1]), password);
        }
        RedisURI redisURI = uriBuilder.build();

        ClientOptions clientOptions = ClientOptions.builder()
                .pingBeforeActivateConnection(false)
                .autoReconnect(false)
                //.decodeBufferPolicy()
                .suspendReconnectOnProtocolFailure(false)
                .requestQueueSize(Integer.MAX_VALUE)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.DEFAULT)
                .protocolVersion(ProtocolVersion.newestSupported())
                .scriptCharset(StandardCharsets.UTF_8)
                .socketOptions(SocketOptions.create())
                .sslOptions(SslOptions.builder().build())
                .timeoutOptions(TimeoutOptions.create())
                .publishOnScheduler(false)
                .build();

        RedisClient redisClient = RedisClient.create(res, redisURI);

        redisClient.setOptions(clientOptions);

        StatefulRedisSentinelConnection<byte[], byte[]> statefulRedisSentinelConnection = redisClient.connectSentinel(ByteArrayCodec.INSTANCE, redisURI);
    }

    void testCluster() {
        // 创建 ClusterTopologyRefreshOptions （集群拓扑信息刷新）
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(false)
                //.refreshPeriod(Duration.ofMinutes(10))
                //.enablePeriodicRefresh(Duration.ofMinutes(10))
                .enableAllAdaptiveRefreshTriggers()
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(30))
                .refreshTriggersReconnectAttempts(5)
                .dynamicRefreshSources(true)
                .closeStaleConnections(true)
                .build();

        // 创建 ClientResources （Netty IO及线程设置）
        ClientResources res = clientResources();

        // 创建集群 URI
        List<RedisURI> redisURIS = new ArrayList<>();
        String[] array = nodes.split(",");
        for (String node : array) {
            String[] hostAndPort = node.split(":");
            RedisURI redisURI = RedisURI.create(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
            // 设置密码等
            redisURIS.add(redisURI);
        }

        RedisClusterClient redisClusterClient = RedisClusterClient.create(res, redisURIS);
        redisClusterClient.setOptions(ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions) // cluster
                .maxRedirects(5)                                // cluster
                .nodeFilter(null)                               // cluster
                .validateClusterNodeMembership(true)            // cluster
                .pingBeforeActivateConnection(false)
                .autoReconnect(true)
                //.decodeBufferPolicy()
                .suspendReconnectOnProtocolFailure(false)
                .requestQueueSize(Integer.MAX_VALUE)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.DEFAULT)
                .protocolVersion(ProtocolVersion.newestSupported())
                .scriptCharset(StandardCharsets.UTF_8)
                .socketOptions(SocketOptions.create())
                .sslOptions(SslOptions.builder().build())
                .timeoutOptions(TimeoutOptions.create())
                .publishOnScheduler(false)
                .build());
    }

    private ClientResources clientResources() {
        return DefaultClientResources.builder().build();
        // DefaultClientResources.Builder builder = DefaultClientResources.builder();
        // return builder.ioThreadPoolSize(ioThreadPoolSize)
        //         .computationThreadPoolSize(computationThreadPoolSize)
        //         //.addressResolverGroup(DefaultAddressResolverGroup.INSTANCE)
        //         //.commandLatencyRecorder(new DefaultCommandLatencyCollector())
        //         .commandLatencyPublisherOptions()
        //         .dnsResolver()
        //         .eventBus()
        //         .eventLoopGroupProvider()
        //         .eventExecutorGroup()
        //         .nettyCustomizer()
        //         .reconnectDelay()
        //         .socketAddressResolver()
        //         .threadFactoryProvider()
        //         .timer()
        //         .tracing()
        //         .build();
    }


}