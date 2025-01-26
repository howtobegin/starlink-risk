#!/bin/bash

flink_bin="/home/lbs/software/flink/bin/flink"
flink_name="slr-engine"
flink_main="com.liboshuai.slr.module.engine.EngineApplication"
flink_jar="/home/lbs/project/starlink-risk/slr-engine/slr-engine-biz/target/slr-engine-biz-1.0.jar"
flink_savepoint="hdfs:///flink/starlink-risk/slr-engine/savepoint"
savepoint_log="/home/lbs/project/starlink-risk/slr-engine/slr-engine-biz/savepoint.log"


function init() {
      echo -----------------------------------------------------------------------------------
      echo "------------------------------- ${flink_name} starting ---------------------------"
      echo -----------------------------------------------------------------------------------
      ${flink_bin} run-application \
      -p 10 \
      -t yarn-application \
      -Dyarn.application.name="${flink_name}" \
      -Dyarn.containers.vcores=10 \
      -Dtaskmanager.memory.managed.size=0m \
      -Drest.flamegraph.enabled=true \
      -Dstate.backend.latency-track.keyed-state-enabled=true \
      -c ${flink_main} ${flink_jar}
}

#function init() {
#      echo -----------------------------------------------------------------------------------
#      echo "------------------------------- ${flink_name} starting ---------------------------"
#      echo -----------------------------------------------------------------------------------
#      ${flink_bin} run-application -t yarn-application -Dyarn.application.name="${flink_name}" \
#      -Drest.flamegraph.enabled=true \
#      -Dstate.backend.local-recovery=true \
#      -Dstate.backend.latency-track.keyed-state-enabled=true \
#      -Dstate.backend.rocksdb.predefined-options=SPINNING_DISK_OPTIMIZED_HIGH_MEM \
#      -Dstate.backend.rocksdb.block.cache-size=512m \
#      -Dstate.backend.rocksdb.writebuffer.size=256m \
#      -Dstate.backend.rocksdb.compaction.level.max-size-level-base=640m \
#      -Dstate.backend.rocksdb.writebuffer.count=5 \
#      -Dstate.backend.rocksdb.thread.num=4 \
#      -Dstate.backend.rocksdb.writebuffer.number-to-merge=3 \
#      -Dstate.backend.rocksdb.memory.partitioned-index-filters=true \
#      -c ${flink_main} ${flink_jar}
#}

