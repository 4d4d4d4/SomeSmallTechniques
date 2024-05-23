package org.swtdemo.requestthrottling.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swtdemo.requestthrottling.annotation.RequestLimitAnnotation;

import java.util.UUID;

/**
 * @Classname TestControoler
 * @Description 什么也没有写哦~
 * @Date 2024/5/23 下午10:14
 * @Created by 憧憬
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/limit")
    @RequestLimitAnnotation(key = "测试请求", permitsPerSecond = 3, expire = 1, message = "服务器繁忙请稍后")
    public String test(){
        return "当前服务请求成功 : " + UUID.randomUUID();
    }
}
