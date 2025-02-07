package com.liboshuai.slr.server.biz.framework.config;

import com.liboshuai.slr.server.biz.constants.AsyncExecutorConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class SlrServerAsyncConfig {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * mock事件测试文件
     */
    @Bean(name = AsyncExecutorConstants.MOCK_EVENT_FILE_ASYNC_EXECUTOR)
    public Executor mockEventFileAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        taskExecutor.setCorePoolSize(AVAILABLE_PROCESSORS * 2);
        // 线程池维护线程的最大数量，只有在缓冲队列满了以后才会申请超过核心线程数的线程
        taskExecutor.setMaxPoolSize(AVAILABLE_PROCESSORS * 4);
        //缓存队列
        taskExecutor.setQueueCapacity(500);
        //允许的空闲时间，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        taskExecutor.setKeepAliveSeconds(300);
        //异步方法内部线程名称
        taskExecutor.setThreadNamePrefix(AsyncExecutorConstants.MOCK_EVENT_FILE_ASYNC_EXECUTOR + "-");
        //拒绝策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }

    /**
     * 发送预警信息
     */
    @Bean(name = AsyncExecutorConstants.SEND_MSG_TO_RSO_ASYNC_EXECUTOR)
    public Executor sendAlertMsgAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        taskExecutor.setCorePoolSize(AVAILABLE_PROCESSORS * 2);
        // 线程池维护线程的最大数量，只有在缓冲队列满了以后才会申请超过核心线程数的线程
        taskExecutor.setMaxPoolSize(AVAILABLE_PROCESSORS * 4);
        //缓存队列
        taskExecutor.setQueueCapacity(500);
        //允许的空闲时间，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        taskExecutor.setKeepAliveSeconds(300);
        //异步方法内部线程名称
        taskExecutor.setThreadNamePrefix(AsyncExecutorConstants.SEND_MSG_TO_RSO_ASYNC_EXECUTOR + "-");
        //拒绝策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }

    /**
     * 保存事件信息到mongoDB
     */
    @Bean(name = AsyncExecutorConstants.SAVE_EVENT_TO_MONGO_ASYNC_EXECUTOR)
    public Executor saveEventToMongoAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        taskExecutor.setCorePoolSize(AVAILABLE_PROCESSORS);
        // 线程池维护线程的最大数量，只有在缓冲队列满了以后才会申请超过核心线程数的线程
        taskExecutor.setMaxPoolSize(AVAILABLE_PROCESSORS * 2);
        //缓存队列
        taskExecutor.setQueueCapacity(2000);
        //允许的空闲时间，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        taskExecutor.setKeepAliveSeconds(300);
        //异步方法内部线程名称
        taskExecutor.setThreadNamePrefix(AsyncExecutorConstants.SAVE_EVENT_TO_MONGO_ASYNC_EXECUTOR + "-");
        //拒绝策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }
}