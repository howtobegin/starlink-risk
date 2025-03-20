#!/bin/bash

#******************************************************
# Author:               李博帅
# Date:                 2025年03月03日 14点15分
# Version:              1.0
# Desc:                 用于星链风控系统生产前后端发布及回滚
#******************************************************

# 检查是否请求帮助
if [ "$1" == "--help" ]; then
    echo "用法: $0 <VERSION_ID> <ACTION>"
    echo
    echo "可用操作:"
    echo "  p   发布后端"
    echo "  r   回滚后端"
    echo
    echo "示例:"
    echo "  $0 2025010101 p"
    echo "  $0 2025010201 r"
    exit 0
fi

# 检查参数数量
if [ "$#" -lt 2 ]; then
    echo "用法: $0 <VERSION_ID> <ACTION>"
    echo "可用操作: p, r"
    exit 1
fi

# 版本号与行为
VERSION_ID=$1
ACTION=$2

# 定义路径变量
BASE_DIR="/app/dmapp/project/slr"
BACKUP_DIR="${BASE_DIR}/bak/slrServer${VERSION_ID}"
CHANGE_DIR="${BASE_DIR}/chg/slrServer${VERSION_ID}"

# 发布后端
publish() {
    echo "发布后端中..."
    mkdir -p "$BACKUP_DIR"
    rm -rf "$BACKUP_DIR/jar"
    mv -f "$BASE_DIR/jar" "$BACKUP_DIR" || { echo "备份后端包失败"; exit 1; }
    cp -rf "$CHANGE_DIR/jar" "$BASE_DIR/" || { echo "更新新后端包失败"; exit 1; }
    ${BASE_DIR}/bin/slr-server.sh restart || { echo "重启后端服务失败"; exit 1; }
}

# 回滚后端
rollback() {
    echo "回滚后端中..."
    rm -rf "$BASE_DIR/jar"
    cp -rf "$BACKUP_DIR/jar" "$BASE_DIR/" || { echo "恢复后端包失败"; exit 1; }
    ${BASE_DIR}/bin/slr-server.sh restart || { echo "重启后端服务失败"; exit 1; }
}

# 根据传入的操作执行相应的函数
case "$ACTION" in
    p)
        publish
        ;;
    r)
        rollback
        ;;
    *)
        echo "无效操作: $ACTION"
        echo "可用操作: p, r"
        exit 1
        ;;
esac

echo "操作完成。"
