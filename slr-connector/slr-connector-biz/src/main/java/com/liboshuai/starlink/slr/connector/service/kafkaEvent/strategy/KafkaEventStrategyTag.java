package com.liboshuai.starlink.slr.connector.service.kafkaEvent.strategy;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaEventStrategyTag {
    String[] channels();
}