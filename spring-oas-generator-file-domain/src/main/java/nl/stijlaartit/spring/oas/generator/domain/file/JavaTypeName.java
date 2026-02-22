package nl.stijlaartit.spring.oas.generator.domain.file;

import javax.lang.model.SourceVersion;
import java.util.Set;

public sealed interface JavaTypeName permits JavaTypeName.Reserved, JavaTypeName.Generated {
     Set<String> RESERVED_TYPE_NAMES = Set.of(
            "Object", "Class", "String", "Integer", "Long", "Boolean", "Double", "Float",
            "Short", "Byte", "Character", "Number", "Void", "Enum", "List", "BigDecimal"
    );

    String value();

    record Reserved(String value) implements JavaTypeName {
        public Reserved {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Name is blank.");
            }
            if (!RESERVED_TYPE_NAMES.contains(value)) {
                throw new IllegalArgumentException("Name is not a reserved Java schema: " + value);
            }
        }
    }


     record Generated(String value) implements JavaTypeName {

        public Generated {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Name is blank.");
            }
            if (!SourceVersion.isIdentifier(value) || SourceVersion.isKeyword(value)) {
                throw new IllegalArgumentException("Invalid name: " + value);
            }
            if (!Character.isUpperCase(value.charAt(0))) {
                throw new IllegalArgumentException("Invalid name: " + value);
            }
            if (RESERVED_TYPE_NAMES.contains(value)) {
                throw new IllegalArgumentException("Name collides with reserved Java schema: " + value);
            }
        }
    }
}
