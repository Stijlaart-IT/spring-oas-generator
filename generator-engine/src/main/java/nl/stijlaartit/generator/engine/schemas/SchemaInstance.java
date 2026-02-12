package nl.stijlaartit.generator.engine.schemas;

import io.swagger.v3.oas.models.media.Schema;

import java.util.Objects;

public final class SchemaInstance {
    private final Schema<?> schema;
    private final SchemaParent parent;
    private final String jsonPath;

    public SchemaInstance(Schema<?> schema, SchemaParent parent, String jsonPath) {
        this.schema = Objects.requireNonNull(schema);
        this.parent = Objects.requireNonNull(parent);
        this.jsonPath = Objects.requireNonNull(jsonPath);
    }

    public Schema<?> getSchema() {
        return schema;
    }

    public SchemaParent getParent() {
        return parent;
    }

    public String getJsonPath() {
        return jsonPath;
    }
}
