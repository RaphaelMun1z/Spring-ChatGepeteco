package io.github.raphaelmun1z.ChatGepeteco.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ChatGepeteco API")
                .version("0.1.0")
                .description("Documentação da API do ChatGepeteco"));
    }
}
