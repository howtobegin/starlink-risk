#!/bin/bash

# **********************************************************
# * Author: boshuai.li
# * Create Time: 2025-02-18 14:58:27
# * Description: 该脚本用于删除指定目录中的旧日志文件，仅保留最近两天（今天和昨天）的日志。
# **********************************************************

# 日志目录路径
LOG_DIR="/home/lbs/docker/nginx/logs"

# 日志文件前缀
LOG_PREFIX="slr_event_local_"

# 获取今天和昨天的日期（格式：YYYY-MM-DD）
TODAY=$(date +%Y-%m-%d)
YESTERDAY=$(date -d "1 day ago" +%Y-%m-%d)

# 启用 nullglob，避免无匹配文件时报错
shopt -s nullglob

echo "开始清理日志文件，日志目录：$LOG_DIR"
echo "保留日志日期：$TODAY 和 $YESTERDAY"

# 遍历匹配的日志文件
for file in "$LOG_DIR"/"$LOG_PREFIX"*.json; do
    # 提取文件名中的日期部分
    filename=$(basename "$file")
    date_part=${filename#"$LOG_PREFIX"}
    date_part=${date_part%.json}

    # 判断是否需要删除
    if [[ "$date_part" != "$TODAY" && "$date_part" != "$YESTERDAY" ]]; then
        echo "删除旧日志文件：$file"
        rm -- "$file"
    else
        echo "保留日志文件：$file"
    fi
done

# 恢复 nullglob 设置
shopt -u nullglob

echo "日志清理完成"

# crontab
# 0 1 * * * /home/lbs/docker/nginx/script/cleanup_nginx_logs.sh >> /home/lbs/docker/nginx/logs/log_cleanup.log 2>&1