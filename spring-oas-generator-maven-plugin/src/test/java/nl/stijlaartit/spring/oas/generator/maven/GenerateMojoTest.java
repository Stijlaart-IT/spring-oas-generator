package nl.stijlaartit.spring.oas.generator.maven;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GenerateMojoTest {

    @Test
    void throwsWhenSpringConfigIsConfiguredWithoutServiceGroupName() throws Exception {
        Path tempDir = Files.createTempDirectory("spring-oas-generator-mojo-test");
        Path spec = tempDir.resolve("openapi.yml");
        Files.writeString(spec, "openapi: 3.0.3\ninfo:\n  title: Test\n  version: 1.0.0\npaths: {}\n");

        MavenProject project = new MavenProject();
        Build build = new Build();
        build.setDirectory(tempDir.resolve("target").toString());
        project.setBuild(build);

        SpringConfigConfiguration springConfigConfiguration = new SpringConfigConfiguration();
        setField(springConfigConfiguration, "serviceGroupName", "  ");

        GenerateMojo mojo = new GenerateMojo();
        setField(mojo, "openapiSpec", spec.toFile());
        setField(mojo, "outputPackage", "com.example.generated");
        setField(mojo, "project", project);
        setField(mojo, "springConfig", springConfigConfiguration);

        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
