package com.lastmile.transport.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.lastmile"})
public class TransportApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TransportApp.class, args);
    }

}
