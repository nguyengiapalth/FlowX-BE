package project.ii.flowx.config;



import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowX")
                        .version("0.0.0")
                        .contact(new Contact()
                                .name("Nguyen Khac Giap")
                                .url("https://example.com")
                                .email("nguyengiapnf5@gmail.com")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:3001")
                                .description("Server phát triển"),

                        new Server()
                                .url("https://api.example.com")
                                .description("Server production")
                ))
                .components(new Components()
                        // Bearer token auth
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication")))

                ;
    }
}
