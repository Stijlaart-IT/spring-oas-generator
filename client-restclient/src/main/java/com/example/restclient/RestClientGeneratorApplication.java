package com.example.restclient;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestClientGeneratorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(RestClientGeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: <openapi-spec-file>");
            System.exit(1);
        }

        String specFile = args[0];
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specFile, null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            System.err.println("Failed to parse OpenAPI spec: " + specFile);
            if (result.getMessages() != null) {
                result.getMessages().forEach(System.err::println);
            }
            System.exit(1);
        }

        System.out.println(openAPI);
    }
}
