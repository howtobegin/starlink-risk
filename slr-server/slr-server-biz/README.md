## 注意事项

1. 项目密码均使用Jasypt进行加密，启动项目需要在JVM参数中指定加密密钥，例如：
    ```
    -Djasypt.encryptor.password=xxxxx
    ```
   > https://lv7lql7fxx8.feishu.cn/docx/CqEGdGIHkoN2rpxYp5dcyeu9nzf

2. 若分布式部署，需要设置程序参数`--workerId`，例如：
    ```
    java -jar --workerId=2
    ```
   > 注意是`程序参数`不是`jvm参数`，详细区别查看：https://juejin.cn/post/7452163912776302633