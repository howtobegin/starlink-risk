FROM eclipse-temurin:8-jre

## 创建目录，并使用它作为工作目录
RUN mkdir -p /slr-admin/{logs,data}
WORKDIR /slr-admin
## 将后端项目的 Jar 文件，复制到镜像中
COPY ./target/slr-admin-biz-1.0.jar app.jar

## 设置 TZ 时区
ENV TZ=Asia/Shanghai
## 设置 JAVA_OPTS 环境变量，可通过 docker run -e "JAVA_OPTS=" 进行覆盖
ENV JAVA_OPTS="-Xms8g -Xmx8g"

## 应用参数
ENV ARGS=""

## 暴露后端项目的 30000 端口
EXPOSE 30010

## 挂载目录
VOLUME /slr-admin/logs/
VOLUME /slr-admin/data/

## 启动后端项目
CMD java ${JAVA_OPTS} -jar app.jar $ARGS