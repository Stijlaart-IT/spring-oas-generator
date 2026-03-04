package nl.stijlaartit.spring.oas.generator.cli;

import nl.stijlaartit.spring.oas.generator.engine.Generator;
import nl.stijlaartit.spring.oas.generator.engine.GeneratorConfig;
import nl.stijlaartit.spring.oas.generator.serialization.BuilderMode;
import nl.stijlaartit.spring.oas.generator.serialization.JacksonVersion;
import nl.stijlaartit.spring.oas.generator.serialization.NullWrapperSerializerConfig;
import nl.stijlaartit.spring.oas.generator.serialization.RecordModelWriterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class GeneratorCliApplication implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorCliApplication.class);
    private static final String OPTION_OPENAPI_SPEC = "openapi-spec";
    private static final String OPTION_OUTPUT_PATH = "output-path";
    private static final String OPTION_OUTPUT_PACKAGE = "output-package";
    private static final String OPTION_RECORD_MODEL_BUILDER_MODE = "record-model-builder-mode";
    private static final String OPTION_RECORD_MODEL_DISABLE_JACKSON_REQUIRED = "record-model-disable-jackson-required";
    private static final String OPTION_RECORD_MODEL_JACKSON_VERSION = "record-model-jackson-version";

    public static void main(String[] args) {
        SpringApplication.run(GeneratorCliApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, List<String>> parsedOptions = parseNamedOptions(args.getSourceArgs());

        Optional<String> specFileOpt = getSingleValue(parsedOptions, OPTION_OPENAPI_SPEC);
        Optional<String> outputPathOpt = getSingleValue(parsedOptions, OPTION_OUTPUT_PATH);
        Optional<String> outputPackageOpt = getSingleValue(parsedOptions, OPTION_OUTPUT_PACKAGE);
        if (specFileOpt.isEmpty() || outputPathOpt.isEmpty() || outputPackageOpt.isEmpty()) {
            LOG.error("Usage: --{} <openapi-spec-file> --{} <output-path> --{} <output-package> [--{} <DISABLED|STRICT|RELAXED>] [--{}[=<true|false>]] [--{} <2|3>]",
                    OPTION_OPENAPI_SPEC,
                    OPTION_OUTPUT_PATH,
                    OPTION_OUTPUT_PACKAGE,
                    OPTION_RECORD_MODEL_BUILDER_MODE,
                    OPTION_RECORD_MODEL_DISABLE_JACKSON_REQUIRED,
                    OPTION_RECORD_MODEL_JACKSON_VERSION);
            System.exit(1);
        }
        String specFile = specFileOpt.orElseThrow();
        String outputPath = outputPathOpt.orElseThrow();
        String outputPackage = outputPackageOpt.orElseThrow();
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
        String specName = specPath.getFileName().toString();
        if (!(specName.endsWith(".yml") || specName.endsWith(".yaml") || specName.endsWith(".json"))) {
            LOG.error("Spec file must be a .yml, .yaml, or .json file: {}", specPath);
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
        BuilderMode builderMode = BuilderMode.parse(getSingleValue(parsedOptions, OPTION_RECORD_MODEL_BUILDER_MODE).orElse("STRICT"))
                .orElse(null);
        if (builderMode == null) {
            LOG.error("Invalid --{} value. Allowed values: DISABLED, STRICT, RELAXED.", OPTION_RECORD_MODEL_BUILDER_MODE);
            System.exit(1);
        }

        boolean disableJacksonRequired = parseBooleanFlag(parsedOptions, OPTION_RECORD_MODEL_DISABLE_JACKSON_REQUIRED);

        JacksonVersion jacksonVersion = JacksonVersion.parse(getSingleValue(parsedOptions, OPTION_RECORD_MODEL_JACKSON_VERSION).orElse("3"))
                .orElse(null);
        if (jacksonVersion == null) {
            LOG.error("Invalid --{} value. Allowed values: 2, 3.", OPTION_RECORD_MODEL_JACKSON_VERSION);
            System.exit(1);
        }

        final var logger = new Slf4jLogger(LOG);
        RecordModelWriterConfig recordModelWriterConfig =
                new RecordModelWriterConfig(builderMode, disableJacksonRequired);
        GeneratorConfig generatorConfig = new GeneratorConfig(specPath, outputDir, trimmedPackage)
                .withRecordModelWriterConfig(recordModelWriterConfig)
                .withNullWrapperSerializerConfig(new NullWrapperSerializerConfig(jacksonVersion));

        new Generator(logger).generate(generatorConfig);
    }

    private static Optional<String> getSingleValue(Map<String, List<String>> options, String optionName) {
        List<String> values = options.get(optionName);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        String value = values.getLast();
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private static boolean parseBooleanFlag(Map<String, List<String>> options, String optionName) {
        List<String> values = options.get(optionName);
        if (values == null) {
            return false;
        }
        if (values == null || values.isEmpty()) {
            return true;
        }
        String value = values.getLast();
        if (value == null || value.isBlank()) {
            return true;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static Map<String, List<String>> parseNamedOptions(String[] sourceArgs) {
        Map<String, List<String>> options = new HashMap<>();
        for (int i = 0; i < sourceArgs.length; i++) {
            String token = sourceArgs[i];
            if (!token.startsWith("--")) {
                continue;
            }

            String withoutPrefix = token.substring(2);
            String optionName;
            String optionValue = null;
            int equalsIndex = withoutPrefix.indexOf('=');
            if (equalsIndex >= 0) {
                optionName = withoutPrefix.substring(0, equalsIndex);
                optionValue = withoutPrefix.substring(equalsIndex + 1);
            } else {
                optionName = withoutPrefix;
                if (i + 1 < sourceArgs.length && !sourceArgs[i + 1].startsWith("--")) {
                    optionValue = sourceArgs[++i];
                }
            }

            options.computeIfAbsent(optionName, key -> new ArrayList<>());
            if (optionValue != null) {
                options.get(optionName).add(optionValue);
            }
        }
        return options;
    }
}
