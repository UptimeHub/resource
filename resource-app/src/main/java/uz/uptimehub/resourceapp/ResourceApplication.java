package uz.uptimehub.resourceapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class ResourceApplication {

    static void main(String[] args) {
        SpringApplication.run(ResourceApplication.class, args);
    }

}
