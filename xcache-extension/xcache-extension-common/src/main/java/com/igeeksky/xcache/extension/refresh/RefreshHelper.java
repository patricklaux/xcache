package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xtool.core.concurrent.Futures;
import com.igeeksky.xtool.core.function.tuple.Tuple2;

import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * 刷新工具类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/12/10
 */
public class RefreshHelper {

    /**
     * 私有构造器
     */
    private RefreshHelper() {
    }

    /**
     * 检查上次刷新任务队列是否已经全部执行完成
     *
     * @return {@code true} - 刷新队列已完成； {@code false} - 刷新队列未完成
     */
    public static boolean tasksUnfinished(ArrayList<Tuple2<Future<?>[], Integer>> tasksList) {
        int size = tasksList.size();
        for (int i = 0; i < size; i++) {
            Tuple2<Future<?>[], Integer> tuple = tasksList.get(i);
            if (tuple != null) {
                Future<?>[] futures = tuple.getT1();
                int last = Futures.checkAll(tuple.getT2(), futures);
                if (last < futures.length) {
                    tasksList.set(i, tuple.mapT2(index -> last));
                    return true;
                }
                tasksList.set(i, null);
            }
        }
        tasksList.clear();
        return false;
    }

}
