package nl.stijlaartit.spring.oas.generator.serialization;

public record ClientWriterConfig(IoMode ioMode) {

    public static ClientWriterConfig defaultConfig() {
        return new ClientWriterConfig(IoMode.BLOCKING);
    }

    public enum IoMode {
        BLOCKING,
        REACTIVE
    }
}
