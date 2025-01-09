package com.liboshuai.slr.module.engine.utils;

import com.liboshuai.slr.module.engine.constants.FlinkEnvConstants;
import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import com.liboshuai.slr.module.engine.framework.exception.FlinkPropertiesException;
import com.liboshuai.slr.module.engine.framework.exception.FlinkPropertiesExceptionInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * author: liboshuai
 * description: 配置信息读取类
 * date: 2023
 */
@Slf4j
public class ParameterUtil {

    /**
     * 默认配置文件
     */
    private static final String DEFAULT_CONFIG = ParameterConstants.FLINK_ROOT_FILE;
    /**
     * 带环境配置文件
     */
    private static final String FLINK_ENV_FILE = ParameterConstants.FLINK_ENV_FILE;
    /**
     * 环境变量
     */
    private static final String ENV_ACTIVE = ParameterConstants.FLINK_ENV_ACTIVE;

    /**
     * author: liboshuai
     * description: 配置文件+启动参数+系统环境变量 生成ParameterTool
     *
     * @return org.apache.flink.api.java.utils.ParameterTool
     */
    public static ParameterTool getParameters(final String[] args) {

        /* **********************
         *
         * 知识点：
         *
         * 3.
         *
         * Java读取资源的方式：
         *
         * a. Class.getResourceAsStream(Path): Path 必须以 “/”，表示从ClassPath的根路径读取资源
         * b. Class.getClassLoader().getResourceAsStream(Path)：Path 无须以 “/”, 默认从ClassPath的根路径读取资源
         *
         * 推荐使用第2种,也就是类加载器的方式获取静态资源文件, 不要通过ClassPath的相对路径查找
         *
         * 4.
         * idea 运行时会将 src/main/resources 的配置文件复制到 target/classes
         *
         * *********************/

        InputStream inputStream = ParameterUtil.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG);

