#!/bin/bash

mkdir -p /app/rongshu/slr/bin
mkdir -p /app/rongshu/slr/jar
mkdir -p /app/rongshu/slr/log

hadoop fs -mkdir -p /user/rongshu/starlink_risk/checkpoint
hadoop fs -mkdir -p /user/rongshu/starlink_risk/savepoint