# localhost 更换为对应 kafka IP
kafka-topics.sh --create \
--bootstrap-server localhost:9096,localhost:9097,localhost:9098 \
--replication-factor 3 \
--partitions 6 \
--topic slr_event

# localhost 更换为对应 kafka IP
kafka-topics.sh --create \
--bootstrap-server localhost:9096,localhost:9097,localhost:9098 \
--replication-factor 3 \
--partitions 6 \
--topic slr_warn
