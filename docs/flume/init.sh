#!/bin/bash

cd ./data/flume

mkdir -p /app/dmapp/software
tar -zxvf flume-1.11.0.tar.gz -C /app/dmapp/software

mkdir -p /app/dmapp/software/flume/logs
mkdir -p /app/dmapp/software/flume/data/{properties,position}

cp -rf ./bin/flume-prod.sh /app/dmapp/software/flume/bin
chmod +x /app/dmapp/software/flume/bin/flume-prod.sh

cp -rf ./conf/log4j2.xml /app/dmapp/software/flume/conf
cp -rf ./conf/flume-env.sh /app/dmapp/software/flume/conf

cp -rf ./data/properties/fileToKafka.properties /app/dmapp/software/flume/data/properties

cp -rf ./lib/slr-flume-1.0.jar /app/dmapp/software/flume/lib

/app/dmapp/software/flume/bin/flume-prod.sh start

tail -fn 1000 /app/dmapp/software/flume/logs/flume.log
