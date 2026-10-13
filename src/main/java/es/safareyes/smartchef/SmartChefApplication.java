package es.safareyes.smartchef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SmartChefApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartChefApplication.class, args);
    }

}
