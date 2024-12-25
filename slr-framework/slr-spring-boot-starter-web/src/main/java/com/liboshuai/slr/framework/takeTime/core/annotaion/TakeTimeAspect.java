package com.liboshuai.slr.framework.takeTime.core.annotaion;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Aspect
public class TakeTimeAspect {
    //统计请求的处理时间
    TransmittableThreadLocal<Long> startTime = new TransmittableThreadLocal<>();
    TransmittableThreadLocal<Long> endTime = new TransmittableThreadLocal<>();

    /**
     * 带有@TakeTime注解的方法
     */
    @Pointcut("@annotation(com.liboshuai.slr.framework.takeTime.core.aop.TakeTime)")
    public void TakeTime() {

    }

    @Before("TakeTime()")
    public void doBefore(JoinPoint joinPoint) {
        // 获取方法的名称
        String methodName = joinPoint.getSignature().getName();
        // 获取方法入参
        Object[] param = joinPoint.getArgs();
        StringBuilder sb = new StringBuilder();
        for (Object o : param) {
            sb.append(o).append(";");
        }
        //接收到请求，记录请求内容
        String requestUrl = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes)) {
            HttpServletRequest httpServletRequest = attributes.getRequest();
            requestUrl = httpServletRequest.getRequestURL().toString();
        }
        startTime.set(System.currentTimeMillis());
        log.info("==================================================");
        log.info("方法名称: [{}] ", methodName);
        log.info("方法参数: {}", sb);
        log.info("请求URL: [{}]", requestUrl);
        log.info("开始时间: [{}]", DateUtil.format(new Date(startTime.get()), DatePattern.NORM_DATETIME_MS_PATTERN));
        log.info("--------------------------------------------------");
    }

    @AfterReturning(returning = "ret", pointcut = "TakeTime()")
    public void doAfterReturning(JoinPoint joinPoint, Object ret) {
        endTime.set(System.currentTimeMillis());
        long duration = endTime.get() - startTime.get();
        log.info("--------------------------------------------------");
        log.info("方法名称: [{}]", joinPoint.getSignature().getName());
        log.info("方法返回值: {}", JsonUtils.toJsonString(ret));
        log.info("执行耗时: {} ms / {} seconds / {} minute",
                duration,
                String.format("%.2f", duration / 1000.0),
                String.format("%.2f", duration / 1000.0 / 60.0));
        log.info("结束时间: [{}]", DateUtil.format(new Date(endTime.get()), DatePattern.NORM_DATETIME_MS_PATTERN));
        log.info("==================================================");
    }
}
