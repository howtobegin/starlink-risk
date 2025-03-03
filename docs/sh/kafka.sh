# 创建 starlink_risk_event topic，用于接收业务方的事件数据
./bin/kafka-topics.sh --create \
--bootstrap-server 10.152.143.105:9092 \
--replication-factor 3 \
--partitions 6 \
--topic starlink_risk_event_uat

# 创建 starlink_risk_alert topic，用于接收 flink 生成的告警数据
./bin/kafka-topics.sh --create \
--bootstrap-server 10.152.143.105:9092 \
--replication-factor 3 \
--partitions 6 \
--topic starlink_risk_alert_uat

# 创建 top 列表
./bin/kafka-topics.sh --list --bootstrap-server 10.152.143.105:9092

# 生产 数据到 topic 中
./bin/kafka-console-producer.sh --broker-list 10.152.143.105:9092 --topic starlink_risk_event_uat
./bin/kafka-console-producer.sh --broker-list 10.152.143.105:9092 --topic starlink_risk_alert_uat

# 消费 topic 中的数据
./bin/kafka-console-consumer.sh --bootstrap-server 10.152.143.105:9092 --topic starlink_risk_event_uat
./bin/kafka-console-consumer.sh --bootstrap-server 10.152.143.105:9092 --topic starlink_risk_alert_uat

# 查看指定 topic 所有分区的消息数量
./bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list 10.152.143.105:9092 --topic starlink_risk_event_uat --time -1
./bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list 10.152.143.105:9092 --topic starlink_risk_alert_uat --time -1
