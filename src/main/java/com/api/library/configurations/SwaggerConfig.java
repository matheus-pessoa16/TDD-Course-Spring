package com.api.library.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().contact(contatct()).title("Library API")
        .description("API para controle de empréstimo de livros com TDD").version("1.0").build();
  }

  private Contact contatct() {
    return new Contact("Matheus Pessoa", "http://github.com/matheus-pessoa16", "matheus.pessoa16@gmail.com");
  }

  @Bean
  public Docket docket() {
    return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.basePackage("com.api.library"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo());
  }
}
