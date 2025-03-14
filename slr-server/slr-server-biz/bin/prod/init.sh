#!/bin/bash

#******************************************************
# Author:               李博帅
# Date:                 2025年02月06日 14点15分
# Version:              1.0
# Desc:                 用于星链风控系统生产发布初始化
#******************************************************

cd ./data/spring

mkdir -p /app/dmapp/project/slr/log/dump
mkdir -p /app/dmapp/project/slr/log/gc
mkdir -p /app/dmapp/project/slr/bin
mkdir -p /app/dmapp/project/slr/jar
mkdir -p /app/dmapp/project/slr/bin
mkdir -p /app/dmapp/project/slr/data
mkdir -p /app/dmapp/project/slr/chg
mkdir -p /app/dmapp/project/slr/bak

cp -rf ./bin/slr-server.sh /app/dmapp/project/slr/bin/
cp -rf ./jar/slr-server-biz-1.0.jar /app/dmapp/project/slr/jar
# 注意：两台不同的机器需要修改脚本中的雪花算法id
chmod +x /app/dmapp/project/slr/bin/slr-server.sh

/app/dmapp/project/slr/bin/slr-server.sh start
