package com.lastmile.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.lastmile", "com.lastmile.traveler.service", "com.lastmile.traveler"})
public class TravelerService extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TravelerService.class, args);
    }

}
