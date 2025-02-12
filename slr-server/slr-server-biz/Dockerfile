FROM eclipse-temurin:8-jre

## 创建目录，并使用它作为工作目录
RUN mkdir -p /slr-server/{logs,data}
WORKDIR /slr-server
## 将后端项目的 Jar 文件，复制到镜像中
COPY ../target/slr-server-1.0.jar app.jar

## 设置 TZ 时区
ENV TZ=Asia/Shanghai
## 设置 JAVA_OPTS 环境变量，可通过 docker run -e "JAVA_OPTS=" 进行覆盖
ENV JAVA_OPTS="-Xms8g -Xmx8g"

## 应用参数
ENV ARGS=""

## 暴露后端项目的 48888 端口 和 xxl-job 执行执行器端口
EXPOSE 48888
EXPOSE 9999

## 挂载目录
VOLUME /slr-server/logs/
VOLUME /slr-server/data/

## 启动后端项目
CMD java ${JAVA_OPTS} -jar app.jar $ARGS