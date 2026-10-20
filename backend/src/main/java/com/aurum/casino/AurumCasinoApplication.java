package com.aurum.casino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AurumCasinoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AurumCasinoApplication.class, args);
    }
}
