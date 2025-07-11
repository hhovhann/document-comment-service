package am.hhovhann.document_comment_service.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Document Comment Backend API")
                    .version("1.0")
                    .description("API for managing documents and paragraph-anchored comments")
                    .contact(
                        Contact()
                            .name("Team")
                            .email("support@example.com")
                    )
            )
    }
}