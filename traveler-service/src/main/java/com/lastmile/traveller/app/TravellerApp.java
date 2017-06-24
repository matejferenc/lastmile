package com.lastmile.traveller.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.lastmile", "com.lastmile.traveller.service", "com.lastmile.traveller", "com.lastmile.traveller.app"})
public class TravellerApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TravellerApp.class, args);
    }

}
