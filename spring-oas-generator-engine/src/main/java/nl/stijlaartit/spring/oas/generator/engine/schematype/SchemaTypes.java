package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

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

    public SchemaType resolveFromSchema(SimpleSchema schema) {
        for (SchemaType type : types) {
            for (var instance : type.instances()) {
                if (instance.schema() == schema) {
                    return type;
                }
            }
        }
        throw new IllegalStateException("Could not find schema in schema types");
    }

    public ConcreteSchemaType resolveConcrete(SimpleSchema schema) {
        final var schemaType = resolveFromSchema(schema);
        return resolveConcrete(schemaType);
    }

    public ConcreteSchemaType resolveConcrete(SchemaType schemaType) {
        return switch (schemaType) {
            case ConcreteSchemaType concrete -> concrete;
            case RefSchemaType ref -> resolveConcrete(resolveByRef(ref.ref()));
            case DeferredSchemaType deferredSchemaType -> resolveConcrete(deferredSchemaType.target());
        };
    }

    private SchemaType resolveByRef(SchemaRef ref) {
        final Predicate<SchemaInstance> matcher;

        if (ref.type().equals("schemas")) {
            matcher = v -> v.path().root() instanceof PathRoot.ComponentSchema(
                    String componentName
            ) && componentName.equals(ref.name()) && v.path().segments().isEmpty();
        } else if (ref.type().equals("parameters")) {
            matcher = v -> v.path().root() instanceof PathRoot.ComponentParameter(String componentName) &&
                    componentName.equals(ref.name()) && v.path().segments().isEmpty();
        } else {
            throw new IllegalArgumentException("Unsupported ref type: " + ref.type());
        }


        for (SchemaType type : types) {
            final var matches = type.instances().stream().anyMatch(matcher);
            if (matches) {
                return type;
            }
        }

        throw new IllegalStateException("Could not resolve ref schema type: " + ref);
    }
}
