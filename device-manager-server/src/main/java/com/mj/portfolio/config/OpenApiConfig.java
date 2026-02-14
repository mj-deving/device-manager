package com.mj.portfolio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deviceManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Device Manager API")
                        .version("1.0.0")
                        .description("REST API for managing network devices — Portfolio Project 2")
                        .contact(new Contact()
                                .name("mj-deving")
                                .url("https://github.com/mj-deving")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("http://213.199.32.18").description("VPS production")
                ));
    }
}
