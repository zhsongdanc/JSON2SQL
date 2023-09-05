package com.zhsong.sql.controller;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @Author: demussong
 * @Description:
 * @Date: 2023/9/5 13:42
 */

@RestController
public class TestController {


    @GetMapping("/hello")
    public String sayHello() {
        return "hello";
    }
}
