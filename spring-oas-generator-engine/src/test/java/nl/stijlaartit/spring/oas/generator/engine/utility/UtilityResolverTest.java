package nl.stijlaartit.spring.oas.generator.engine.utility;

import nl.stijlaartit.spring.oas.generator.domain.file.ApiFile;
import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;
import nl.stijlaartit.spring.oas.generator.domain.file.PackageInfoFile;
import nl.stijlaartit.spring.oas.generator.domain.file.SpringConfigFile;
import nl.stijlaartit.spring.oas.generator.engine.SpringConfigGenerationConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UtilityResolverTest {

    @Test
    void addsSpringConfigFileWhenConfiguredAndClientsExist() {
        UtilityResolver resolver = new UtilityResolver(
                "com.example.generated.models",
                "com.example.generated.client",
                "com.example.generated.config",
                new SpringConfigGenerationConfig("petstore")
        );

        List<GenerationFile> files = resolver.resolve(List.of(), List.of(new ApiFile("PetApi", List.of())));

        assertThat(files).anyMatch(SpringConfigFile.class::isInstance);
        SpringConfigFile springConfig = (SpringConfigFile) files.stream()
                .filter(SpringConfigFile.class::isInstance)
                .findFirst()
                .orElseThrow();
        assertThat(springConfig.packageName()).isEqualTo("com.example.generated.config");
        assertThat(springConfig.apiPackage()).isEqualTo("com.example.generated.client");
        assertThat(springConfig.serviceGroupName()).isEqualTo("petstore");
        assertThat(springConfig.apiTypeNames()).containsExactly("PetApi");
    }

    @Test
    void doesNotAddSpringConfigFileWhenNotConfigured() {
        UtilityResolver resolver = new UtilityResolver(
                "com.example.generated.models",
                "com.example.generated.client",
                "com.example.generated.config",
                null
        );

        List<GenerationFile> files = resolver.resolve(List.of(), List.of(new ApiFile("PetApi", List.of())));

        assertThat(files).noneMatch(SpringConfigFile.class::isInstance);
    }

    @Test
    void doesNotAddSpringConfigFileWhenNoClientsExist() {
        UtilityResolver resolver = new UtilityResolver(
                "com.example.generated.models",
                "com.example.generated.client",
                "com.example.generated.config",
                new SpringConfigGenerationConfig("petstore")
        );

        List<GenerationFile> files = resolver.resolve(List.of(), List.of());

        assertThat(files).noneMatch(SpringConfigFile.class::isInstance);
        assertThat(files).noneMatch(PackageInfoFile.class::isInstance);
    }
}
