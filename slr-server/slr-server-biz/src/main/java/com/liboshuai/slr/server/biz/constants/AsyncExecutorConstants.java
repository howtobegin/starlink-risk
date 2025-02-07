package com.liboshuai.slr.server.biz.constants;

/**
 * 异步线程池名称
 */
public interface AsyncExecutorConstants {
    /**
     * mock事件测试文件
     */
    String MOCK_EVENT_FILE_ASYNC_EXECUTOR = "mockEventFileAsyncExecutor";
    /**
     * 发送信息到荣数运营
     */
    String SEND_MSG_TO_RSO_ASYNC_EXECUTOR = "sendMsgToRsoAsyncExecutor";
    /**
     * 保存事件信息到mongo
     */
    String SAVE_EVENT_TO_MONGO_ASYNC_EXECUTOR = "saveEventToMongoAsyncExecutor";
}
