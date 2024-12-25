package com.liboshuai.slr.framework.takeTime.core.aop;

import java.lang.annotation.*;

/**
 * 统计耗时
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TakeTime {
}