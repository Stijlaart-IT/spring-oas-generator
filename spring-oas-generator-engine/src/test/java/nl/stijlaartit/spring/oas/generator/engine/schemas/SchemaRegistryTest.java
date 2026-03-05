package nl.stijlaartit.spring.oas.generator.engine.schemas;

import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathSegment;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ParamIn;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ResponseMediaType;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleAnySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleReponse;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOperation;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaRegistryTest {

    @Test
    void collectsComponentRootSchema() {
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                Map.of("User", new SimpleStringSchema(false)),
                Map.of(),
                Map.of(),
                List.of(),
                Map.of()
        );

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        SchemaInstance instance = findByRootType(registry, PathRoot.ComponentSchema.class);
        assertNotNull(instance);
        PathRoot.ComponentSchema root = (PathRoot.ComponentSchema) instance.path().root();
        assertEquals("User", root.name());
    }

    @Test
    void collectsNestedComponentSchemas() {
        SimpleSchema nested = new SimpleAnySchema(false);
        SimpleSchema rootSchema = objectSchema(new ObjectProperty("nested", nested));
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                Map.of("User", rootSchema),
                Map.of(),
                Map.of(),
                List.of(),
                Map.of()
        );

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        SchemaInstance nestedInstance = registry.getInstances().stream()
                .filter(instance -> instance.path().segments().stream().anyMatch(PathSegment.Property.class::isInstance))
                .findFirst()
                .orElseThrow();
        assertInstanceOf(PathRoot.ComponentSchema.class, nestedInstance.path().root());
        PathRoot.ComponentSchema root = (PathRoot.ComponentSchema) nestedInstance.path().root();
        assertEquals("User", root.name());
    }

    @Test
    void collectsAllOfSchemas() {
        SimpleSchema wrapper = new CompositeSchema(false, List.of(new SimpleStringSchema(false), new SimpleAnySchema(false)));
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                Map.of("Wrapper", wrapper),
                Map.of(),
                Map.of(),
                List.of(),
                Map.of()
        );

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        long allOfChildren = registry.getInstances().stream()
                .filter(instance -> instance.path().segments().stream().anyMatch(PathSegment.Variant.class::isInstance))
                .count();
        assertThat(allOfChildren).isEqualTo(2);
    }

    @Test
    void collectsOneOfSchemas() {
        SimpleSchema wrapper = new UnionSchema(false, List.of(new SimpleStringSchema(false), new SimpleAnySchema(false)), null);
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                Map.of("Wrapper", wrapper),
                Map.of(),
                Map.of(),
                List.of(),
                Map.of()
        );

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        long oneOfChildren = registry.getInstances().stream()
                .filter(instance -> instance.path().segments().stream().anyMatch(PathSegment.Variant.class::isInstance))
                .count();
        assertThat(oneOfChildren).isEqualTo(2);
    }

    @Test
    void collectsAdditionalPropertiesSchemas() {
        SimpleSchema schema = new nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema(
                false,
                List.of(),
                Set.of(),
                Optional.of(new SimpleStringSchema(false))
        );
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                Map.of("Container", schema),
                Map.of(),
                Map.of(),
                List.of(),
                Map.of()
        );

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        assertThat(registry.getInstances().stream()
                .anyMatch(instance -> instance.path().segments().stream().anyMatch(PathSegment.AdditionalProperties.class::isInstance)))
                .isTrue();
    }

    @Test
    void collectsComponentParameterSchemas() {
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                Map.of(),
                Map.of(),
                Map.of("Limit", new SimpleStringSchema(false)),
                List.of(),
                Map.of()
        );

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        SchemaInstance instance = findByRootType(registry, PathRoot.ComponentParameter.class);
        assertNotNull(instance);
        PathRoot.ComponentParameter root = (PathRoot.ComponentParameter) instance.path().root();
        assertEquals("Limit", root.name());
    }

    @Test
    void collectsRequestBodySchemas() {
        SimplifiedOperation operation = new SimplifiedOperation(
                "/users",
                HttpMethod.POST,
                null,
                Set.of("default"),
                List.of(),
                List.of(),
                new SimpleStringSchema(false)
        );
        SimplifiedOas simplifiedOas = new SimplifiedOas(Map.of(), Map.of(), Map.of(), List.of(operation), Map.of());

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        SchemaInstance instance = findByRootType(registry, PathRoot.RequestBody.class);
        assertNotNull(instance);
        PathRoot.RequestBody root = (PathRoot.RequestBody) instance.path().root();
        assertThat(root.operationName()).isEqualTo(OperationName.pathAndMethod("/users", HttpMethod.POST));
    }

    @Test
    void collectsResponseBodySchemasWithStatus() {
        SimplifiedOperation operation = new SimplifiedOperation(
                "/users",
                HttpMethod.POST,
                null,
                Set.of("default"),
                List.of(),
                List.of(new SimpleReponse("404", new SimpleStringSchema(false), ResponseMediaType.APPLICATION_JSON)),
                null
        );
        SimplifiedOas simplifiedOas = new SimplifiedOas(Map.of(), Map.of(), Map.of(), List.of(operation), Map.of());

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        SchemaInstance instance = findByRootType(registry, PathRoot.ResponseBody.class);
        assertNotNull(instance);
        PathRoot.ResponseBody root = (PathRoot.ResponseBody) instance.path().root();
        assertThat(root.operationName()).isEqualTo(OperationName.pathAndMethod("/users", HttpMethod.POST));
        assertThat(root.status()).isEqualTo("404");
    }

    @Test
    void collectsOperationParameterSchemas() {
        SimplifiedOperation operation = new SimplifiedOperation(
                "/users",
                HttpMethod.POST,
                null,
                Set.of("default"),
                List.of(new SimpleParam("limit", ParamIn.Query, new SimpleStringSchema(false), false)),
                List.of(),
                null
        );
        SimplifiedOas simplifiedOas = new SimplifiedOas(Map.of(), Map.of(), Map.of(), List.of(operation), Map.of());

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        SchemaInstance instance = findByRootType(registry, PathRoot.RequestParam.class);
        assertNotNull(instance);
        PathRoot.RequestParam root = (PathRoot.RequestParam) instance.path().root();
        assertThat(root.operationName()).isEqualTo(OperationName.pathAndMethod("/users", HttpMethod.POST));
        assertThat(root.paramName()).isEqualTo("limit");
    }

    @Test
    void collectsSharedPathParameterSchemas() {
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                Map.of(),
                Map.of(),
                Map.of(),
                List.of(),
                Map.of("/users/{id}", List.of(new SimpleParam("id", ParamIn.Path, new SimpleStringSchema(false), true)))
        );

        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);

        SchemaInstance instance = findByRootType(registry, PathRoot.SharedPathParam.class);
        assertNotNull(instance);
        PathRoot.SharedPathParam root = (PathRoot.SharedPathParam) instance.path().root();
        assertThat(root.path()).isEqualTo("/users/{id}");
        assertThat(root.name()).isEqualTo("id");
    }

    @Test
    void throwsOnReferenceCycle() {
        List<SimpleSchema> components = new ArrayList<>();
        CompositeSchema schema = new CompositeSchema(false, components);
        components.add(schema);
        SimplifiedOas simplifiedOas = new SimplifiedOas(Map.of("Node", schema), Map.of(), Map.of(), List.of(), Map.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> SchemaRegistry.resolve(simplifiedOas));
        assertTrue(ex.getMessage().contains("cycle"));
    }

    private static SchemaInstance findByRootType(SchemaRegistry registry, Class<? extends PathRoot> rootType) {
        return registry.getInstances().stream()
                .filter(instance -> rootType.isInstance(instance.path().root()))
                .findFirst()
                .orElse(null);
    }

    private static nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema objectSchema(ObjectProperty... properties) {
        return new nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema(
                false,
                List.of(properties),
                Set.of(),
                Optional.empty()
        );
    }
}
