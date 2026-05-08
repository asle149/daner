package com.daner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.daner")
public class DanerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DanerApplication.class, args);
    }
}
