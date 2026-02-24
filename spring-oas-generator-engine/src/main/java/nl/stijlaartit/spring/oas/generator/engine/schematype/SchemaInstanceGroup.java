package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SchemaInstanceGroup(SimpleSchema schema, List<SchemaInstance> instances) {
    public SchemaInstanceGroup {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(instances);
    }

    public static SchemaInstanceGroups groupBySchemaEquals(List<SchemaInstance> instances) {
        Map<SimpleSchema, List<SchemaInstance>> grouped = new LinkedHashMap<>();
        for (SchemaInstance instance : instances) {
            SimpleSchema schema = instance.schema();
            if (!grouped.containsKey(schema)) {
                grouped.put(schema, new ArrayList<>());
            }
            grouped.get(schema).add(instance);
        }

        List<SchemaInstanceGroup> groups = new ArrayList<>();
        for (Map.Entry<SimpleSchema, List<SchemaInstance>> entry : grouped.entrySet()) {
            ComponentSplit split = splitRootComponents(entry.getValue());
            if (split.roots().isEmpty()) {
                groups.add(new SchemaInstanceGroup(entry.getKey(), entry.getValue()));
            } else {
                final var firstInstances = new ArrayList<SchemaInstance>();
                firstInstances.add(split.roots().getFirst());
                firstInstances.addAll(split.others());
                groups.add(new SchemaInstanceGroup(entry.getKey(), firstInstances));

                for (SchemaInstance otherRoot : split.roots.stream().skip(1).toList()) {
                    groups.add(new SchemaInstanceGroup(entry.getKey(), List.of(otherRoot)));
                }
            }
        }
        return new SchemaInstanceGroups(groups);
    }

    private static ComponentSplit splitRootComponents(List<SchemaInstance> value) {
        List<SchemaInstance> roots = new ArrayList<>();
        List<SchemaInstance> others = new ArrayList<>();
        for (SchemaInstance schemaInstance : value) {
            switch (schemaInstance.path().root()) {
                case PathRoot.ComponentParameter ignored -> {
                    if (schemaInstance.path().segments().isEmpty()) {
                        roots.add(schemaInstance);
                    } else {
                        others.add(schemaInstance);
                    }
                }
                case PathRoot.ComponentSchema ignored -> {
                    if (schemaInstance.path().segments().isEmpty()) {
                        roots.add(schemaInstance);
                    } else {
                        others.add(schemaInstance);
                    }
                }
                case PathRoot.RequestBody ignored -> others.add(schemaInstance);
                case PathRoot.RequestParam ignored -> others.add(schemaInstance);
                case PathRoot.ResponseBody ignored -> others.add(schemaInstance);
                case PathRoot.SharedPathParam ignored -> others.add(schemaInstance);
            }
        }
        return new ComponentSplit(roots, others);
    }

    record ComponentSplit(List<SchemaInstance> roots, List<SchemaInstance> others) {

    }
}
