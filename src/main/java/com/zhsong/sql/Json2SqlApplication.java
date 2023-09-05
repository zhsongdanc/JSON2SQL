package com.zhsong.sql;

import com.zhsong.sql.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;

/*
 * @Author: demussong
 * @Description:
 * @Date: 2023/9/5 13:45
 */
@SpringBootApplication
@ComponentScan("com.zhsong.sql.controller")
public class Json2SqlApplication {



    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Json2SqlApplication.class, args);
        String[] beanNamesForType1 = context.getBeanNamesForType(Object.class);
        System.out.println("size=" + beanNamesForType1.length);
        String[] beanNamesForType = context.getBeanNamesForType(TestController.class);
        System.out.println("controller size=" + beanNamesForType.length);
        for (String s : beanNamesForType) {
            System.out.println(s);
        }

    }
}
