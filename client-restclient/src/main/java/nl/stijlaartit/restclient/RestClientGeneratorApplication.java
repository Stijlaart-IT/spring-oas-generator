package nl.stijlaartit.restclient;

import nl.stijlaartit.generation.model.ModelResolver;
import nl.stijlaartit.generator.model.ModelDescriptor;
import nl.stijlaartit.generator.model.ModelWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class RestClientGeneratorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(RestClientGeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: <openapi-spec-file> <output-path> <output-package>");
            System.exit(1);
        }

        String specFile = args[0];
        String outputPath = args[1];
        String outputPackage = args[2];
        String modelsPackage = outputPackage + ".models";

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specFile, null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            System.err.println("Failed to parse OpenAPI spec: " + specFile);
            if (result.getMessages() != null) {
                result.getMessages().forEach(System.err::println);
            }
            System.exit(1);
        }

        ModelResolver modelResolver = new ModelResolver();
        List<ModelDescriptor> models = modelResolver.resolve(openAPI);

        Path output = Path.of(outputPath);
        ModelWriter modelWriter = new ModelWriter(modelsPackage);
        modelWriter.writeAll(models, output);

        System.out.println("Generated " + models.size() + " model(s) to "
                + output.resolve(modelsPackage.replace('.', '/')));
    }
}
