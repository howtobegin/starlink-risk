package com.liboshuai.slr.framework.takeTime.config;

import com.liboshuai.slr.framework.takeTime.core.annotaion.TakeTimeAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(TakeTimeAspect.class)
@AutoConfigureAfter(org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport.class)
public class SlrTakeTimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TakeTimeAspect takeTimeAspect() {
        return new TakeTimeAspect();
    }
}
