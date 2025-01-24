package com.liboshuai.slr.module.engine.function;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import com.liboshuai.slr.module.engine.dto.*;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DorisAsyncFunction extends RichAsyncFunction<MysqlCdcDTO, KafkaEventDTO> {

    private final ParameterTool parameterTool;
    // 连接池
    private DruidDataSource druidDataSource;
    // 线程池
    private ExecutorService executorService;

    public DorisAsyncFunction(ParameterTool parameterTool) {
        this.parameterTool = parameterTool;
    }

    @Override
    public void open(Configuration parameters) {
        String host = parameterTool.get(ParameterConstants.DORIS_FE_HOST);
        String queryPort = parameterTool.get(ParameterConstants.DORIS_FE_PORT_QUERY);
        String feNodes = host + DefaultConstants.COLON + queryPort;
        String username = parameterTool.get(ParameterConstants.DORIS_USERNAME);
        String password = parameterTool.get(ParameterConstants.DORIS_PASSWORD);
        String database = parameterTool.get(ParameterConstants.DORIS_DATABASE);
        // 创建连接池、配置连接参数
        druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        String url = String.format("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC", feNodes, database);
        druidDataSource.setUrl(url);
        // 其他必要的Druid配置可以在这里添加
        druidDataSource.setInitialSize(5);
        druidDataSource.setMinIdle(5);
        druidDataSource.setMaxActive(20);
        druidDataSource.setMaxWait(2000); // 连接超时时间，单位毫秒
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setValidationQuery("SELECT 1");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);

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
        String op = mysqlCdcDTO.getOp();
        // 规则下线时，查询doris创建数据清洗流
        if (!Envelope.Operation.DELETE.code().equals(op)) {
            resultFuture.complete(Collections.emptyList());
            return;
        }
        String tableName = parameterTool.get(ParameterConstants.DORIS_TABLE_STATE);
        // 执行异步操作
        executorService.submit(() -> {
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
            List<KafkaEventDTO> kafkaEventDTOList = new ArrayList<>();
            try (DruidPooledConnection connection = druidDataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         String.format("SELECT TARGET_VALUE FROM %s WHERE RULE_CODE = ? and RULE_VERSION = ? and CHANNEL =? and TARGET_FIELD = ?", tableName)
                 )) {
                // 设置参数
                preparedStatement.setLong(1, ruleInfoDTOBefore.getRuleCode());
                preparedStatement.setLong(2, ruleInfoDTOBefore.getRuleVersion());
                preparedStatement.setString(3, ruleInfoDTOBefore.getChannel());
                preparedStatement.setString(4, ruleInfoDTOBefore.getTargetField());
                // 执行SQL并获取结果
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String targetValue = resultSet.getString("TARGET_VALUE");
                        StateHistoryDTO stateHistoryDTO = StateHistoryDTO.builder()
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
                                .stateHistoryDTO(stateHistoryDTO)
                                .build();
                        kafkaEventDTOList.add(kafkaEventDTO);
                    }
                }
                // 提交结果
                resultFuture.complete(kafkaEventDTOList);
            } catch (Exception e) {
                log.error("Error processing asyncInvoke for RuleInfoDTO: {}", mysqlCdcDTO, e);
                // 可以根据需求选择是否提交空结果或有错误标识的结果
                resultFuture.completeExceptionally(e);
            }
        });
    }

    @Override
    public void close() {
        // 关闭连接池
        if (druidDataSource != null) {
            druidDataSource.close();
        }

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

