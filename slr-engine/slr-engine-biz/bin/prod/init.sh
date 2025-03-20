#!/bin/bash

cd ./data/flink

mkdir -p /app/rongshu/slr/bin
mkdir -p /app/rongshu/slr/jar
mkdir -p /app/rongshu/slr/log
mkdir -p /app/rongshu/slr/bak
mkdir -p /app/rongshu/slr/chg

hadoop fs -mkdir -p /user/rongshu/starlink_risk/checkpoint
hadoop fs -mkdir -p /user/rongshu/starlink_risk/savepoint

cp ./bin/slr-engine.sh /app/rongshu/slr/bin
cp ./jar/slr-engine-biz-1.0.jar /app/rongshu/slr/jar

chmod +x /app/rongshu/slr/bin/slr-engine.sh
/app/rongshu/slr/bin/slr-engine.sh init
