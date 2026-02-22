package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
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

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Generated("User"), complex.modelName());
    }

    @Test
    void buildsComplexTypeForGeneratedEnum() {
        StringSchema status = new StringSchema();
        status.setEnum(List.of("A", "B"));
        TypeDescriptorFactory factory = createFactory(Map.of("Status", status));

        TypeDescriptor type = factory.build(status);

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Generated("Status"), complex.modelName());
    }

    @Test
    void buildsListQualifiedTypeWithResolvedItem() {
        Schema<?> names = new ArraySchema().items(new StringSchema());
        TypeDescriptorFactory factory = createFactory(Map.of("Names", names));

        TypeDescriptor type = factory.build(names);

        TypeDescriptor list = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals("java.util", list.packageName());
        assertEquals(new JavaTypeName.Reserved("List"), list.modelName());
        assertEquals(1, list.generics().size());
        TypeDescriptor element = assertInstanceOf(TypeDescriptor.class, list.generics().getFirst());
        assertEquals("java.lang", element.packageName());
        assertEquals(new JavaTypeName.Reserved("String"), element.modelName());
    }

    @Test
    void buildsComplexTypeForComponentMap() {
        Schema<?> attributes = new ObjectSchema().additionalProperties(new StringSchema());
        TypeDescriptorFactory factory = createFactory(Map.of("Attributes", attributes));

        TypeDescriptor type = factory.build(attributes);

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Generated("Attributes"), complex.modelName());
    }

    @Test
    void buildsComplexTypeFromRef() {
        Schema<?> user = new ObjectSchema().addProperty("name", new StringSchema());
        Schema<?> ref = new Schema<>().$ref("#/components/schemas/User");
        TypeDescriptorFactory factory = createFactory(Map.of("User", user, "AltUser", ref));

        TypeDescriptor type = factory.build(ref);

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Generated("User"), complex.modelName());
    }

    @Test
    void buildsQualifiedTypeForInlinePrimitive() {
        Schema<?> name = new StringSchema();
        TypeDescriptorFactory factory = createFactory(Map.of("Name", name));

        TypeDescriptor type = factory.build(name);

        TypeDescriptor qualified = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals("java.lang", qualified.packageName());
        assertEquals(new JavaTypeName.Reserved("String"), qualified.modelName());
    }

    private TypeDescriptorFactory createFactory(Map<String, Schema<?>> components) {
        OpenAPI openAPI = new OpenAPI();
        Components comps = new Components();
        comps.setSchemas(new LinkedHashMap<>(components));
        openAPI.setComponents(comps);

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        return new TypeDescriptorFactory(schemaTypes, "com.example.models");
    }
}
