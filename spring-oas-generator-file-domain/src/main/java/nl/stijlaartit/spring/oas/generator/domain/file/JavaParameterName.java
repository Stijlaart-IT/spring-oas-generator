package nl.stijlaartit.spring.oas.generator.domain.file;

import javax.lang.model.SourceVersion;

public record JavaParameterName(String value) {
    public JavaParameterName {
        if (value == null || !SourceVersion.isIdentifier(value) || SourceVersion.isKeyword(value)) {
            throw new IllegalArgumentException("Invalid Java parameter identifier: " + value);
        }
    }
}
