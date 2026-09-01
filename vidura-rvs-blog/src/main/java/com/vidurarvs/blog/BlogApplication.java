package com.vidurarvs.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the ViduraRvs blog platform.
 *
 * Run this class (or `mvn spring-boot:run`) to start the app on
 * http://localhost:8080 once MySQL is configured in application.properties.
 */
@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
