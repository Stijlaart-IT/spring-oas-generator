package nl.stijlaartit.generator.maven;

import nl.stijlaartit.generator.engine.Generator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class GenerateMojo extends AbstractMojo {

    @Parameter(name = "openapiSpec", property = "openapiSpec", required = true)
    private File openapiSpec;

    @Parameter(name = "outputPackage", property = "outputPackage", required = true)
    private String outputPackage;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        Path specPath = openapiSpec != null ? openapiSpec.toPath() : null;
        if (specPath == null) {
            throw new MojoExecutionException("openapiSpec must be provided.");
        }
        if (!Files.isRegularFile(specPath)) {
            throw new MojoExecutionException("Spec file does not exist or is not a regular file: " + specPath);
        }
        if (!Files.isReadable(specPath)) {
            throw new MojoExecutionException("Spec file is not readable: " + specPath);
        }
        String specName = specPath.getFileName().toString();
        if (!(specName.endsWith(".yml") || specName.endsWith(".yaml") || specName.endsWith(".json"))) {
            throw new MojoExecutionException("Spec file must be a .yml, .yaml, or .json file: " + specPath);
        }

        String trimmedPackage = outputPackage != null ? outputPackage.trim() : "";
        if (trimmedPackage.isEmpty()) {
            throw new MojoExecutionException("Output package must not be empty.");
        }
        if (!trimmedPackage.matches("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*")) {
            throw new MojoExecutionException("Output package is not a valid Java package name: " + outputPackage);
        }

        Path outputDir = Path.of(project.getBuild().getDirectory(), "generated-sources");
        try {
            Files.createDirectories(outputDir);
            new Generator().generate(specPath, outputDir, trimmedPackage);
            project.addCompileSourceRoot(outputDir.toString());
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to generate sources.", ex);
        }
    }
}
