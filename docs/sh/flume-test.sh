#!/bin/bash

# 定义 Flume 配置相关的变量
FLUME_AGENT_NAME="slr_test"
FLUME_CONF_DIR="/home/lbs/software/flume/conf"
FLUME_CONF_FILE="${FLUME_CONF_DIR}/fileToKafka-test.properties"
FLUME_BIN="/home/lbs/software/flume/bin/flume-ng"
PROCESS_NAME="fileToKafka-test.properties"

start() {
    # 检查是否有相同的进程在运行
    if pgrep -f "${PROCESS_NAME}" > /dev/null; then
        echo "Flume agent '${FLUME_AGENT_NAME}' 已经在运行。"
        exit 1
    fi

    nohup ${FLUME_BIN} agent -c "${FLUME_CONF_DIR}" -f "${FLUME_CONF_FILE}" \
        -n "${FLUME_AGENT_NAME}" >/dev/null 2>&1 &

    echo "Flume agent '${FLUME_AGENT_NAME}' 已启动。"
}

stop() {
    PIDS=$(pgrep -f "${PROCESS_NAME}")

    if [ -n "${PIDS}" ]; then
        for pid in ${PIDS}; do
            echo "正在停止 Flume 进程，PID: ${pid}"
            kill ${pid}
        done

        # 等待进程停止
        for pid in ${PIDS}; do
            while kill -0 ${pid} 2>/dev/null; do
                echo "正在停止 Flume agent，PID: ${pid}"
                sleep 1
            done
        done

        echo "Flume agent 已停止。"
    else
        echo "未找到正在运行的 Flume agent。"
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

            echo "Flume agent 正在运行："
            echo "PID: ${pid}"
            echo "启动时间: ${START_TIME}"
            echo "启动用户: ${USER}"
            echo "运行时长: ${RUNNING_TIME}"
        done
    else
        echo "Flume agent 未在运行。"
    fi
}

help() {
    echo "用法: $0 {start|stop|restart|status|--help}"
    echo ""
    echo "命令:"
    echo "  start      启动 Flume agent"
    echo "  stop       停止 Flume agent"
    echo "  restart    重启 Flume agent"
    echo "  status     查看 Flume agent 状态"
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