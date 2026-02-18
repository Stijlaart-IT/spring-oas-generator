package nl.stijlaartit.generator.engine.model;

import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.UnionModelFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ImplementsByMapping {
    private final Map<String, List<String>> mappings;

    public ImplementsByMapping(Map<String, List<String>> mappings) {
        this.mappings = mappings;
    }


    public List<String> parentInterfacesForName(String name) {
        return mappings.getOrDefault(name, List.of());
    }

    public static ImplementsByMapping create(List<GenerationFile> models) {
        Map<String, List<String>> implementsByModel = new HashMap<>();
        for (GenerationFile model : models) {
            if (!(model instanceof UnionModelFile unionModel)) {
                continue;
            }
            for (OneOfVariant variant : unionModel.variants()) {
                implementsByModel
                        .computeIfAbsent(variant.modelName(), key -> new ArrayList<>())
                        .add(unionModel.name());
            }
        }
        for (Map.Entry<String, List<String>> entry : implementsByModel.entrySet()) {
            List<String> distinct = new ArrayList<>(new LinkedHashSet<>(entry.getValue()));
            entry.setValue(distinct);
        }
        return new ImplementsByMapping(implementsByModel);
    }

    public static ImplementsByMapping empty() {
        return new ImplementsByMapping(Map.of());
    }
}
