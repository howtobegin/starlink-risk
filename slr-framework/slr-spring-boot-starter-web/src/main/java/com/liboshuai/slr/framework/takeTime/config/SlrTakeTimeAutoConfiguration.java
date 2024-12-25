package com.liboshuai.slr.framework.takeTime.config;

import com.liboshuai.slr.framework.takeTime.core.annotaion.TakeTimeAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SlrTakeTimeAutoConfiguration {

    @Bean
    public TakeTimeAspect takeTimeAspect() {
        return new TakeTimeAspect();
    }
}
