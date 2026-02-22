package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NamingUtil {

    private NamingUtil() {
    }

    public static SchemaPath findShortestComponentPath(List<SchemaInstance> instances) {
        SchemaPath best = null;
        int bestLength = Integer.MAX_VALUE;
        for (SchemaInstance instance : instances) {
            final var path = instance.path();
            if (!(path.root() instanceof PathRoot.ComponentSchema)) {
                continue;
            }
            int length = path.segments().size();
            if (length < bestLength) {
                best = path;
                bestLength = length;
            }
        }
        if (best == null) {
            throw new IllegalStateException("No component path found for schema model.");
        }
        return best;
    }

    public static boolean hasComponentPath(List<SchemaInstance> instances) {
        for (SchemaInstance instance : instances) {
            if (instance.path().root() instanceof PathRoot.ComponentSchema) {
                return true;
            }
        }
        return false;
    }

    public static SchemaPath findShortestOperationPath(List<SchemaInstance> instances) {
        SchemaPath best = null;
        int bestLength = Integer.MAX_VALUE;
        for (SchemaInstance instance : instances) {
            SchemaPath path = instance.path();
            final var x = switch (path.root()) {
                case PathRoot.ComponentSchema ignored -> Optional.empty();
                case PathRoot.RequestBody ignored -> Optional.of(path);
                case PathRoot.RequestParam ignored -> Optional.of(path);
                case PathRoot.ResponseBody ignored -> Optional.of(path);
                case PathRoot.ComponentParameter ignored -> Optional.empty();
                case PathRoot.SharedPathParam ignored -> Optional.empty();
            };
            if (x.isEmpty()) {
                continue;
            }

            int length = path.segments().size();
            if (length < bestLength) {
                best = path;
                bestLength = length;
            }
        }
        if (best == null) {
            throw new IllegalStateException("No operation path found for schema model.");
        }
        return best;
    }

    public static boolean hasOperationPath(List<SchemaInstance> instances) {
        for (SchemaInstance instance : instances) {
            final var hasOperationRoot = switch (instance.path().root()) {
                case PathRoot.ComponentSchema ignored -> false;
                case PathRoot.RequestBody ignored -> true;
                case PathRoot.RequestParam ignored -> true;
                case PathRoot.ResponseBody ignored -> true;
                case PathRoot.ComponentParameter ignored -> false;
                case PathRoot.SharedPathParam ignored -> false;
            };
            if (hasOperationRoot) {
                return true;
            }
        }
        return false;
    }

    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_' || c == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else if (i == 0) {
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String camel = toCamelCase(input);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    public static JavaTypeName toJavaTypeName(List<String> parts) {
        StringBuilder nameBuilder = new StringBuilder();
        for (String part : parts) {
            nameBuilder.append(normalizePartToPascalCase(part));
        }
        String name = nameBuilder.toString();
        if (name.isBlank()) {
            name = "Type";
        } else if (Character.isDigit(name.charAt(0))) {
            name = "Type" + name;
        }
        if (JavaTypeName.RESERVED_TYPE_NAMES.contains(name)) {
            return new JavaTypeName.Reserved(name);
        }
        return new JavaTypeName.Generated(name);
    }

    private static String normalizePartToPascalCase(String part) {
        if (part == null || part.isBlank()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < part.length(); i++) {
            char c = part.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                token.append(c);
            } else {
                appendToken(result, token);
                token.setLength(0);
            }
        }
        appendToken(result, token);
        return result.toString();
    }

    private static void appendToken(StringBuilder result, StringBuilder token) {
        if (token.isEmpty()) {
            return;
        }
        String tokenValue = token.toString();
        for (String chunk : splitCamelToken(tokenValue)) {
            result.append(pascalizeChunk(chunk));
        }
    }

    private static List<String> splitCamelToken(String token) {
        if (isAllUpper(token) || isAllLower(token) || isAllDigits(token)) {
            return List.of(token);
        }
        List<String> parts = new ArrayList<>();
        int start = 0;
        for (int i = 1; i < token.length(); i++) {
            char prev = token.charAt(i - 1);
            char current = token.charAt(i);
            if ((Character.isLowerCase(prev) || Character.isDigit(prev)) && Character.isUpperCase(current)) {
                parts.add(token.substring(start, i));
                start = i;
            }
        }
        parts.add(token.substring(start));
        return parts;
    }

    private static boolean isAllUpper(String token) {
        boolean hasLetter = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
                if (!Character.isUpperCase(c)) {
                    return false;
                }
            }
        }
        return hasLetter;
    }

    private static boolean isAllLower(String token) {
        boolean hasLetter = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
                if (!Character.isLowerCase(c)) {
                    return false;
                }
            }
        }
        return hasLetter;
    }

    private static boolean isAllDigits(String token) {
        for (int i = 0; i < token.length(); i++) {
            if (!Character.isDigit(token.charAt(i))) {
                return false;
            }
        }
        return !token.isEmpty();
    }

    private static String pascalizeChunk(String chunk) {
        if (chunk.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < chunk.length() && Character.isDigit(chunk.charAt(index))) {
            result.append(chunk.charAt(index));
            index++;
        }
        if (index < chunk.length()) {
            result.append(Character.toUpperCase(chunk.charAt(index)));
            index++;
            for (; index < chunk.length(); index++) {
                result.append(Character.toLowerCase(chunk.charAt(index)));
            }
        }
        return result.toString();
    }
}
