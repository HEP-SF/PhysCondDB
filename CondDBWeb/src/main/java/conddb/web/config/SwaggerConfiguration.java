package conddb.web.config;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.google.common.base.Predicate;

@EnableSwagger2
@EnableWebMvc
@Configuration
public class SwaggerConfiguration {

	@Bean
	public Docket condDbWebApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.groupName("conddbweb")
				.apiInfo(apiInfo())
				//.select()
				//.paths(conddbClientPaths())
				.pathMapping("/conddbweb")
				.select()
				.build();
	}
	
	@Bean
	public Docket condDbApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.groupName("conddb")
				.apiInfo(apiInfo())
				//.select()
				//.paths(conddbClientPaths())
				.pathMapping("/conddb")
				.select()
				.build();
	}

	@SuppressWarnings("unchecked")
	private Predicate<String> conddbClientPaths() {
		return or(regex("/conddbweb.*"), regex("/conddb.*"), regex("/.*")
				);
	}
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Springfox PhysCondDB API")
                .description("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum " +
                        "has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a " +
                        "galley of type and scrambled it to make a type specimen book. It has survived not only five " +
                        "centuries, but also the leap into electronic typesetting, remaining essentially unchanged. " +
                        "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum " +
                        "passages, and more recently with desktop publishing software like Aldus PageMaker including " +
                        "versions of Lorem Ipsum.")
                .termsOfServiceUrl("http://springfox.io")
                .contact("springfox")
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE")
                .version("2.0")
                .build();
    }
}
