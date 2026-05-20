package com.project.insajang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InsajangApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsajangApplication.class, args);
    }

}
