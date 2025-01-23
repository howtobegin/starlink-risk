package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.engine.dto.*;
import com.liboshuai.slr.module.engine.utils.RedisUtil;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.CollectionUtil;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisAsyncFunction extends RichAsyncFunction<MysqlCdcDTO, KafkaEventDTO> {
    /**
     * 线程池
     */
    private ExecutorService executorService;

    @Override
    public void open(Configuration parameters) {
        // 创建线程池，用于执行异步操作
        executorService = new ThreadPoolExecutor(
                5,
                15,
                1,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Override
    public void asyncInvoke(MysqlCdcDTO mysqlCdcDTO, ResultFuture<KafkaEventDTO> resultFuture) {
        // 执行异步操作
        executorService.submit(() -> {
            try {
                String op = mysqlCdcDTO.getOp();
                // 只在规则下线时，查询doris创建数据清洗流
                if (!Envelope.Operation.DELETE.code().equals(op)) {
                    resultFuture.complete(Collections.emptyList());
                    return;
                }
                // 变更之前的数据
                RuleJsonDTO ruleCdcDTOBefore = JsonUtils.parseObject(mysqlCdcDTO.getBefore(), RuleJsonDTO.class);
                if (Objects.isNull(ruleCdcDTOBefore)) {
                    ruleCdcDTOBefore = new RuleJsonDTO();
                }
                String ruleJsonBefore = ruleCdcDTOBefore.getRuleJson();
                RuleInfoDTO ruleInfoDTOBefore = JsonUtils.parseObject(ruleJsonBefore, RuleInfoDTO.class);
                if (Objects.isNull(ruleInfoDTOBefore)) {
                    log.warn("下线清理旧状态失败，MysqlCdc规则信息ruleInfoDTOBefore必须非空！");
                    // 提交空结果以防止 Flink 流处理阻塞
                    resultFuture.complete(Collections.emptyList());
                    return;
                }
                // 构建 redis key
                String redisKey = String.join(RedisKeyConstants.REDIS_KEY_SPLIT,
                        RedisKeyConstants.REDIS_KEY_PREFIX,
                        ruleInfoDTOBefore.getRuleCode().toString(),
                        ruleInfoDTOBefore.getRuleVersion().toString(),
                        ruleInfoDTOBefore.getChannel(),
                        ruleInfoDTOBefore.getTargetField());
                // 获取redis中key对应的value
                // TODO: 如果后续单key过大，需要拆分key
                Set<String> targetValueList = RedisUtil.smembers(redisKey);
                List<KafkaEventDTO> kafkaEventDTOList = new ArrayList<>();
                if (CollectionUtil.isNullOrEmpty(targetValueList)) {
                    log.warn("下线清理旧状态失败，redis中key为[{}]没有值！", redisKey);
                    resultFuture.complete(Collections.emptyList());
                    return;
                }
                for (String targetValue : targetValueList) {
                    RuleKeyHistoryDTO ruleKeyHistoryDTO = RuleKeyHistoryDTO.builder()
                            .ruleCode(ruleInfoDTOBefore.getRuleCode())
                            .ruleVersion(ruleInfoDTOBefore.getRuleVersion())
                            .channel(ruleInfoDTOBefore.getChannel())
                            .targetField(ruleInfoDTOBefore.getTargetField())
                            .targetValue(targetValue)
                            .build();
                    KafkaEventDTO kafkaEventDTO = KafkaEventDTO.builder()
                            .channel(ruleInfoDTOBefore.getChannel())
                            .targetField(ruleInfoDTOBefore.getTargetField())
                            .targetValue(targetValue)
                            .ruleKeyHistoryDTO(ruleKeyHistoryDTO)
                            .build();
                    kafkaEventDTOList.add(kafkaEventDTO);
                }
                resultFuture.complete(kafkaEventDTOList);
            } catch (Exception e) {
                log.error("AsyncRedisFunction 异步任务执行失败，异常: {}", e.getMessage(), e);
                resultFuture.complete(Collections.emptyList());
            }
        });
    }

    @Override
    public void close() {
        // 关闭线程池
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

