package com.lastmile.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.lastmile"})
public class TravelerService extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TravelerService.class, args);
    }

}
