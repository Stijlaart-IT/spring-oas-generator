package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

public final class SchemaTypes {

    private final List<SchemaType> types;

    public SchemaTypes(List<SchemaType> types) {
        this.types = List.copyOf(types);
    }

    public List<SchemaType> types() {
        return types;
    }

    public List<GeneratedSchemaType> generatedSchemaTypes() {
        List<GeneratedSchemaType> generated = new ArrayList<>();
        for (SchemaType type : types) {
            if (type instanceof GeneratedSchemaType generatedType) {
                generated.add(generatedType);
            }
        }
        return List.copyOf(generated);
    }

    public SchemaType resolveFromSchema(Schema<?> schema) {
        for (SchemaType type : types) {
            for (var instance : type.instances()) {
                if (instance.schema() == schema) {
                    return type;
                }
            }
        }
        throw new IllegalStateException("Could not find schema in schema types");
    }
}
