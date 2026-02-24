package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.RefSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TypeDescriptorFactoryTest {

    @Test
    void buildsComplexTypeForGeneratedObject() {
        SimpleSchema user = new SimpleObjectSchema(false, List.of(new ObjectProperty("name", new SimpleStringSchema(false))), Set.of(), Optional.empty());
        TypeDescriptorFactory factory = createFactory(Map.of("User", user));

        TypeDescriptor type = factory.build(user);

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Generated("User"), complex.modelName());
    }

    @Test
    void buildsComplexTypeForGeneratedEnum() {
        SimpleSchema status = new SimpleStringSchema(false);
        TypeDescriptorFactory factory = createFactory(Map.of("Status", status));

        TypeDescriptor type = factory.build(status);

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Reserved("String"), complex.modelName());
    }

    @Test
    void buildsListQualifiedTypeWithResolvedItem() {
        SimpleSchema names = new SimpleArraySchema(false, new SimpleStringSchema(false));
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
        SimpleSchema attributes = new SimpleObjectSchema(false, List.of(), Set.of(), Optional.of(new SimpleStringSchema(false)));
        TypeDescriptorFactory factory = createFactory(Map.of("Attributes", attributes));

        TypeDescriptor type = factory.build(attributes);

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Generated("Attributes"), complex.modelName());
    }

    @Test
    void buildsComplexTypeFromRef() {
        SimpleSchema user = new SimpleObjectSchema(false, List.of(new ObjectProperty("name", new SimpleStringSchema(false))), Set.of(), Optional.empty());
        SimpleSchema ref = new RefSchema(false, new SchemaRef("schemas", "User"));
        TypeDescriptorFactory factory = createFactory(Map.of("User", user, "AltUser", ref));

        TypeDescriptor type = factory.build(ref);

        TypeDescriptor complex = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals(new JavaTypeName.Generated("User"), complex.modelName());
    }

    @Test
    void buildsQualifiedTypeForInlinePrimitive() {
        SimpleSchema name = new SimpleStringSchema(false);
        TypeDescriptorFactory factory = createFactory(Map.of("Name", name));

        TypeDescriptor type = factory.build(name);

        TypeDescriptor qualified = assertInstanceOf(TypeDescriptor.class, type);
        assertEquals("java.lang", qualified.packageName());
        assertEquals(new JavaTypeName.Reserved("String"), qualified.modelName());
    }

    @Test
    void buildsQualifiedSchemaWithPeriod() {
        SimpleSchema name = new SimpleObjectSchema(false, List.of(), Set.of(), Optional.empty());
        TypeDescriptorFactory factory = createFactory(Map.of(" FOO.Bar", name));

        TypeDescriptor descriptor = factory.build(name);
        assertThat(descriptor.modelName()).isEqualTo(new JavaTypeName.Generated("FooBar"));
    }

    private TypeDescriptorFactory createFactory(Map<String, SimpleSchema> components) {
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                new LinkedHashMap<>(components),
                Map.of(),
                List.of(),
                Map.of()
        );
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        return new TypeDescriptorFactory(schemaTypes, "com.example.models");
    }
}
