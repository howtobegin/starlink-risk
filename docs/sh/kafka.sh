# 创建 starlink_risk_event topic，用于接收业务方的事件数据
./bin/kafka-topics.sh --create \
--bootstrap-server localhost:9092 \
--replication-factor 3 \
--partitions 6 \
--topic starlink_risk_event_local

# 创建 starlink_risk_alert topic，用于接收 flink 生成的告警数据
./bin/kafka-topics.sh --create \
--bootstrap-server localhost:9092 \
--replication-factor 3 \
--partitions 6 \
--topic starlink_risk_alert_local

# 创建 top 列表
./bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# 向 slr_event topic 发送单条数据，模拟业务方数据生产
./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic starlink_risk_event_local

# 消费 slr_alert top 数据，查看flink 生成的告警数据
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic starlink_risk_alert_local

# 查看指定 topic 所有分区的消息数量
./bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic starlink_risk_event_local --time -1
