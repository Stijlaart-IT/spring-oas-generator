package nl.stijlaartit.spring.oas.generator.engine.schemas;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;

import java.util.Objects;

public record SchemaInstance(Schema<?> schema, SchemaPath path) {
    public SchemaInstance {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(path);
    }
}
