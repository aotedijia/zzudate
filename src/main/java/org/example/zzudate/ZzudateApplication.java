package org.example.zzudate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZzudateApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzudateApplication.class, args);
    }

}
