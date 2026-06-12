package com.analyzer.common.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  Swagger 配置类
 */
@Configuration
public class SwaggerConfig {

    private static final String BASE_PACKAGE = "com.analyzer.modules.";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My API")
                        .description("项目接口文档")
                        .version("v1.0.0"));
    }

    @Bean
    public GroupedOpenApi projectApi() {
        return GroupedOpenApi.builder()
                .group("项目接口")
                .packagesToScan(BASE_PACKAGE + "project")
                .build();
    }
}
