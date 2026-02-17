package nl.stijlaartit.generator.engine.domain;

import java.util.Objects;

public record NullWrapperFile(String packageName) implements GenerationFile {

    public NullWrapperFile {
        Objects.requireNonNull(packageName, "packageName");
    }

    @Override
    public String name() {
        return "NullWrapper";
    }
}
