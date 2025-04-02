package com.whale.bean.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

// spring 会推导bean的最近公共basepackage，这里向上找到 com.whale.bean
// @SpringBootApplication(scanBasePackages = "")  // 如果指定了spring扫描，实际上也可以，非常smart
@SpringBootApplication
@ComponentScans(value ={ @ComponentScan("com.whale.bean")} )
public class Application {
    public static void main( String[] args ){
        SpringApplication.run(Application.class, args);
    }
}
