package nl.stijlaartit.spring.oas.generator.engine.model;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TypeDescriptorFactoryTest {

    @Test
    void buildsComplexTypeForGeneratedObject() {
        Schema<?> user = new ObjectSchema().addProperty("name", new StringSchema());
        TypeDescriptorFactory factory = createFactory(Map.of("User", user));

        TypeDescriptor type = factory.build(user);

        TypeDescriptor.ComplexType complex = assertInstanceOf(TypeDescriptor.ComplexType.class, type);
        assertEquals(new JavaTypeName.Generated("User"), complex.modelName());
    }

    @Test
    void buildsComplexTypeForGeneratedEnum() {
        StringSchema status = new StringSchema();
        status.setEnum(List.of("A", "B"));
        TypeDescriptorFactory factory = createFactory(Map.of("Status", status));

        TypeDescriptor type = factory.build(status);

        TypeDescriptor.ComplexType complex = assertInstanceOf(TypeDescriptor.ComplexType.class, type);
        assertEquals(new JavaTypeName.Generated("Status"), complex.modelName());
    }

    @Test
    void buildsListTypeWithResolvedItem() {
        Schema<?> names = new ArraySchema().items(new StringSchema());
        TypeDescriptorFactory factory = createFactory(Map.of("Names", names));

        TypeDescriptor type = factory.build(names);

        TypeDescriptor.ListType list = assertInstanceOf(TypeDescriptor.ListType.class, type);
        TypeDescriptor.SimpleType element = assertInstanceOf(TypeDescriptor.SimpleType.class, list.elementType());
        assertEquals("java.lang.String", element.qualifiedName());
    }

    @Test
    void buildsComplexTypeForComponentMap() {
        Schema<?> attributes = new ObjectSchema().additionalProperties(new StringSchema());
        TypeDescriptorFactory factory = createFactory(Map.of("Attributes", attributes));

        TypeDescriptor type = factory.build(attributes);

        TypeDescriptor.ComplexType complex = assertInstanceOf(TypeDescriptor.ComplexType.class, type);
        assertEquals(new JavaTypeName.Generated("Attributes"), complex.modelName());
    }

    @Test
    void buildsComplexTypeFromRef() {
        Schema<?> user = new ObjectSchema().addProperty("name", new StringSchema());
        Schema<?> ref = new Schema<>().$ref("#/components/schemas/User");
        TypeDescriptorFactory factory = createFactory(Map.of("User", user, "AltUser", ref));

        TypeDescriptor type = factory.build(ref);

        TypeDescriptor.ComplexType complex = assertInstanceOf(TypeDescriptor.ComplexType.class, type);
        assertEquals(new JavaTypeName.Generated("User"), complex.modelName());
    }

    @Test
    void buildsSimpleTypeForInlinePrimitive() {
        Schema<?> name = new StringSchema();
        TypeDescriptorFactory factory = createFactory(Map.of("Name", name));

        TypeDescriptor type = factory.build(name);

        TypeDescriptor.SimpleType simple = assertInstanceOf(TypeDescriptor.SimpleType.class, type);
        assertEquals("java.lang.String", simple.qualifiedName());
    }

    private TypeDescriptorFactory createFactory(Map<String, Schema<?>> components) {
        OpenAPI openAPI = new OpenAPI();
        Components comps = new Components();
        comps.setSchemas(new LinkedHashMap<>(components));
        openAPI.setComponents(comps);

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        return new TypeDescriptorFactory(schemaTypes);
    }
}
