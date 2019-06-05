package com.jcspider.server.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhuang.hu
 * @since 28 五月 2019
 */
@SpringBootApplication(scanBasePackages = "com.jcspider")
public class JCSpiderApplication {


    public static void main(String[] args) {
        SpringApplication.run(JCSpiderApplication.class, args);
    }


}
