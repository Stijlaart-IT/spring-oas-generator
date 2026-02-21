package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

public record SchemaInstanceGroups(List<SchemaInstanceGroup> groups) {
    public SchemaInstanceGroup groupForSchema(Schema v) {
        return groups
                .stream()
                .filter(g -> g.instances().stream().anyMatch(i -> i.schema().equals(v)))
                .findFirst()
                .orElseThrow();

    }
}
