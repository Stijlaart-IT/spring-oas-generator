package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record UnionSchemaType(List<SchemaInstance> instances,
                              JavaTypeName name,
                              List<SchemaInstance> variantInstances,
                              @Nullable String discriminatorProperty) implements GeneratedSchemaType {

    public UnionSchemaType {
        Objects.requireNonNull(instances);
        Objects.requireNonNull(name);
        Objects.requireNonNull(variantInstances);
    }

    public UnionSchemaType(List<SchemaInstance> instances, JavaTypeName name, List<SchemaInstance> variantInstances) {
        this(instances, name, variantInstances, null);
    }
}
