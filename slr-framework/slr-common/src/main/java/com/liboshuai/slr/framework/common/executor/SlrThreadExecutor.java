package com.liboshuai.slr.framework.common.executor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * 这是{@link ThreadPoolTaskExecutor}的一个简单替换，可搭配TransmittableThreadLocal实现父子线程之间的数据传递
 *
 * @author liboshuai
 * @version 1.0
 */
public class SlrThreadExecutor extends ThreadPoolExecutor {

    public SlrThreadExecutor(int core, int max, int queueCapacity, String name) {
        this(
                core,
                max,
                0L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactoryBuilder().setNamePrefix(name).build(),
                new AbortPolicy()
        );
    }

    public SlrThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    @Override
    public void execute(Runnable runnable) {
        Runnable ttlRunnable = TtlRunnable.get(runnable);
        super.execute(ttlRunnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Callable<T> ttlCallable = TtlCallable.get(task);
        return super.submit(ttlCallable);
    }

    @Override
    public Future<?> submit(Runnable task) {
        Runnable ttlRunnable = TtlRunnable.get(task);
        return super.submit(ttlRunnable);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Runnable ttlRunnable = TtlRunnable.get(task);
        return super.submit(ttlRunnable, result);
    }

}

