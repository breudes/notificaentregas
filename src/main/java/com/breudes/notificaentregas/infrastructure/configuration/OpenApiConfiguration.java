package com.breudes.notificaentregas.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI notificaEntregasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notificação de Entregas")
                        .description("Serviço responsável por consumir notificações de encomendas via RabbitMQ e enviar e-mails aos destinatários. Este serviço não expõe endpoints REST.")
                        .version("1.0.0"));
    }
}
