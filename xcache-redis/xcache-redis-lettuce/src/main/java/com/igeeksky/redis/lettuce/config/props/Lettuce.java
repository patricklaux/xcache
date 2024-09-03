package com.igeeksky.redis.lettuce.config.props;


import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.redis.lettuce.config.LettuceClusterConfig;
import com.igeeksky.redis.lettuce.config.LettuceGenericConfig;
import com.igeeksky.redis.lettuce.config.LettuceSentinelConfig;
import com.igeeksky.redis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SslVerifyMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-27
 */
public class Lettuce {

    /**
     * {@link RedisOperatorFactory} 唯一标识
     * <p>
     * 默认值：lettuce
     * <p>
     * 如果仅一套 Redis 配置，建议保持默认值。<p>
     * 如果有多套 Redis 配置，可以依次配置为 lettuce1, lettuce2 …… 或你所喜欢的任何名称，保持唯一即可
     */
    private String id = "lettuce";

    /**
     * 集群模式
     */
    private LettuceCluster cluster;

    /**
     * 哨兵模式
     */
    private LettuceSentinel sentinel;

    /**
     * 单机模式 或 主从模式
     */
    private LettuceStandalone standalone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LettuceCluster getCluster() {
        return cluster;
    }

    public void setCluster(LettuceCluster cluster) {
        this.cluster = cluster;
    }

    public LettuceSentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(LettuceSentinel sentinel) {
        this.sentinel = sentinel;
    }

    public LettuceStandalone getStandalone() {
        return standalone;
    }

    public void setStandalone(LettuceStandalone standalone) {
        this.standalone = standalone;
    }

    public LettuceStandaloneConfig createStandaloneConfig() {
        LettuceStandaloneConfig config = new LettuceStandaloneConfig();
        setGeneric(this.id, standalone, config);

        String node = StringUtils.trimToNull(standalone.getNode());
        if (node != null) {
            config.setNode(new RedisNode(node));
        }

        String readFrom = StringUtils.trimToNull(standalone.getReadFrom());
        if (readFrom != null) {
            config.setReadFrom(ReadFrom.valueOf(readFrom));
        }

        config.setNodes(convertNodes(standalone.getNodes()));
        config.setClientOptions(standalone.getClientOptions());

        return config;
    }

    public LettuceSentinelConfig createSentinelConfig() {
        LettuceSentinelConfig config = new LettuceSentinelConfig();
        setGeneric(this.id, sentinel, config);

        String masterId = StringUtils.trimToNull(sentinel.getMasterId());
        Assert.notNull(masterId, () -> "Id:[" + this.id + "] sentinel:master-id must not be null or empty");
        config.setMasterId(masterId);

        List<RedisNode> sentinels = convertNodes(sentinel.getNodes());
        Assert.notEmpty(sentinels, () -> "Id:[" + this.id + "] sentinel:nodes must not be empty");
        config.setNodes(sentinels);

        String readFrom = StringUtils.trimToNull(sentinel.getReadFrom());
        if (readFrom != null) {
            config.setReadFrom(ReadFrom.valueOf(readFrom));
        }

        String sentinelUsername = StringUtils.trimToNull(sentinel.getUsername());
        if (sentinelUsername != null) {
            config.setSentinelUsername(sentinelUsername);
        }

        String sentinelPassword = StringUtils.trimToNull(sentinel.getPassword());
        if (sentinelPassword != null) {
            config.setSentinelPassword(sentinelPassword);
        }

        config.setClientOptions(sentinel.getClientOptions());

        return config;
    }

    public LettuceClusterConfig createClusterConfig() {
        LettuceClusterConfig config = new LettuceClusterConfig();
        setGeneric(this.id, cluster, config);

        List<RedisNode> nodes = convertNodes(cluster.getNodes());
        Assert.notEmpty(nodes, () -> "Id:[" + this.id + "] cluster:nodes must not be empty");
        config.setNodes(nodes);

        String readFrom = StringUtils.trimToNull(cluster.getReadFrom());
        if (readFrom != null) {
            config.setReadFrom(ReadFrom.valueOf(readFrom));
        }

        config.setClientOptions(cluster.getClientOptions());

        return config;
    }