        try {
            //读取根配置文件
            ParameterTool defaultPropertiesFile =
                    ParameterTool.fromPropertiesFile(inputStream);

            //获取环境参数
            String envActive = getEnvActiveValue(defaultPropertiesFile);

            //读取真正的配置环境 (推荐使用 Thread.currentThread() 读取配置文件)

            return ParameterTool
                    // ParameterTool读取变量优先级 系统环境变量>启动参数变量>配置文件变量

                    // 从配置文件获取配置
                    .fromPropertiesFile(
                            //当前线程
                            Thread.currentThread()
                                    //返回该线程的上下文信息, 获取类加载器
                                    .getContextClassLoader()
                                    .getResourceAsStream(envActive))
                    // 从启动参数中获取配置
                    .mergeWith(ParameterTool.fromArgs(args))
                    // 从系统环境变量获取配置
                    .mergeWith(ParameterTool.fromSystemProperties());
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }

    }


    /**
     * author: liboshuai
     * description: 配置文件+系统环境变量 生成ParameterTool
     *
     * @param :
     * @return org.apache.flink.api.java.utils.ParameterTool
     */
    public static ParameterTool getParameters() {


        InputStream inputStream = ParameterUtil.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG);

        /* **********************
         *
         * 注意：
         *
         * ParameterTool 读取配置文件需要抛出 IOException,
         * IOException 的捕捉就在这里 catch
         *
         * 以前代码是直接抛出,没有进行catch,要注意对以前代码的修改
         *
         * *********************/

        try {
            ParameterTool defaultPropertiesFile =
                    ParameterTool.fromPropertiesFile(inputStream);

            //获取环境参数
            String envActive = getEnvActiveValue(defaultPropertiesFile);

            //读取真正的配置环境 (推荐使用 Thread.currentThread() 读取配置文件)
            return ParameterTool
                    // ParameterTool读取变量优先级 系统环境变量>启动参数变量>配置文件变量

                    // 从配置文件获取配置
                    .fromPropertiesFile(
                            //当前线程
                            Thread.currentThread()
                                    //返回该线程的上下文信息, 获取类加载器
                                    .getContextClassLoader()
                                    .getResourceAsStream(envActive))
                    // 从系统环境变量获取配置
                    .mergeWith(ParameterTool.fromSystemProperties());
        } catch (Exception e) {
            throw new FlinkPropertiesException(FlinkPropertiesExceptionInfo.PROPERTIES_NULL);
        }
    }

    /**
     * author: liboshuai
     * description: 获取环境配置变量
     *
     * @param defaultPropertiesFile:
     * @return java.lang.String
     */
    private static String getEnvActiveValue(ParameterTool defaultPropertiesFile) {
        //选择参数环境
        String envActive = null;
        if (defaultPropertiesFile.has(ENV_ACTIVE)) {
            envActive = String.format(FLINK_ENV_FILE, defaultPropertiesFile.get(ENV_ACTIVE));
        }

        return envActive;
    }

    /**
     * author: liboshuai
     * description: 从配置文件参数配置流式计算的上下文环境
     *
     * @param env:
     * @param parameterTool:
     * @return org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
     */
    public static void envWithConfig(
            StreamExecutionEnvironment env,
            ParameterTool parameterTool
    ) throws IOException, URISyntaxException {
        //并行度设置
        env.setParallelism(parameterTool.getInt(ParameterConstants.FLINK_PARALLELISM));
        // 启用 checkpoint，并设置时间间隔为 1 分钟
        env.enableCheckpointing(parameterTool.getInt(ParameterConstants.FLINK_CHECKPOINT_INTERVAL));
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        // checkpoint 必须在 1分钟内结束，否则被丢弃
        checkpointConfig.setCheckpointTimeout(parameterTool.getInt(ParameterConstants.FLINK_CHECKPOINT_TIMEOUT));
        // 作业手动取消时，保留 checkpoint 数据（需手动清理）
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // checkpoint 语义设置为 精确一致( EXACTLY_ONCE )
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        // 最多允许 checkpoint 失败 3 次
        checkpointConfig.setTolerableCheckpointFailureNumber(parameterTool.getInt(ParameterConstants.FLINK_CHECKPOINT_FAILURENUMBER));
        // 同一时间只允许一个 checkpoint 进行
        checkpointConfig.setMaxConcurrentCheckpoints(parameterTool.getInt(ParameterConstants.FLINK_CHECKPOINT_MAXCONCURRENT));
        // 设置 checkpoint 存储位置
        setupCheckpointStorage(parameterTool, checkpointConfig);
        //设置 StateBacked 为 rocksDB，并开启增量存储
        env.setStateBackend(new HashMapStateBackend());
    }

    /**
     * 配置 Flink 的 checkpoint 存储。
     *
     * @param parameterTool    参数工具，包含配置参数
     * @param checkpointConfig 检查点配置对象
     * @throws IOException 如果在本地环境下无法创建 checkpoint 目录
     */
    public static void setupCheckpointStorage(ParameterTool parameterTool, CheckpointConfig checkpointConfig)
            throws IOException, URISyntaxException {
        // 获取 checkpoint 存储的 URL
        String flinkCheckpointUrl = parameterTool.get(ParameterConstants.FLINK_CHECKPOINT_URL);
        if (flinkCheckpointUrl == null || flinkCheckpointUrl.isEmpty()) {
            throw new IllegalArgumentException("FLINK_CHECKPOINT_URL 参数未配置或为空");
        }
        // 获取 Flink 环境的激活状态
        String flinkEnvActive = parameterTool.get(ParameterConstants.FLINK_ENV_ACTIVE, FlinkEnvConstants.LOCAL);
        if (FlinkEnvConstants.LOCAL.equalsIgnoreCase(flinkEnvActive)) {
            // 本地环境，确保 checkpoint 目录存在
            Path checkpointPath = Paths.get(new URI(flinkCheckpointUrl));
            if (Files.notExists(checkpointPath)) {
                try {
                    Files.createDirectories(checkpointPath);
                    log.info("已成功创建 checkpoint 目录: {}", checkpointPath.toAbsolutePath());
                } catch (IOException e) {
                    log.error("无法创建 checkpoint 目录: {}", checkpointPath.toAbsolutePath(), e);
                    throw new IOException("无法创建 checkpoint 目录: " + checkpointPath.toAbsolutePath(), e);
                }
            } else {
                log.info("checkpoint 目录已存在: {}", checkpointPath.toAbsolutePath());
            }
        } else {
            // 非本地环境，设置 HDFS 的用户名
            String hdfsUsername = parameterTool.get(ParameterConstants.FLINK_CHECKPOINT_HDFS_USERNAME);
            if (hdfsUsername == null || hdfsUsername.isEmpty()) {
                throw new IllegalArgumentException("FLINK_CHECKPOINT_HDFS_USERNAME 参数在非本地环境下必须配置");
            }
            System.setProperty("HADOOP_USER_NAME", hdfsUsername);
            log.info("已设置 HADOOP_USER_NAME 为: {}", hdfsUsername);
        }
        // 配置 checkpoint 存储路径
        checkpointConfig.setCheckpointStorage(flinkCheckpointUrl);
        log.info("已设置 checkpoint 存储路径为: {}", flinkCheckpointUrl);
    }
}
