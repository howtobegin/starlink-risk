## 注意事项

本地idea启动项目时，需要添加vm参数：`-Djava.io.tmpdir=C:/Me/flink/temp`
，否则RockDB会报错无法创建临时目录文件。具体参考：https://juejin.cn/post/7450077044141654066