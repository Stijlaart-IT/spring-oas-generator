package nl.stijlaartit.restclient;

import nl.stijlaartit.generator.engine.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class RestClientGeneratorApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(RestClientGeneratorApplication.class);

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
        LOG.info("Generating with arguments: ");
        LOG.info("- spec file: [{}]", specFile);
        LOG.info("- output path [{}]", outputPath);
        LOG.info("- output package [{}]", outputPackage);

        Path specPath = Path.of(specFile);
        if (!Files.isRegularFile(specPath)) {
            System.err.println("Spec file does not exist or is not a regular file: " + specPath);
            System.exit(1);
        }
        if (!Files.isReadable(specPath)) {
            System.err.println("Spec file is not readable: " + specPath);
            System.exit(1);
        }

        Path outputDir = Path.of(outputPath);
        if (Files.exists(outputDir) && !Files.isDirectory(outputDir)) {
            System.err.println("Output path exists but is not a directory: " + outputDir);
            System.exit(1);
        }

        String trimmedPackage = outputPackage != null ? outputPackage.trim() : "";
        if (trimmedPackage.isEmpty()) {
            System.err.println("Output package must not be empty.");
            System.exit(1);
        }
        if (!trimmedPackage.matches("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*")) {
            System.err.println("Output package is not a valid Java package name: " + outputPackage);
            System.exit(1);
        }

        new Generator().generate(specPath, outputDir, trimmedPackage);
    }
}
