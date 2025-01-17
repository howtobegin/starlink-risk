/home/lbs/software/flink/bin/flink run \
  -m one:8081 \
  -c com.liboshuai.slr.module.engine.EngineApplication \
  -Drest.flamegraph.enabled=true \
  /home/lbs/project/starlink-risk/slr-engine/slr-engine-biz/target/slr-engine-biz-1.0.jar
