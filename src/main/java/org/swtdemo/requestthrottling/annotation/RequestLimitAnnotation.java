package org.swtdemo.requestthrottling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname RequestLimitAnnotaion
 * @Description 什么也没有写哦~
 * @Date 2024/5/23 下午9:08
 * @Created by 憧憬
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLimitAnnotation {
    public String key() default  "";
    long permitsPerSecond() default 3;
    int expire() default  60;
    String message() default  "服务繁忙请稍后";
}