    private static void setGeneric(String id, LettuceGeneric original, LettuceGenericConfig config) {
        id = StringUtils.trimToNull(id);
        Assert.notNull(id, "lettuce:connections:id must not be null or empty");

        config.setId(id);

        String username = StringUtils.trimToNull(original.getUsername());
        if (username != null) {
            config.setUsername(username);
        }

        String password = StringUtils.trimToNull(original.getPassword());
        if (password != null) {
            config.setPassword(password);
        }

        config.setDatabase(original.getDatabase());

        String clientName = StringUtils.trimToNull(original.getClientName());
        if (clientName != null) {
            config.setClientName(clientName);
        }

        Boolean ssl = original.getSsl();
        if (ssl != null) {
            config.setSsl(ssl);
        }

        Boolean startTls = original.getStartTls();
        if (startTls != null) {
            config.setStartTls(startTls);
        }

        String sslVerifyMode = StringUtils.toUpperCase(original.getSslVerifyMode());
        if (StringUtils.hasLength(sslVerifyMode)) {
            config.setSslVerifyMode(SslVerifyMode.valueOf(sslVerifyMode));
        }

        Long timeout = original.getTimeout();
        if (timeout != null) {
            config.setTimeout(timeout);
        }
    }

    private static List<RedisNode> convertNodes(List<String> sources) {
        List<RedisNode> nodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sources)) {
            sources.forEach(node -> nodes.add(new RedisNode(node)));
        }
        return nodes;
    }


    /**
     * 客户端配置选项
     * <p>
     * 注意：{@link io.lettuce.core.protocol.DecodeBufferPolicy} 需要编程实现，无法配置
     *
     * @see io.lettuce.core.ClientOptions
     */
    public static class ClientOptions {

        /**
         * 默认值：true
         *
         * @see io.lettuce.core.ClientOptions#DEFAULT_AUTO_RECONNECT
         */
        private Boolean autoReconnect;

        /**
         * 默认值：DEFAULT
         * <p>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_DISCONNECTED_BEHAVIOR}
         * <p>
         * <b>可选项</b>：<p>
         * DEFAULT：如果 auto-reconnect 为 true 则连接断开时接受命令, 如果 auto-reconnect 为 false, 则连接断开时拒绝命令
         * <p>
         * ACCEPT_COMMANDS：连接断开时接受命令
         * <p>
         * REJECT_COMMANDS：连接断开时拒绝命令
         *
         * @see io.lettuce.core.ClientOptions.DisconnectedBehavior
         */
        private String disconnectedBehavior;

        /**
         * 默认值：true
         *
         * @see io.lettuce.core.ClientOptions#DEFAULT_PING_BEFORE_ACTIVATE_CONNECTION
         */
        private Boolean pingBeforeActivateConnection;

        /**
         * 默认值：RESP3
         *
         * @see io.lettuce.core.ClientOptions#DEFAULT_PROTOCOL_VERSION
         */
        private String protocolVersion;

        /**
         * 默认值：false
         *
         * @see io.lettuce.core.ClientOptions#DEFAULT_PUBLISH_ON_SCHEDULER
         */
        private Boolean publishOnScheduler;

        /**
         * 默认值：Integer.MAX_VALUE (2147483647)
         *
         * @see io.lettuce.core.ClientOptions#DEFAULT_REQUEST_QUEUE_SIZE
         */
        private Integer requestQueueSize;

        /**
         * 默认值：UTF-8
         *
         * @see io.lettuce.core.ClientOptions#DEFAULT_SCRIPT_CHARSET
         */
        private String scriptCharset;

        /**
         * 默认值：false
         *
         * @see io.lettuce.core.ClientOptions#DEFAULT_SUSPEND_RECONNECT_PROTO_FAIL
         */
        private Boolean suspendReconnectOnProtocolFailure;

        /**
         * 客户端 socket 配置选项
         *
         * @see io.lettuce.core.SocketOptions
         */
        private SocketOptions socketOptions;

        /**
         * {@link io.lettuce.core.SslOptions.Builder#sslContext(Consumer)} 如有特殊情况，需自行编程实现（一般无需处理）。
         * <p>
         * 属性 Supplier&lt;SSLParameters&gt; sslParametersSupplier 由 {@link io.lettuce.core.SslOptions} 根据配置生成，无需处理。
         * <p>
         * 属性 KeystoreAction keymanager 由 {@link io.lettuce.core.SslOptions} 根据配置生成，无需处理。
         * <p>
         * 属性 KeystoreAction trustmanager 由 {@link io.lettuce.core.SslOptions} 根据配置生成，无需处理。
         *
         * @see io.lettuce.core.SslOptions
         * @see io.lettuce.core.SslOptions.Builder
         * @see io.netty.handler.ssl.SslContextBuilder
         */
        private SslOptions sslOptions;

        /**
         * 注意：{@link io.lettuce.core.TimeoutOptions.TimeoutSource} 不同的命令采用不同的超时配置，如有特殊需求，需自行编程实现
         *
         * @see io.lettuce.core.TimeoutOptions
         * @see io.lettuce.core.TimeoutOptions.Builder
         */
        private TimeoutOptions timeoutOptions;

        public Boolean getAutoReconnect() {
            return autoReconnect;
        }

        public void setAutoReconnect(Boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
        }

        public String getDisconnectedBehavior() {
            return disconnectedBehavior;
        }

        public void setDisconnectedBehavior(String disconnectedBehavior) {
            this.disconnectedBehavior = disconnectedBehavior;
        }

        public Boolean getPingBeforeActivateConnection() {
            return pingBeforeActivateConnection;
        }

        public void setPingBeforeActivateConnection(Boolean pingBeforeActivateConnection) {
            this.pingBeforeActivateConnection = pingBeforeActivateConnection;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public Boolean getPublishOnScheduler() {
            return publishOnScheduler;
        }

        public void setPublishOnScheduler(Boolean publishOnScheduler) {
            this.publishOnScheduler = publishOnScheduler;
        }

        public Integer getRequestQueueSize() {
            return requestQueueSize;
        }

        public void setRequestQueueSize(Integer requestQueueSize) {
            this.requestQueueSize = requestQueueSize;
        }

        public String getScriptCharset() {
            return scriptCharset;
        }

        public void setScriptCharset(String scriptCharset) {
            this.scriptCharset = scriptCharset;
        }

        public Boolean getSuspendReconnectOnProtocolFailure() {
            return suspendReconnectOnProtocolFailure;
        }

        public void setSuspendReconnectOnProtocolFailure(Boolean suspendReconnectOnProtocolFailure) {
            this.suspendReconnectOnProtocolFailure = suspendReconnectOnProtocolFailure;
        }

        public SocketOptions getSocketOptions() {
            return socketOptions;
        }

        public void setSocketOptions(SocketOptions socketOptions) {
            this.socketOptions = socketOptions;
        }

        public SslOptions getSslOptions() {
            return sslOptions;
        }

        public void setSslOptions(SslOptions sslOptions) {
            this.sslOptions = sslOptions;
        }

        public TimeoutOptions getTimeoutOptions() {
            return timeoutOptions;
        }

        public void setTimeoutOptions(TimeoutOptions timeoutOptions) {
            this.timeoutOptions = timeoutOptions;
        }
    }


    /**
     * 集群客户端配置选项
     * <p>
     * 注意： {@link io.lettuce.core.protocol.DecodeBufferPolicy} 需要编程实现，无法配置
     *
     * @see io.lettuce.core.cluster.ClusterClientOptions
     */
    public static class ClusterClientOptions extends ClientOptions {

        private Integer maxRedirects;

        private Boolean validateClusterNodeMembership;

        /**
         * 白名单
         */
        private Set<String> nodeFilter;

        private ClusterTopologyRefreshOptions topologyRefreshOptions;

        public Integer getMaxRedirects() {
            return maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }

        public Boolean getValidateClusterNodeMembership() {
            return validateClusterNodeMembership;
        }

        public void setValidateClusterNodeMembership(Boolean validateClusterNodeMembership) {
            this.validateClusterNodeMembership = validateClusterNodeMembership;
        }

        public Set<String> getNodeFilter() {
            return nodeFilter;
        }

        public void setNodeFilter(Set<String> nodeFilter) {
            this.nodeFilter = nodeFilter;
        }

        public ClusterTopologyRefreshOptions getTopologyRefreshOptions() {
            return topologyRefreshOptions;
        }

        public void setTopologyRefreshOptions(ClusterTopologyRefreshOptions topologyRefreshOptions) {
            this.topologyRefreshOptions = topologyRefreshOptions;
        }

    }


    public static class SocketOptions {

        /**
         * 默认值：10000， 单位：millisecond
         *
         * @see io.lettuce.core.SocketOptions#DEFAULT_CONNECT_TIMEOUT_DURATION
         */
        private Long connectTimeout;

        /**
         * 默认值：true
         *
         * @see io.lettuce.core.SocketOptions#DEFAULT_SO_NO_DELAY
         */
        private Boolean tcpNoDelay;

        /**
         * 默认值：false
         *
         * @see io.lettuce.core.SocketOptions#DEFAULT_SO_KEEPALIVE
         */
        private KeepAliveOptions keepAlive;

        /**
         * 默认值：false
         *
         * @see io.lettuce.core.SocketOptions#DEFAULT_TCP_USER_TIMEOUT_ENABLED
         */
        private TcpUserTimeoutOptions tcpUserTimeout;

        public Long getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Long connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Boolean getTcpNoDelay() {
            return tcpNoDelay;
        }

        public void setTcpNoDelay(Boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        public KeepAliveOptions getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(KeepAliveOptions keepAlive) {
            this.keepAlive = keepAlive;
        }

        public TcpUserTimeoutOptions getTcpUserTimeout() {
            return tcpUserTimeout;
        }

        public void setTcpUserTimeout(TcpUserTimeoutOptions tcpUserTimeout) {
            this.tcpUserTimeout = tcpUserTimeout;
        }
    }

    /**
     * @see io.lettuce.core.SocketOptions.KeepAliveOptions
     */
    public static class KeepAliveOptions {

        /**
         * 默认值：9
         */
        private Integer count;

        /**
         * 默认值：false
         */
        private Boolean enabled;

        /**
         * 默认值：7200000， 单位：millisecond
         */
        private Long idle;

        /**
         * 默认值：75000， 单位：millisecond
         */
        private Long interval;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Long getIdle() {
            return idle;
        }

        public void setIdle(Long idle) {
            this.idle = idle;
        }

        public Long getInterval() {
            return interval;
        }

        public void setInterval(Long interval) {
            this.interval = interval;
        }

    }

    /**
     * @see io.lettuce.core.SocketOptions.TcpUserTimeoutOptions
     */
    public static class TcpUserTimeoutOptions {

        /**
         * 默认值：false
         */
        private Boolean enabled;

        /**
         * 默认值：7875000 单位：millisecond
         *
         * @see io.lettuce.core.SocketOptions.TcpUserTimeoutOptions#DEFAULT_TCP_USER_TIMEOUT
         */
        private Long tcpUserTimeout;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Long getTcpUserTimeout() {
            return tcpUserTimeout;
        }

        public void setTcpUserTimeout(Long tcpUserTimeout) {
            this.tcpUserTimeout = tcpUserTimeout;
        }
    }


    public static class SslOptions {

        /**
         * JDK, OPENSSL,OPENSSL_REFCNT
         * <p>
         * 默认：SslProvider.JDK
         *
         * @see io.netty.handler.ssl.SslProvider
         */
        private String sslProvider;

        /**
         * 数字证书存档文件格式
         */
        private String keyStoreType;

        /**
         * URL
         */
        private String keystore;

        private String keystorePassword;

        /**
         * URL
         */
        private String truststore;

        private String truststorePassword;

        /**
         * 安全协议
         * <p>
         * 例如："TLSv1.3", "TLSv1.2"
         */
        private List<String> protocols;

        /**
         * 密码套件
         */
        private List<String> cipherSuites;

        /**
         * 握手超时时间，单位：millisecond
         */
        private Long handshakeTimeout;

        public String getSslProvider() {
            return sslProvider;
        }

        public void setSslProvider(String sslProvider) {
            this.sslProvider = sslProvider;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }

        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public String getKeystore() {
            return keystore;
        }

        public void setKeystore(String keystore) {
            this.keystore = keystore;
        }

        public String getKeystorePassword() {
            return keystorePassword;
        }

        public void setKeystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
        }

        public String getTruststore() {
            return truststore;
        }

        public void setTruststore(String truststore) {
            this.truststore = truststore;
        }

        public String getTruststorePassword() {
            return truststorePassword;
        }

        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

        public List<String> getProtocols() {
            return protocols;
        }

        public void setProtocols(List<String> protocols) {
            this.protocols = protocols;
        }

        public List<String> getCipherSuites() {
            return cipherSuites;
        }

        public void setCipherSuites(List<String> cipherSuites) {
            this.cipherSuites = cipherSuites;
        }

        public Long getHandshakeTimeout() {
            return handshakeTimeout;
        }

        public void setHandshakeTimeout(Long handshakeTimeout) {
            this.handshakeTimeout = handshakeTimeout;
        }
    }


    public static class TimeoutOptions {

        /**
         * 所有命令采用相同的超时时间，单位：millisecond
         * <p>
         * 配置此选项后，则 {@linkplain io.lettuce.core.TimeoutOptions.Builder#timeoutCommands()} 自动为true。
         */
        private Long fixedTimeout;

        public Long getFixedTimeout() {
            return fixedTimeout;
        }

        public void setFixedTimeout(Long fixedTimeout) {
            this.fixedTimeout = fixedTimeout;
        }

    }

    public static class ClusterTopologyRefreshOptions {

        private Set<String> adaptiveRefreshTriggers;

        private Long adaptiveRefreshTimeout;

        private Boolean closeStaleConnections;

        private Boolean dynamicRefreshSources;

        private Boolean periodicRefreshEnabled;

        private Long refreshPeriod;

        private Integer refreshTriggersReconnectAttempts;

        public Set<String> getAdaptiveRefreshTriggers() {
            return adaptiveRefreshTriggers;
        }

        public void setAdaptiveRefreshTriggers(Set<String> adaptiveRefreshTriggers) {
            this.adaptiveRefreshTriggers = adaptiveRefreshTriggers;
        }

        public Long getAdaptiveRefreshTimeout() {
            return adaptiveRefreshTimeout;
        }

        public void setAdaptiveRefreshTimeout(Long adaptiveRefreshTimeout) {
            this.adaptiveRefreshTimeout = adaptiveRefreshTimeout;
        }

        public Boolean getCloseStaleConnections() {
            return closeStaleConnections;
        }

        public void setCloseStaleConnections(Boolean closeStaleConnections) {
            this.closeStaleConnections = closeStaleConnections;
        }

        public Boolean getDynamicRefreshSources() {
            return dynamicRefreshSources;
        }

        public void setDynamicRefreshSources(Boolean dynamicRefreshSources) {
            this.dynamicRefreshSources = dynamicRefreshSources;
        }

        public Boolean getPeriodicRefreshEnabled() {
            return periodicRefreshEnabled;
        }

        public void setPeriodicRefreshEnabled(Boolean periodicRefreshEnabled) {
            this.periodicRefreshEnabled = periodicRefreshEnabled;
        }

        public Long getRefreshPeriod() {
            return refreshPeriod;
        }

        public void setRefreshPeriod(Long refreshPeriod) {
            this.refreshPeriod = refreshPeriod;
        }

        public Integer getRefreshTriggersReconnectAttempts() {
            return refreshTriggersReconnectAttempts;
        }

        public void setRefreshTriggersReconnectAttempts(Integer refreshTriggersReconnectAttempts) {
            this.refreshTriggersReconnectAttempts = refreshTriggersReconnectAttempts;
        }

    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "{", "}")
                .add("\"id\":\"" + id + "\"")
                .add("\"cluster\":" + cluster)
                .add("\"sentinel\":" + sentinel)
                .add("\"standalone\":" + standalone)
                .toString();
    }

}