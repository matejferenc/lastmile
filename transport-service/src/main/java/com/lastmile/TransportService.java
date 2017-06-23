package com.lastmile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.lastmile"})
public class TransportService extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TransportService.class, args);
    }

}
