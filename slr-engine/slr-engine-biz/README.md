# 启动项目

## 本地环境idea

1. 项目根目录下执行命令，安装`flink-doris-connector`到本地maven仓库
    ```shell
    mvn install:install-file -DgroupId=org.apache.doris -DartifactId=flink-connector-doris_2.12 -Dversion=1.14_2.12-1.1.1 -Dpackaging=jar -Dfile=slr-engine/slr-engine-biz/libs/flink-doris-connector-1.14_2.12-1.1.1.jar
    ```

2. idea 添加vm参数：`-Djava.io.tmpdir=C:/Me/flink/temp`（目录要提前创建）
   > 目的是解决RockDB会报错无法创建临时目录文件，具体参考：https://juejin.cn/post/7450077044141654066

3. 项目根目录下执行命令，进行maven install
    ```shell
    mvn clean install -P local -Dmaven.test.skip=true
    ```

4. 找到`com.liboshuai.slr.module.engine.EngineApplication`类的main方法，右键run

## 开发环境yarn

1. 登录yarn服务器，拉取项目
    ```shell
    git clone https://github.com/liboshuai01/starlink-risk.git
    ```

2. 项目根目录下执行命令，安装`flink-doris-connector`到本地maven仓库
    ```shell
    mvn install:install-file -DgroupId=org.apache.doris -DartifactId=flink-connector-doris_2.12 -Dversion=1.14_2.12-1.1.1 -Dpackaging=jar -Dfile=slr-engine/slr-engine-biz/libs/flink-doris-connector-1.14_2.12-1.1.1.jar
    ```
3. 项目根目录下执行命令，进行maven install
    ```shell
    mvn clean install -P test -Dmaven.test.skip=true
    ```

4. 任意目录下执行目录，创建savepoint、checkpoint的hdfs目录
    ```shell
    hadoop fs -mkdir -p /flink/starlink-risk/slr-engine/savepoint
    hadoop fs -mkdir -p /flink/starlink-risk/slr-engine/checkpoint
    ```

5. 项目根目录下执行命令，提交jar包到yarn集群运行
    ```shell
    bash ./slr-engine/slr-engine-biz/bin/slr-engine.sh init
    ```