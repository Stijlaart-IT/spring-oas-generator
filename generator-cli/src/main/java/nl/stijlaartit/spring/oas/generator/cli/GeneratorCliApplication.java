package nl.stijlaartit.spring.oas.generator.cli;

import nl.stijlaartit.spring.oas.generator.engine.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class GeneratorCliApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorCliApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GeneratorCliApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 3) {
            LOG.error("Usage: <openapi-spec-file> <output-path> <output-package>");
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
            LOG.error("Spec file does not exist or is not a regular file: {}", specPath);
            System.exit(1);
        }
        if (!Files.isReadable(specPath)) {
            LOG.error("Spec file is not readable: {}", specPath);
            System.exit(1);
        }

        Path outputDir = Path.of(outputPath);
        if (Files.exists(outputDir) && !Files.isDirectory(outputDir)) {
            LOG.error("Output path exists but is not a directory: {}", outputDir);
            System.exit(1);
        }

        String trimmedPackage = outputPackage != null ? outputPackage.trim() : "";
        if (trimmedPackage.isEmpty()) {
            LOG.error("Output package must not be empty.");
            System.exit(1);
        }
        if (!trimmedPackage.matches("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*")) {
            LOG.error("Output package is not a valid Java package name: {}", outputPackage);
            System.exit(1);
        }
        final var logger = new Slf4jLogger(LOG);

        new Generator(logger).generate(specPath, outputDir, trimmedPackage);
    }
}
