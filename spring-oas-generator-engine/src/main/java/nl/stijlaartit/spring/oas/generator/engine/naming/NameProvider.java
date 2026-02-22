package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathSegment;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.findShortestComponentPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.findShortestOperationPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.hasComponentPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.hasOperationPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.toPascalCase;

public class NameProvider {

    private final Set<String> usedNames = new LinkedHashSet<>();

    public static NameProvider create() {
        return new NameProvider();
    }


    public JavaTypeName resolveUniqueName(List<SchemaInstance> instances) {
        JavaTypeName resolvedName = resolveName(instances);
        String baseName = resolvedName.value();
        String uniqueName = baseName;
        int suffix = 2;
        while (usedNames.contains(uniqueName)) {
            uniqueName = baseName + suffix++;
        }
        usedNames.add(uniqueName);
        return new JavaTypeName.Generated(uniqueName);
    }

    private JavaTypeName resolveName(List<SchemaInstance> instances) {
        for (SchemaInstance instance : instances) {
            if (instance.path().root() instanceof PathRoot.ComponentSchema(
                    String name
            ) && instance.path().segments().isEmpty()) {
                return new JavaTypeName.Generated(toPascalCase(name));
            }
        }

        if (hasComponentPath(instances)) {
            SchemaPath componentPath = findShortestComponentPath(instances);
            return schemaPathToString(componentPath);
        }

        if (hasOperationPath(instances)) {
            SchemaPath operationPath = findShortestOperationPath(instances);
            return schemaPathToString(operationPath);
        }

        // TODO. Do we need to support this? Or just throw?
//        return new JavaTypeName.Generated(inlineName);
        throw new IllegalStateException("Could not resolve name for instances: " + instances);
    }

    private JavaTypeName schemaPathToString(SchemaPath path) {
        final List<String> rootTexts = switch (path.root()) {
            case PathRoot.ComponentParameter componentParameter -> List.of(componentParameter.name());
            case PathRoot.ComponentSchema componentSchema -> List.of(componentSchema.name());
            case PathRoot.RequestBody requestBody ->
                    concat(operationNameToStringParts(requestBody.operationName()), List.of("Request"));
            case PathRoot.RequestParam requestParam ->
                    concat(operationNameToStringParts(requestParam.operationName()), List.of("RequestParam", requestParam.paramName()));
            case PathRoot.ResponseBody responseBody ->
                    concat(operationNameToStringParts(responseBody.operationName()), List.of(responseBody.status(), "Response"));
            case PathRoot.SharedPathParam sharedPathParam -> List.of(sharedPathParam.path(), "Param", sharedPathParam.name());
        };

        final var segmentTexts = path.segments().stream().map(PathSegment::displayName).toList();
        List<String> allParts = concat(rootTexts, segmentTexts);
        return NamingUtil.toJavaTypeName(allParts);
    }

    private <T> List<T> concat(List<T> a, List<T> b) {
        final var answer = new ArrayList<>(a);
        answer.addAll(b);
        return answer;
    }

    private List<String> operationNameToStringParts(OperationName operationName) {
        return switch (operationName) {
            case OperationName.Id id -> List.of(id.value());
            case OperationName.PathAndMethod pathAndMethod ->
                    List.of(pathAndMethod.method().name(), pathAndMethod.path());
        };
    }
}
