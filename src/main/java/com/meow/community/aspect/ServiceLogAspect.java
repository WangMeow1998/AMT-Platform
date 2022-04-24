package com.meow.community.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;


@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.meow.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //用户[IP]，在[时间]，访问了[com.meow.community.service.xxx()].
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null){ //消费者可能在消费的时候调动了Service，触发了日志，因此这种attr为空的时候要处理下
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String userIp = request.getRemoteHost();
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s]",userIp, nowTime, target));
    }

}
