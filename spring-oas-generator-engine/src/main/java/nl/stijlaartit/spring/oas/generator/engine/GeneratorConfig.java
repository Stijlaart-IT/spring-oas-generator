package nl.stijlaartit.spring.oas.generator.engine;

import nl.stijlaartit.spring.oas.generator.serialization.ClientWriterConfig;
import nl.stijlaartit.spring.oas.generator.serialization.NullWrapperSerializerConfig;
import nl.stijlaartit.spring.oas.generator.serialization.RecordModelWriterConfig;

import java.nio.file.Path;
import java.util.Objects;

public record GeneratorConfig(
        Path specFile,
        Path outputDirectory,
        String outputPackage,
        RecordModelWriterConfig recordModelWriterConfig,
        ClientWriterConfig clientWriterConfig,
        NullWrapperSerializerConfig nullWrapperSerializerConfig
) {

    public GeneratorConfig {
        Objects.requireNonNull(specFile, "specFile");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(outputPackage, "outputPackage");
        Objects.requireNonNull(recordModelWriterConfig, "recordModelWriterConfig");
        Objects.requireNonNull(clientWriterConfig, "clientWriterConfig");
        Objects.requireNonNull(nullWrapperSerializerConfig, "nullWrapperSerializerConfig");
    }

    public GeneratorConfig(Path specFile, Path outputDirectory, String outputPackage) {
        this(
                specFile,
                outputDirectory,
                outputPackage,
                RecordModelWriterConfig.defaultConfig(),
                ClientWriterConfig.defaultConfig(),
                NullWrapperSerializerConfig.defaultConfig()
        );
    }

    public GeneratorConfig withRecordModelWriterConfig(RecordModelWriterConfig recordModelWriterConfig) {
        return new GeneratorConfig(specFile, outputDirectory, outputPackage, recordModelWriterConfig, clientWriterConfig, nullWrapperSerializerConfig);
    }

    public GeneratorConfig withClientWriterConfig(ClientWriterConfig clientWriterConfig) {
        return new GeneratorConfig(specFile, outputDirectory, outputPackage, recordModelWriterConfig, clientWriterConfig, nullWrapperSerializerConfig);
    }

    public GeneratorConfig withNullWrapperSerializerConfig(NullWrapperSerializerConfig nullWrapperSerializerConfig) {
        return new GeneratorConfig(specFile, outputDirectory, outputPackage, recordModelWriterConfig, clientWriterConfig, nullWrapperSerializerConfig);
    }
}
