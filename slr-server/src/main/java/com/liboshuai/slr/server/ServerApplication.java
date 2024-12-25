package com.liboshuai.slr.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableMongoRepositories(basePackages = {"${slr.info.base-package}.server", "${slr.info.base-package}.module"})
@SpringBootApplication(scanBasePackages = {"${slr.info.base-package}.server", "${slr.info.base-package}.module"})
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
