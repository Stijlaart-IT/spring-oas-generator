package nl.stijlaartit.spring.oas.generator.serialization;

import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;
import nl.stijlaartit.spring.oas.generator.domain.file.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.domain.file.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ImplementsByMapping {
    private final Map<JavaTypeName, List<JavaTypeName>> mappings;

    public ImplementsByMapping(Map<JavaTypeName, List<JavaTypeName>> mappings) {
        this.mappings = mappings;
    }


    public List<JavaTypeName> parentInterfacesForName(JavaTypeName name) {
        return mappings.getOrDefault(name, List.of());
    }

    public static ImplementsByMapping create(List<GenerationFile> models) {
        Map<JavaTypeName, List<JavaTypeName>> implementsByModel = new HashMap<>();
        for (GenerationFile model : models) {
            if (!(model instanceof UnionModelFile unionModel)) {
                continue;
            }
            for (OneOfVariant variant : unionModel.variants()) {
                implementsByModel
                        .computeIfAbsent(variant.modelName(), key -> new ArrayList<>())
                        .add(unionModel.typeName());
            }
        }
        for (Map.Entry<JavaTypeName, List<JavaTypeName>> entry : implementsByModel.entrySet()) {
            List<JavaTypeName> distinct = new ArrayList<>(new LinkedHashSet<>(entry.getValue()));
            entry.setValue(distinct);
        }
        return new ImplementsByMapping(implementsByModel);
    }

    public static ImplementsByMapping empty() {
        return new ImplementsByMapping(Map.of());
    }
}
