package org.swtdemo.requestthrottling.aop;

import io.lettuce.core.dynamic.annotation.Command;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.swtdemo.requestthrottling.annotation.RequestLimitAnnotation;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Classname CurrentLimiting
 * @Description 限流切面类注解
 * @Date 2024/5/23 下午9:11
 * @Created by 憧憬
 */

@Aspect
@Slf4j
@Component
public class CurrentLimiting {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct // 类似static用于初始化
    public void init(){
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/redisLimit.lua")));
    }

    @Pointcut("@annotation(org.swtdemo.requestthrottling.annotation.RequestLimitAnnotation)")
    public void pointCut(){}


    @Around("pointCut()")
    public Object limit(ProceedingJoinPoint joinPoint){
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        RequestLimitAnnotation annotation = method.getAnnotation(RequestLimitAnnotation.class);
        Object proceed = null;

        if(annotation != null){
            String key = annotation.key();
            String className = method.getDeclaringClass().getName();
            String methodName = method.getName();
            log.info("key:{}, className:{}, methodName:{}", key, className, methodName);
            if(null == key){
                throw new RuntimeException("key不能为空");
            }

            Long permitsPerSecond = annotation.permitsPerSecond();
            Integer expire = annotation.expire();
            List<String> keys = new ArrayList<>();
            keys.add(key);
            Long count = redisTemplate.execute(
                    redisScript,
                    keys,
                    String.valueOf(permitsPerSecond),
                    String.valueOf(expire)
            );
            if(null != count && count == 0L){
                return annotation.message();
            }
            try {
                proceed = joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return proceed;
    }
}
