package ru.kuznec.fileapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.kuznec.fileapp.security.jwt.JwtConfig;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class FileappApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileappApplication.class, args);
    }
}
