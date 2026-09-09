package ca.mcgill.ecse321.gallerysystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI gallerySystemOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Online Art Gallery API")
                                                .description("REST API for an online art marketplace connecting artists and customers.\n\n"
                                                                + "The API provides:\n"
                                                                + "- Artwork listing and management\n"
                                                                + "- Customer shopping carts\n"
                                                                + "- Checkout and order processing\n"
                                                                + "- User management\n\n"
                                                                + "Typical customer workflow:\n"
                                                                + "Browse artwork → Add items to cart → Checkout → View orders")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Ben Mwaniki")
                                                                .email("ben12mwaniki@proton.me"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")));
        }
}