#!/bin/bash

#******************************************************
# Author:               李博帅
# Date:                 2025年02月06日 14点15分
# Version:              1.0
# Desc:                 用于星链风控系统uat后端服务管理
#******************************************************

# 定义变量
PROCESS_NAME="/app/dmapp/project/slr/jar/slr-server-biz-1.0.jar" # 进程名称
ROOT_PATH="/app/dmapp/project/slr" # 项目根路径
GC_LOG_PATH="${ROOT_PATH}/log/gc/service-gc.log" # jvm gc日志路径
HEAP_DUMP_PATH="${ROOT_PATH}/log/dump" # oom dump文件路径
JAR_PATH="${ROOT_PATH}/jar/slr-server-biz-1.0.jar" # jar包路径
WORKER_ID=1 # 雪花算法 workerId
DEBUG_PORT=5005


start() {
    # 检查是否有相同的进程在运行
    if pgrep -f "${PROCESS_NAME}" > /dev/null; then
        echo "应用已经在运行。"
        exit 1
    fi

    nohup java \
        -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="${DEBUG_PORT}" \
        -Xms1024m -Xmx2048m \
        -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled \
        -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark \
        -Xloggc:"${GC_LOG_PATH}" -XX:GCLogFileSize=100M \
        -XX:NumberOfGCLogFiles=1000 -XX:+UseGCLogFileRotation -XX:+PrintGCDateStamps \
        -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -verbose:gc -XX:+HeapDumpOnOutOfMemoryError \
        -XX:HeapDumpPath="${HEAP_DUMP_PATH}" \
        -jar "${JAR_PATH}" \
        --workerId="${WORKER_ID}" \
        >/dev/null 2>&1 &

    echo "应用已启动。"
}

stop() {
    PIDS=$(pgrep -f "${PROCESS_NAME}")

    if [ -n "${PIDS}" ]; then
        for pid in ${PIDS}; do
            echo "正在停止进程，PID: ${pid}"
            kill ${pid}
        done

        # 等待进程停止
        for pid in ${PIDS}; do
            while kill -0 ${pid} 2>/dev/null; do
                echo "正在停止应用，PID: ${pid}"
                sleep 1
            done
        done

        echo "应用已停止。"
    else
        echo "未找到正在运行的应用。"
    fi
}

restart() {
    stop
    start
}

status() {
    PIDS=$(pgrep -f "${PROCESS_NAME}")

    if [ -n "${PIDS}" ]; then
        for pid in ${PIDS}; do
            START_TIME=$(ps -o lstart= -p ${pid})
            RUNNING_TIME=$(ps -o etime= -p ${pid})
            USER=$(ps -o user= -p ${pid})

            echo "应用正在运行："
            echo "PID: ${pid}"
            echo "启动时间: ${START_TIME}"
            echo "启动用户: ${USER}"
            echo "运行时长: ${RUNNING_TIME}"
        done
    else
        echo "应用未在运行。"
    fi
}

help() {
    echo "用法: $0 {start|stop|restart|status|--help}"
    echo ""
    echo "命令:"
    echo "  start      启动应用"
    echo "  stop       停止应用"
    echo "  restart    重启应用"
    echo "  status     查看应用状态"
    echo "  --help     显示帮助信息"
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    --help)
        help
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status|--help}"
        exit 1
        ;;
esac