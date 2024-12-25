package com.liboshuai.starlink.slr.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${slr.info.base-package}
@SpringBootApplication(scanBasePackages = {"${slr.info.base-package}.server", "${slr.info.base-package}.admin", "${slr.info.base-package}.connector"})
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
