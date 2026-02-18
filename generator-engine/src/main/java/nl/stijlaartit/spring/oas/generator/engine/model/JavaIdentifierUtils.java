package nl.stijlaartit.spring.oas.generator.engine.model;

import java.util.Set;

public final class JavaIdentifierUtils {

    private static final Set<String> RESERVED_WORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "_",
            "true", "false", "null",
            "var", "yield", "record", "sealed", "permits",
            "open", "module", "requires", "transitive", "exports", "opens", "to", "uses",
            "provides", "with"
    );

    private JavaIdentifierUtils() {
    }

    public static String sanitize(String name) {
        if (name.isBlank()) {
            return "value";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                if (Character.isJavaIdentifierStart(c)) {
                    result.append(c);
                } else if (Character.isJavaIdentifierPart(c)) {
                    result.append('_').append(c);
                } else {
                    result.append('_');
                }
            } else {
                result.append(Character.isJavaIdentifierPart(c) ? c : '_');
            }
        }

        String sanitized = result.toString();
        if (sanitized.equals("_")) {
            sanitized = "value";
        }
        if (RESERVED_WORDS.contains(sanitized)) {
            sanitized = sanitized + "_";
        }
        return sanitized;
    }
}
