package nl.stijlaartit.spring.oas.generator.cli;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorCliApplicationTest {

    @Test
    void parsesSpringConfigServiceGroupNameOption() {
        Map<String, List<String>> options = GeneratorCliApplication.parseNamedOptions(new String[]{
                "--openapi-spec", "spec.yml",
                "--output-path", "out",
                "--output-package", "com.example.generated",
                "--spring-config-service-group-name", "petstore"
        });

        assertEquals("petstore",
                GeneratorCliApplication.getSingleValue(options, "spring-config-service-group-name").orElseThrow());
    }

    @Test
    void keepsSpringConfigServiceGroupNameOptionalWhenAbsent() {
        Map<String, List<String>> options = GeneratorCliApplication.parseNamedOptions(new String[]{
                "--openapi-spec", "spec.yml",
                "--output-path", "out",
                "--output-package", "com.example.generated"
        });

        assertTrue(GeneratorCliApplication.getSingleValue(options, "spring-config-service-group-name").isEmpty());
    }
}
