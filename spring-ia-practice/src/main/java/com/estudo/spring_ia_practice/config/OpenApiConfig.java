package com.estudo.spring_ia_practice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI careFlowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareFlow AI API")
                        .description("API do assistente de consultas e procedimentos da clínica CareFlow.")
                        .version("v1"));
    }
}
