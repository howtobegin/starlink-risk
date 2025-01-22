package com.liboshuai.slr.module.admin.framework.config.executor;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminAsyncConfig {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();


//    @Bean(name = AsyncExecutorConstants.SEND_ALERT_MSG_ASYNC_EXECUTOR)
//    public Executor sendAlertMessageAsyncConfig() {
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        //设置核心线程数
//        taskExecutor.setCorePoolSize(AVAILABLE_PROCESSORS);
//        // 线程池维护线程的最大数量，只有在缓冲队列满了以后才会申请超过核心线程数的线程
//        taskExecutor.setMaxPoolSize(AVAILABLE_PROCESSORS * 2);
//        //缓存队列
//        taskExecutor.setQueueCapacity(500);
//        //允许的空闲时间，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
//        taskExecutor.setKeepAliveSeconds(300);
//        //异步方法内部线程名称
//        taskExecutor.setThreadNamePrefix(AsyncExecutorConstants.SEND_ALERT_MSG_ASYNC_EXECUTOR + "-");
//        //拒绝策略
//        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        taskExecutor.initialize();
//        return taskExecutor;
//    }
}