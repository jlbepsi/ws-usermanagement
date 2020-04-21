package fr.epsi.montpellier.wsusermanagement.security.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.*;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.epsi.montpellier.wsusermanagement"))
                .paths(PathSelectors.any())
                .build()
                .produces(DEFAULT_PRODUCES_AND_CONSUMES)
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES)
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "WSUserManagement",
                "API de gestion des utilisateurs LDAP en Spring Boot",
                "1.0",
                "urn:tos",
                new Contact("Jean-Luc Bompard", "https://github.com/jlbepsi", "jlb.epsi@gmail.com"),
                "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", new ArrayList<>()
        );
    }

    private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES =
            new HashSet<String>(Collections.singletonList("application/json"));
}
