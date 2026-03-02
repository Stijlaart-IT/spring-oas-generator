package nl.stijlaartit.spring.oas.generator.engine;

import nl.stijlaartit.spring.oas.generator.serialization.ClientWriterConfig;
import nl.stijlaartit.spring.oas.generator.serialization.BuilderMode;
import nl.stijlaartit.spring.oas.generator.serialization.JacksonVersion;
import nl.stijlaartit.spring.oas.generator.serialization.NullWrapperSerializerConfig;
import nl.stijlaartit.spring.oas.generator.serialization.RecordModelWriterConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratorConfigTest {

    @Test
    void constructor_usesDefaultWriterConfigs() {
        GeneratorConfig config = new GeneratorConfig(Path.of("spec.yml"), Path.of("out"), "com.example.generated");

        assertThat(config.recordModelWriterConfig()).isEqualTo(RecordModelWriterConfig.defaultConfig());
        assertThat(config.clientWriterConfig()).isEqualTo(ClientWriterConfig.defaultConfig());
        assertThat(config.nullWrapperSerializerConfig()).isEqualTo(NullWrapperSerializerConfig.defaultConfig());
    }

    @Test
    void withMethods_overrideOnlyTargetConfig() {
        GeneratorConfig config = new GeneratorConfig(Path.of("spec.yml"), Path.of("out"), "com.example.generated");
        RecordModelWriterConfig recordConfig = new RecordModelWriterConfig(BuilderMode.RELAXED);
        ClientWriterConfig clientConfig = new ClientWriterConfig(ClientWriterConfig.IoMode.REACTIVE);
        NullWrapperSerializerConfig nullWrapperConfig =
                new NullWrapperSerializerConfig(JacksonVersion.V2);

        GeneratorConfig withRecordConfig = config.withRecordModelWriterConfig(recordConfig);
        GeneratorConfig withClientConfig = config.withClientWriterConfig(clientConfig);
        GeneratorConfig withNullWrapperConfig = config.withNullWrapperSerializerConfig(nullWrapperConfig);

        assertThat(withRecordConfig.recordModelWriterConfig()).isEqualTo(recordConfig);
        assertThat(withRecordConfig.clientWriterConfig()).isEqualTo(config.clientWriterConfig());

        assertThat(withClientConfig.clientWriterConfig()).isEqualTo(clientConfig);
        assertThat(withClientConfig.recordModelWriterConfig()).isEqualTo(config.recordModelWriterConfig());

        assertThat(withNullWrapperConfig.nullWrapperSerializerConfig()).isEqualTo(nullWrapperConfig);
        assertThat(withNullWrapperConfig.recordModelWriterConfig()).isEqualTo(config.recordModelWriterConfig());
        assertThat(withNullWrapperConfig.clientWriterConfig()).isEqualTo(config.clientWriterConfig());
    }
}
