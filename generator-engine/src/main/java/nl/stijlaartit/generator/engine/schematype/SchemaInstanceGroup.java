package nl.stijlaartit.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SchemaInstanceGroup {
    private final Schema<?> schema;
    private final List<SchemaInstance> instances;

    public SchemaInstanceGroup(Schema<?> schema, List<SchemaInstance> instances) {
        this.schema = Objects.requireNonNull(schema);
        this.instances = List.copyOf(instances);
    }

    public Schema<?> schema() {
        return schema;
    }

    public List<SchemaInstance> instances() {
        return instances;
    }

    public static List<SchemaInstanceGroup> groupBySchemaEquals(List<SchemaInstance> instances) {
        Map<Schema<?>, List<SchemaInstance>> grouped = new LinkedHashMap<>();
        for (SchemaInstance instance : instances) {
            Schema<?> schema = instance.getSchema();
            Schema<?> key = findExistingKey(grouped, schema);
            if (key == null) {
                grouped.put(schema, new ArrayList<>());
                key = schema;
            }
            grouped.get(key).add(instance);
        }

        List<SchemaInstanceGroup> groups = new ArrayList<>();
        for (Map.Entry<Schema<?>, List<SchemaInstance>> entry : grouped.entrySet()) {
            groups.add(new SchemaInstanceGroup(entry.getKey(), entry.getValue()));
        }
        return groups;
    }

    private static @Nullable Schema<?> findExistingKey(Map<Schema<?>, List<SchemaInstance>> grouped,
                                                       Schema<?> schema) {
        for (Schema<?> key : grouped.keySet()) {
            if (key.equals(schema)) {
                return key;
            }
        }
        return null;
    }
}
