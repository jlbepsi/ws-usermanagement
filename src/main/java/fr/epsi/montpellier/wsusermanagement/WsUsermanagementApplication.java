package fr.epsi.montpellier.wsusermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class WsUsermanagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsUsermanagementApplication.class, args);
    }
}
