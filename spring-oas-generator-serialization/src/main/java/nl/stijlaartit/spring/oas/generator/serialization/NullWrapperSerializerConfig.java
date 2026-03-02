package nl.stijlaartit.spring.oas.generator.serialization;

import java.util.Objects;

public record NullWrapperSerializerConfig(JacksonVersion jacksonVersion) {

    public NullWrapperSerializerConfig {
        Objects.requireNonNull(jacksonVersion, "jacksonVersion");
    }

    public static NullWrapperSerializerConfig defaultConfig() {
        return new NullWrapperSerializerConfig(JacksonVersion.V3);
    }
}
