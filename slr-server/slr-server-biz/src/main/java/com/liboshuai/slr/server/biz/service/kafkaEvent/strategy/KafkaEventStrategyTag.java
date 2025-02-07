package com.liboshuai.slr.server.biz.service.kafkaEvent.strategy;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaEventStrategyTag {
    String[] channels();
}