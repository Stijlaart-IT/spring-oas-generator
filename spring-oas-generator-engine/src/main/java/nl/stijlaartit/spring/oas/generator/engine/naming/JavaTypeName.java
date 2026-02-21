package nl.stijlaartit.spring.oas.generator.engine.naming;

import java.util.Set;

public sealed interface JavaTypeName permits JavaTypeName.Reserved, JavaTypeName.Generated {
     Set<String> RESERVED_TYPE_NAMES = Set.of(
            "Object", "Class", "String", "Integer", "Long", "Boolean", "Double", "Float",
            "Short", "Byte", "Character", "Number", "Void", "Enum", "List"
    );

    String value();

    record Reserved(String value) implements JavaTypeName {
        public Reserved {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Name is blank.");
            }
            if (!RESERVED_TYPE_NAMES.contains(value)) {
                throw new IllegalStateException("Name is not a reserved Java schema: " + value);
            }
        }
    }


     record Generated(String value) implements JavaTypeName {

        public Generated {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Name is blank.");
            }
            char first = value.charAt(0);
            if (first == '_' || !Character.isJavaIdentifierStart(first) || !Character.isUpperCase(first)) {
                throw new IllegalStateException("Invalid name: " + value);
            }
            for (int i = 1; i < value.length(); i++) {
                char c = value.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    throw new IllegalStateException("Invalid name: " + value);
                }
            }
            if (RESERVED_TYPE_NAMES.contains(value)) {
                throw new IllegalStateException("Name collides with reserved Java schema: " + value);
            }
        }
    }
}
