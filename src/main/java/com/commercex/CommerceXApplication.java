package com.commercex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommerceXApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommerceXApplication.class, args);
    }

}
