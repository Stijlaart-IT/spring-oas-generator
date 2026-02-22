package nl.stijlaartit.spring.oas.generator.domain.file;

import javax.lang.model.SourceVersion;

public record JavaMethodName(String value) {
    public JavaMethodName {
        if (value == null || !SourceVersion.isIdentifier(value) || SourceVersion.isKeyword(value)) {
            throw new IllegalArgumentException("Invalid Java method identifier: " + value);
        }
    }
}
