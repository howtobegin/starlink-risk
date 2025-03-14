# 10.150.9.124 -> PCS0382
# 10.150.9.146 -> PCS0230
# 10.150.9.147 -> PCS0231

# 创建 starlink_risk_event topic，用于接收业务方的事件数据
./bin/kafka-topics.sh --create \
--bootstrap-server 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 \
--replication-factor 3 \
--partitions 6 \
--topic starlink_risk_event_prod

# 创建 starlink_risk_alert topic，用于接收 flink 生成的告警数据
./bin/kafka-topics.sh --create \
--bootstrap-server 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 \
--replication-factor 3 \
--partitions 6 \
--topic starlink_risk_alert_prod

# 创建 top 列表
./bin/kafka-topics.sh --list --bootstrap-server 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 | grep starlink_risk

# 生产 数据到 topic 中
./bin/kafka-console-producer.sh --broker-list 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 --topic starlink_risk_event_prod
./bin/kafka-console-producer.sh --broker-list 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 --topic starlink_risk_alert_prod

# 消费 topic 中的数据
./bin/kafka-console-consumer.sh --bootstrap-server 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 --topic starlink_risk_event_prod
./bin/kafka-console-consumer.sh --bootstrap-server 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 --topic starlink_risk_alert_prod

# 查看指定 topic 所有分区的消息数量
./bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 --topic starlink_risk_event_prod --time -1
./bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list 10.150.9.124:51923,10.150.9.146:51923,10.150.9.147:51923 --topic starlink_risk_alert_prod --time -1
