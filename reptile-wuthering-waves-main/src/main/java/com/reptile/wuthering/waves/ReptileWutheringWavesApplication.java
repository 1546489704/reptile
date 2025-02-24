package com.reptile.wuthering.waves;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReptileWutheringWavesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReptileWutheringWavesApplication.class, args);
    }

}
