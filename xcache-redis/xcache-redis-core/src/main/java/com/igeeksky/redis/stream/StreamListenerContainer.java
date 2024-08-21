package com.igeeksky.redis.stream;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.ByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 循环监听 Stream 消息
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/21
 */
public class StreamListenerContainer {

    private static final Logger log = LoggerFactory.getLogger(StreamListenerContainer.class);

    private final Map<ByteArray, ReadOffset> streams = Maps.newConcurrentHashMap();
    private final Map<ByteArray, Consumer<StreamMessage>> consumers = Maps.newConcurrentHashMap();

    private final long delay;
    private final ReadOptions options;
    private final RedisOperator operator;
    private final RedisStreamOperator streamOperator;

    private final ScheduledExecutorService scheduler;
    private final ExecutorService executor = createExecutor();

    private volatile Future<?> future;
    private volatile boolean running;
    private final Lock lock = new ReentrantLock();

    public StreamListenerContainer(ScheduledExecutorService scheduler, RedisOperatorFactory factory,
                                   long block, long count, long delay) {
        Assert.isTrue(count > 0, "count must be greater than 0");
        Assert.isTrue(delay > 0, "delay must be greater than 0");
        this.delay = delay;
        if (block >= 0) {
            this.options = ReadOptions.of(block, count);
        } else {
            this.options = ReadOptions.count(count);
        }
        this.scheduler = scheduler;
        this.operator = factory.getRedisOperator();
        this.streamOperator = factory.getRedisStreamOperator();
    }

    public RedisOperator getRedisOperator() {
        return operator;
    }

    public void register(ReadOffset offset, Consumer<StreamMessage> consumer) {
        Assert.notNull(offset, "offset must not be null");
        Assert.notNull(consumer, "consumer must not be null");

        ByteArray key = ByteArray.copyOf(offset.getKey());
        streams.put(key, offset);
        consumers.put(key, consumer);

        this.start();
    }

    public void start() {
        if (running) return;
        lock.lock();
        try {
            if (running) return;
            scheduler.scheduleWithFixedDelay(this::execute, 2, delay, TimeUnit.MILLISECONDS);
            running = true;
        } finally {
            lock.unlock();
        }
    }

    private void execute() {
        try {
            if (future != null && !future.isDone()) {
                return;
            }
            future = executor.submit(this::fetchMessages);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 批量检索多个流的消息并分发给指定的消费者
     * <p>
     * 首先检查流是否开放以及是否有可用的流，然后收集偏移量以用于从流中读取消息。
     * 如果消息不为空，则将消息分发给相应的消费者，并更新读取偏移量。
     */
    private void fetchMessages() {
        try {
            // 检查流链接是否打开，如果没有打开，则直接返回。
            if (!streamOperator.isOpen()) {
                return;
            }
            // 检查是否有可用的流，如果没有，则直接返回。
            if (streams.isEmpty()) {
                return;
            }
            // 创建一个列表来存储所有流的读取偏移量。
            List<ReadOffset> list = new ArrayList<>(streams.size());
            // 遍历流集合，将每个流的偏移量添加到列表中。
            streams.forEach((key, offset) -> list.add(offset));
            // 将列表转换为数组，以用于后续的读取操作。
            ReadOffset[] offsets = list.toArray(new ReadOffset[0]);

            // 从流中读取消息。
            List<StreamMessage> messages = streamOperator.xread(options, offsets);

            // 如果消息列表为空，则直接返回。
            if (CollectionUtils.isEmpty(messages)) {
                return;
            }

            // 遍历每个消息，将消息分发给相应的消费者，并更新读取偏移量。
            for (StreamMessage message : messages) {
                // 从消息中提取键。
                ByteArray key = ByteArray.of(message.key());
                // 获取对应键的消费者，并将消息传递给它。
                Consumer<StreamMessage> consumer = consumers.get(key);
                if (consumer != null) {
                    consumer.accept(message);
                }
                // 获取对应键的当前读取偏移量，并更新为新消息的偏移量。
                ReadOffset offset = streams.get(key);
                if (offset != null) {
                    streams.put(key, ReadOffset.from(offset.getKey(), message.id()));
                }
            }
        } catch (Throwable e) {
            // 捕获任何异常，并记录错误信息。
            log.error(e.getMessage(), e);
        }
    }

    private static ExecutorService createExecutor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("listener-container-thread-"));
    }

}