# 创建 slr_event topic，用于接收业务方的事件数据
kafka-topics.sh --create \
--bootstrap-server #{kafka主机IP}:9096,#{kafka主机IP}:9097,#{kafka主机IP}:9098 \
--replication-factor 3 \
--partitions 6 \
--topic slr_event

# 创建 slr_alert topic，用于接收 flink 生成的告警数据
kafka-topics.sh --create \
--bootstrap-server #{kafka主机IP}:9096,#{kafka主机IP}:9097,#{kafka主机IP}:9098 \
--replication-factor 3 \
--partitions 6 \
--topic slr_alert

# 创建 top 列表
kafka-topics.sh --list --bootstrap-server #{kafka主机IP}:9096,#{kafka主机IP}:9097,#{kafka主机IP}:9098

# 向 slr_event topic 发送单条数据，模拟业务方数据生产
kafka-console-producer.sh --broker-list #{kafka主机IP}:9096,#{kafka主机IP}:9097,#{kafka主机IP}:9098 --topic slr_event

# 消费 slr_alert top 数据，查看flink 生成的告警数据
kafka-console-consumer.sh --bootstrap-server #{kafka主机IP}:9096,#{kafka主机IP}:9097,#{kafka主机IP}:9098 --topic slr_alert
