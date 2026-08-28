package ca.mcgill.ecse321.gallerysystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Bean
        public OpenAPI gallerySystemOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Gallery System API")
                                                .description("Art Gallery E-Commerce Backend API")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Ben Mwaniki")
                                                                .email("ben12mwaniki@proton.me")
                                                                .url("https://your-website.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:" + serverPort)
                                                                .description("Local Development Server"),
                                                new Server()
                                                                .url("https://api.yourdomain.com")
                                                                .description("Production Server")));
        }
}