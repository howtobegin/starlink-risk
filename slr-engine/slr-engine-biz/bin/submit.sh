/home/lbs/software/flink/bin/flink run \
  -m one:8081 \
  -c com.liboshuai.slr.module.engine.EngineApplication \
  -Dstate.backend.rocksdb.block.cache-size=512mb \
  -Dstate.backend.rocksdb.writebuffer.size=128mb \
  -Dstate.backend.rocksdb.writebuffer.count=4 \
  -Dstate.backend.rocksdb.thread.num=4 \
  -Dstate.backend.rocksdb.incremental-checkpointing=true \
  -Dstate.backend.rocksdb.metrics.enable=true \
  /home/lbs/project/starlink-risk/slr-engine/slr-engine-biz/target/slr-engine-biz-1.0.jar
