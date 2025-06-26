package com.pemc.crss.rbcq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
//@EntityScan(basePackages = "com.pemc.crss.rbcq.entity")
public class RbcqApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbcqApplication.class, args);
    }

}