function cancal() {
    echo ---------------------------------------------------------------------------------
    echo "-------------------------------- ${flink_name} cancal -------------------------"
    echo ---------------------------------------------------------------------------------
    yarnId=`yarn application  -list | grep -w  ${flink_name} | awk '{print $1}' | grep application_`
    OUTPUT=$(/home/lbs/software/flink//bin/flink list -t yarn-application -Dyarn.application.id=$yarnId)
    JOB_ID=$(echo "$OUTPUT" | grep " : " | awk '{print $4}')
    ${flink_bin} cancel -t yarn-application -Dyarn.application.id=$yarnId $JOB_ID
}

function start() {
    echo -------------------------------------------------------------------------------------
    echo "-------------------------------- ${flink_name} start ------------------------------"
    echo -------------------------------------------------------------------------------------
    if [ -z "$1" ]; then
        if [ ! -f ${savepoint_log} ]; then
            echo "${savepoint_log} 文件不存在，请指定 savepoint 名称！"
            return 1
        fi
        savepointName=$(tail -n 1 ${savepoint_log} | awk -F'-->' '{print $2}')
        if [ -z "$savepointName" ]; then
            echo "${savepoint_log} 是空的或格式错误，请指定 savepoint 名称！"
            return 1
        fi
        echo "从 ${savepoint_log} 中读取的 savepoint 名称：$savepointName"
    else
        savepointName=$1
        echo "用户手动指定传入的 savepoint 名称：$savepointName"
    fi
    ${flink_bin} run-application -t yarn-application -Dyarn.application.name="${flink_name}" -s ${flink_savepoint}/$savepointName -c ${flink_main} ${flink_jar}
}

function stop() {
    echo "-----------------------------------------------------------------------------------------"
    echo "-------------------------------- ${flink_name} stop -------------------------------------"
    echo "-----------------------------------------------------------------------------------------"
    yarnId=`yarn application  -list | grep -w  ${flink_name} | awk '{print $1}' | grep application_`
    OUTPUT=$(/home/lbs/software/flink//bin/flink list -t yarn-application -Dyarn.application.id=$yarnId)
    JOB_ID=$(echo "$OUTPUT" | grep " : " | awk '{print $4}')
    STOP_RESULT=$(${flink_bin} stop --savepoint ${flink_savepoint} $JOB_ID -yid $yarnId)

    # 使用grep和awk提取savepoint路径
    savepointPath=$(echo "$STOP_RESULT" | grep -oP 'Savepoint completed. Path: .*/\K[^/]*$')

    # 获取当前时间
    currentTime=$(date '+%Y-%m-%d %H:%M:%S')

    # 将时间和savepoint路径写入文件
    echo "$currentTime-->${savepointPath}" >> ${savepoint_log}
    echo "Savepoint 创建并存储在: $savepointPath"
}

function status() {
    echo ---------------------------------------------------------------------------------
    echo "------------------------------ ${flink_name} status ----------------------------"
    echo ---------------------------------------------------------------------------------
    yarnId=`yarn application  -list | grep -w  ${flink_name} | awk '{print $1}' | grep application_`
    if [ "" = "$yarnId" ] ;then
        echo "${flink_name} not running!"
    else
        ${flink_bin} list -t yarn-application -Dyarn.application.id=$yarnId
    fi
}

function help() {
    echo "使用方法: slr-engine.sh 命令 [选项]"
    echo ""
    echo "命令:"
    echo "  init        在yarn应用模式下启动slr-engine项目，不从savepoint恢复。"
    echo "              通常用于项目首次部署。"
    echo "              示例: slr-engine.sh init"
    echo ""
    echo "  start       在yarn应用模式下启动slr-engine项目，并从指定的savepoint中恢复状态数据。"
    echo "              一般用于项目版本升级。（若不传入<savepoint名称>，则使用${savepoint_log}中最后一行数据）"
    echo "              示例: slr-engine.sh start <savepoint名称>"
    echo ""
    echo "  cancal      像正常关闭应用一样关闭项目，尽量完成正在处理的数据和任务，但不会生成savepoint，可能导致数据丢失。"
    echo "              示例: slr-engine.sh cancal"
    echo ""
    echo "  stop        优雅地停止slr-engine项目，并创建savepoint，将状态保存在指定的HDFS路径上。"
    echo "              通常用于项目迭代后的恢复。（同时将<savepoint名称>存入${savepoint_log}文件中的最后一行）"
    echo "              示例: slr-engine.sh stop"
    echo ""
    echo "  status      查看slr-engine项目的当前运行状态和信息。"
    echo "              示例: slr-engine.sh status"
    echo ""
    echo "  --help      显示此帮助信息并退出。"
    echo ""
    echo "请提供有效的命令以管理slr-engine项目。"
}

case "$1" in
    init)
        # 初始化项目，不从savepoint恢复
        init
        ;;
    start)
        # 启动项目，并尝试从指定的savepoint恢复
        start "$2"
        ;;
    stop)
        # 停止项目，并创建savepoint
        stop
        ;;
    cancal)
        # 取消项目运行，不创建savepoint
        cancal
        ;;
    status)
        # 显示项目的当前状态
        status
        ;;
    --help)
        # 显示帮助信息
        help
        ;;
    *)
        # 处理无效的命令
        echo "****************"
        echo "无效的命令: [$1]"
        echo "请使用 '--help' 查看所有有效命令。"
        echo "****************"
        ;;
esac
