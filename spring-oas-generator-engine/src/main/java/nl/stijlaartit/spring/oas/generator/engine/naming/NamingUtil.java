package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaParent;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class NamingUtil {

    private static final Set<String> RESERVED_TYPE_NAMES = Set.of(
            "Object", "Class", "String", "Integer", "Long", "Boolean", "Double", "Float",
            "Short", "Byte", "Character", "Number", "Void", "Enum"
    );

    private NamingUtil() {
    }

    public static PathName findShortestComponentPath(List<SchemaInstance> instances) {
        PathName best = null;
        int bestLength = Integer.MAX_VALUE;
        for (SchemaInstance instance : instances) {
            PathName path = resolvePathToComponent(instance);
            if (path == null) {
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
            if (resolvePathToComponent(instance) != null) {
                return true;
            }
        }
        return false;
    }

    public static PathName findShortestOperationPath(List<SchemaInstance> instances) {
        PathName best = null;
        int bestLength = Integer.MAX_VALUE;
        for (SchemaInstance instance : instances) {
            PathName path = resolvePathToOperation(instance);
            if (path == null) {
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
            if (resolvePathToOperation(instance) != null) {
                return true;
            }
        }
        return false;
    }

    private static PathName resolvePathToComponent(SchemaInstance instance) {
        List<String> segments = new ArrayList<>();
        SchemaInstance current = instance;
        while (current.parent() instanceof SchemaParent.SchemaInstanceParent(
                SchemaInstance parent, SchemaParent.SchemaRelation relation
        )) {
            String segment = resolveSegment(relation);
            if (segment != null && !segment.isBlank()) {
                segments.add(segment);
            }
            current = parent;
        }
        if (current.parent() instanceof SchemaParent.ComponentParent(String componentName)) {
            if (segments.isEmpty()) {
                return null;
            }
            Collections.reverse(segments);
            return new PathName(toPascalCase(componentName), segments);
        }
        if (current.parent() instanceof SchemaParent.ComponentParameterParent(String parameterName)) {
            Collections.reverse(segments);
            return new PathName(toPascalCase(parameterName) + "Parameter", segments);
        }
        return null;
    }

    private static PathName resolvePathToOperation(SchemaInstance instance) {
        List<String> segments = new ArrayList<>();
        SchemaInstance current = instance;
        int depth = 0;
        while (current.parent() instanceof SchemaParent.SchemaInstanceParent(
                SchemaInstance parent, SchemaParent.SchemaRelation relation
        )) {
            String segment = resolveSegment(relation);
            if (segment != null && !segment.isBlank()) {
                segments.add(segment);
            }
            current = parent;
            depth++;
        }
        if (current.parent() instanceof SchemaParent.OperationRequestParent(
                Operation operation, PathItem.HttpMethod method, String path
        )) {
            String base = toPascalCase(resolveOperationId(
                    operation, method, path)) + "Request";
            if (segments.isEmpty() && depth > 0) {
                return null;
            }
            Collections.reverse(segments);
            return new PathName(base, segments);
        }
        if (current.parent() instanceof SchemaParent.OperationResponseParent responseParent) {
            String base = toPascalCase(resolveOperationId(
                    responseParent.operation(), responseParent.method(), responseParent.path())) + "Response";
            if (segments.isEmpty() && depth > 0) {
                return null;
            }
            Collections.reverse(segments);
            return new PathName(base, segments);
        }
        if (current.parent() instanceof SchemaParent.OperationParameterParent(
                Operation operation, PathItem.HttpMethod method, String path, String parameterName, String parameterIn
        )) {
            String operationId = resolveOperationId(
                    operation, method, path);
            String base = toPascalCase(operationId)
                    + (parameterIn != null && !parameterIn.isBlank() ? toPascalCase(parameterIn) : "")
                    + (parameterName != null && !parameterName.isBlank() ? toPascalCase(parameterName) : "")
                    + "Parameter";
            if (segments.isEmpty() && depth > 0) {
                return null;
            }
            Collections.reverse(segments);
            return new PathName(base, segments);
        }
        return null;
    }

    private static String resolveOperationId(Operation operation, PathItem.HttpMethod method, String path) {
        if (operation != null && operation.getOperationId() != null
                && !operation.getOperationId().isBlank()) {
            return operation.getOperationId();
        }
        return OperationIdNaming.fallbackOperationId(method, path);
    }

    private static String resolveSegment(SchemaParent.SchemaRelation relation) {
        return switch (relation) {
            case SchemaParent.SchemaRelation.PropertyRelation propertyRelation -> propertyRelation.propertyName();
            case SchemaParent.SchemaRelation.ListItemRelation ignored -> "Item";
            case SchemaParent.SchemaRelation.OneOfRelation ignored -> null;
            case SchemaParent.SchemaRelation.AllOfRelation ignored -> null;
            case SchemaParent.SchemaRelation.AnyOfRelation ignored -> null;
            case SchemaParent.SchemaRelation.AdditionalPropertiesRelation ignored -> "AdditionalProperties";
        };
    }

    public static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Schema model name is blank.");
        }
        char first = name.charAt(0);
        if (first == '_' || !Character.isJavaIdentifierStart(first) || !Character.isUpperCase(first)) {
            throw new IllegalStateException("Invalid schema model name: " + name);
        }
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                throw new IllegalStateException("Invalid schema model name: " + name);
            }
        }
        if (RESERVED_TYPE_NAMES.contains(name)) {
            throw new IllegalStateException("Schema model name collides with reserved Java schema: " + name);
        }
        return name;
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

    public record PathName(String base, List<String> segments) {
        public String toName() {
            StringBuilder result = new StringBuilder(base);
            for (String segment : segments) {
                result.append(toPascalCase(segment));
            }
            return result.toString();
        }
    }
}
