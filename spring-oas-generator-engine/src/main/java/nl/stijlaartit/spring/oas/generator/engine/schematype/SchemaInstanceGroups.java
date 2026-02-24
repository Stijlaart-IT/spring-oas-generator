package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;

import java.util.List;

public record SchemaInstanceGroups(List<SchemaInstanceGroup> groups) {
    public SchemaInstanceGroup groupForSchema(SimpleSchema v) {
        return groups
                .stream()
                .filter(g -> g.instances().stream().anyMatch(i -> i.schema().equals(v)))
                .findFirst()
                .orElseThrow();

    }
}
