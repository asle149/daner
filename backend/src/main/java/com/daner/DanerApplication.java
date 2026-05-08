package com.daner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan("com.daner")
@EnableScheduling
public class DanerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DanerApplication.class, args);
    }
}
