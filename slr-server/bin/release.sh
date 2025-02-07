#!/bin/bash

#******************************************************
# Author:               李博帅
# Date:                 2024年11月14日 18点10分
# Version:              1.0
# Desc:                 用于南极星系统生产前后端发布及回滚
#******************************************************

# 检查是否请求帮助
if [ "$1" == "--help" ]; then
    echo "用法: $0 <VERSION_ID> <ACTION>"
    echo
    echo "可用操作:"
    echo "  publish_backend    发布后端"
    echo "  publish_frontend   发布前端"
    echo "  publish_all       同时发布后端和前端"
    echo "  rollback_backend   回滚后端"
    echo "  rollback_frontend  回滚前端"
    echo "  rollback_all      同时回滚后端和前端"
    echo
    echo "示例:"
    echo "  $0 2024010101 publish_backend"
    echo "  $0 2024010201 rollback_all"
    exit 0
fi

# 检查参数数量
if [ "$#" -lt 2 ]; then
    echo "用法: $0 <VERSION_ID> <ACTION>"
    echo "可用操作: publish_backend, publish_frontend, publish_all, rollback_backend, rollback_frontend, rollback_all"
    exit 1
fi

# 版本号与行为
VERSION_ID=$1
ACTION=$2

# 定义路径变量
BASE_DIR="/app/dmapp/njstar"
BACKUP_DIR="/app/dmapp/bak/njstar${VERSION_ID}"
CHANGE_DIR="/app/dmapp/chg/njstar${VERSION_ID}"

# 发布后端
publish_backend() {
    echo "发布后端中..."
    mkdir -p "$BACKUP_DIR"
    rm -rf "$BACKUP_DIR/backend"
    mv -f "$BASE_DIR/backend" "$BACKUP_DIR" || { echo "备份后端包失败"; exit 1; }
    cp -rf "$CHANGE_DIR/backend" "$BASE_DIR/" || { echo "更新新后端包失败"; exit 1; }
    ${BASE_DIR}/bin/backend.sh restart || { echo "重启后端服务失败"; exit 1; }
}

# 发布前端
publish_frontend() {
    echo "发布前端中..."
    mkdir -p "$BACKUP_DIR"
    rm -rf "$BACKUP_DIR/frontend"
    mv -f "$BASE_DIR/frontend" "$BACKUP_DIR" || { echo "备份前端包失败"; exit 1; }
    cp -rf "$CHANGE_DIR/frontend" "$BASE_DIR/" || { echo "更新新前端包失败"; exit 1; }
}

# 回滚后端
rollback_backend() {
    echo "回滚后端中..."
    rm -rf "$BASE_DIR/backend"
    cp -rf "$BACKUP_DIR/backend" "$BASE_DIR/" || { echo "恢复后端包失败"; exit 1; }
    ${BASE_DIR}/bin/backend.sh restart || { echo "重启后端服务失败"; exit 1; }
}

# 回滚前端
rollback_frontend() {
    echo "回滚前端中..."
    rm -rf "$BASE_DIR/frontend"
    cp -rf "$BACKUP_DIR/frontend" "$BASE_DIR/" || { echo "恢复前端包失败"; exit 1; }
}

# 根据传入的操作执行相应的函数
case "$ACTION" in
    publish_backend)
        publish_backend
        ;;
    publish_frontend)
        publish_frontend
        ;;
    publish_all)
        publish_backend
        publish_frontend
        ;;
    rollback_backend)
        rollback_backend
        ;;
    rollback_frontend)
        rollback_frontend
        ;;
    rollback_all)
        rollback_backend
        rollback_frontend
        ;;
    *)
        echo "无效操作: $ACTION"
        echo "可用操作: publish_backend, publish_frontend, publish_all, rollback_backend, rollback_frontend, rollback_all"
        exit 1
        ;;
esac

echo "操作完成。"
