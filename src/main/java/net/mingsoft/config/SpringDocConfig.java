


package net.mingsoft.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 铭软开发团队
 * @ClassName:
 * @Description:
 * @date
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info().title("Api Documentation")
                        .description("Api Documentation")
                        .version("V1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
