package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaParent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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

    public ConcreteSchemaType resolveConcrete(Schema<?> schema) {
        final var schemaType = resolveFromSchema(schema);
        return resolveConcrete(schemaType);
    }

    public ConcreteSchemaType resolveConcrete(SchemaType schemaType) {
        return switch (schemaType) {
            case ConcreteSchemaType concrete -> concrete;
            case RefSchemaType ref -> resolveConcrete(resolveByRef(ref.ref()));
        };
    }

    private SchemaType resolveByRef(SchemaRef ref) {
        final Predicate<SchemaInstance> matcher;

        if (ref.type().equals("schemas")) {
            matcher = v -> v.parent() instanceof SchemaParent.ComponentParent(
                    String componentName
            ) && componentName.equals(ref.name());
        } else if (ref.type().equals("parameters")) {
            matcher = v -> v.parent() instanceof SchemaParent.ComponentParameterParent(
                    String componentName
            ) && componentName.equals(ref.name());
        } else {
            throw new IllegalArgumentException("Unsupported ref type: " + ref.type());
        }


        for (SchemaType type : types) {
            final var matches = type.instances().stream().anyMatch(matcher);
            if(matches) {
                return type;
            }
        }

        throw new IllegalStateException("Could not resolve ref schema type");
    }
}
