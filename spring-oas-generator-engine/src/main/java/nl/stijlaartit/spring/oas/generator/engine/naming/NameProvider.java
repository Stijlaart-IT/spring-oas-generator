package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaParent;

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


    public JavaTypeName resolveUniqueName(List<SchemaInstance> instances, String inlineName) {
        JavaTypeName resolvedName = resolveName(instances, inlineName);
        String baseName = resolvedName.value();
        String uniqueName = baseName;
        int suffix = 2;
        while (usedNames.contains(uniqueName)) {
            uniqueName = baseName + suffix++;
        }
        usedNames.add(uniqueName);
        return new JavaTypeName.Generated(uniqueName);
    }

    private JavaTypeName resolveName(List<SchemaInstance> instances, String inlineName) {
        for (SchemaInstance instance : instances) {
            if (instance.parent() instanceof SchemaParent.ComponentParent(String componentName)) {
                return new JavaTypeName.Generated(toPascalCase(componentName));
            }
        }

        if (hasComponentPath(instances)) {
            NamingUtil.PathName componentPath = findShortestComponentPath(instances);
            return new JavaTypeName.Generated(componentPath.toName());
        }

        if (hasOperationPath(instances)) {
            NamingUtil.PathName operationPath = findShortestOperationPath(instances);
            return new JavaTypeName.Generated(operationPath.toName());
        }

        return new JavaTypeName.Generated(inlineName);
    }

}
