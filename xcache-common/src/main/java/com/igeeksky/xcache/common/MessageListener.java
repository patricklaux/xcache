package com.igeeksky.xcache.common;

import java.util.function.Consumer;

/**
 * 消息监听者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface MessageListener<T> extends Consumer<T> {

}
