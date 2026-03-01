package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.RefSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;
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

class TypeInfoResolverTest {

    @Test
    void resolvesPrimitiveStringDescriptor() {
        SimpleSchema name = new SimpleStringSchema(false);
        TestContext context = createContext(Map.of("Name", name));

        TypeDescriptor descriptor = context.typeInfoResolver().get(name).typeDescriptor();

        assertThat(descriptor.packageName()).isEqualTo("java.lang");
        assertThat(descriptor.modelName()).isEqualTo(new JavaTypeName.Reserved("String"));
    }

    @Test
    void resolvesListDescriptorWithResolvedItemType() {
        SimpleSchema names = new SimpleArraySchema(false, new SimpleStringSchema(false));
        TestContext context = createContext(Map.of("Names", names));

        TypeDescriptor descriptor = context.typeInfoResolver().get(names).typeDescriptor();

        assertThat(descriptor.packageName()).isEqualTo("java.util");
        assertThat(descriptor.modelName()).isEqualTo(new JavaTypeName.Reserved("List"));
        assertThat(descriptor.generics()).singleElement()
                .isEqualTo(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")));
    }

    @Test
    void resolvesRefAndDeferredToTargetDescriptor() {
        SimpleSchema user = new SimpleObjectSchema(false, List.of(new ObjectProperty("name", new SimpleStringSchema(false))), Set.of(), Optional.empty());
        SimpleSchema ref = new RefSchema(false, new SchemaRef("schemas", "User"));
        SimpleSchema deferred = new CompositeSchema(false, List.of(ref));
        TestContext context = createContext(Map.of(
                "User", user,
                "AltUser", ref,
                "Wrapper", deferred
        ));

        TypeDescriptor refDescriptor = context.typeInfoResolver().get(ref).typeDescriptor();
        TypeDescriptor deferredDescriptor = context.typeInfoResolver().get(deferred).typeDescriptor();

        assertThat(refDescriptor.modelName()).isEqualTo(new JavaTypeName.Generated("User"));
        assertThat(deferredDescriptor.modelName()).isEqualTo(new JavaTypeName.Generated("User"));
    }

    @Test
    void compositeWithSingleResolvedComponentUsesComponentDescriptor() {
        SimpleSchema startDatum = new CompositeSchema(false, List.of(
                new SimpleStringSchema(false),
                new SimpleStringSchema(false)
        ));
        TestContext context = createContext(Map.of("StartDatum", startDatum));

        TypeInfo.CompositeTypeInfo info = assertInstanceOf(TypeInfo.CompositeTypeInfo.class, context.typeInfoResolver().get(startDatum));
        assertThat(info.typeDescriptor().packageName()).isEqualTo("java.lang");
        assertThat(info.typeDescriptor().modelName()).isEqualTo(new JavaTypeName.Reserved("String"));
        assertThat(info.properties()).isEmpty();
    }

    @Test
    void compositeWithMixedTypesFallsBackToObjectAndNoProperties() {
        SimpleSchema mixed = new CompositeSchema(false, List.of(
                new SimpleStringSchema(false),
                new SimpleIntegerSchema(false)
        ));
        TestContext context = createContext(Map.of("MixedAllOf", mixed));

        TypeInfo.CompositeTypeInfo info = assertInstanceOf(TypeInfo.CompositeTypeInfo.class, context.typeInfoResolver().get(mixed));
        assertThat(info.typeDescriptor())
                .isEqualTo(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object")));
        assertThat(info.properties()).isEmpty();
    }

    @Test
    void compositePropertiesFlattenRecursivelyAndPreserveOrder() {
        SimpleSchema playlistUserObject = new SimpleObjectSchema(
                false,
                List.of(
                        new ObjectProperty("external_urls", new SimpleStringSchema(false)),
                        new ObjectProperty("href", new SimpleStringSchema(false)),
                        new ObjectProperty("id", new SimpleStringSchema(false)),
                        new ObjectProperty("type", new SimpleStringSchema(false)),
                        new ObjectProperty("uri", new SimpleStringSchema(false))
                ),
                Set.of("external_urls", "href", "id", "type", "uri"),
                Optional.empty()
        );
        SimpleSchema playlistOwnerObject = new CompositeSchema(
                false,
                List.of(
                        playlistUserObject,
                        new SimpleObjectSchema(
                                false,
                                List.of(new ObjectProperty("display_name", new SimpleStringSchema(false))),
                                Set.of(),
                                Optional.empty()
                        )
                )
        );
        TestContext context = createContext(Map.of(
                "PlaylistUserObject", playlistUserObject,
                "PlaylistOwnerObject", playlistOwnerObject
        ));

        TypeInfo.CompositeTypeInfo info = assertInstanceOf(TypeInfo.CompositeTypeInfo.class, context.typeInfoResolver().get(playlistOwnerObject));

        TypeInfo.CompositeProperties properties = info.properties().orElseThrow();
        assertThat(properties.properties().keySet()).containsExactly(
                "external_urls", "href", "id", "type", "uri", "display_name"
        );
        assertThat(properties.requiredProperties()).containsExactlyInAnyOrder(
                "external_urls", "href", "id", "type", "uri"
        );
    }

    @Test
    void unionInfersDiscriminatorFromObjectAndCompositeVariants() {
        SimpleSchema car = new SimpleObjectSchema(
                false,
                List.of(
                        new ObjectProperty("type", new StringEnumSchema(false, List.of("car"))),
                        new ObjectProperty("brand", new SimpleStringSchema(false))
                ),
                Set.of(),
                Optional.empty()
        );
        SimpleSchema bike = new CompositeSchema(
                false,
                List.of(
                        new SimpleObjectSchema(
                                false,
                                List.of(new ObjectProperty("type", new StringEnumSchema(false, List.of("bike")))),
                                Set.of(),
                                Optional.empty()
                        ),
                        new SimpleObjectSchema(
                                false,
                                List.of(new ObjectProperty("model", new SimpleStringSchema(false))),
                                Set.of(),
                                Optional.empty()
                        )
                )
        );
        SimpleSchema vehicle = new UnionSchema(false, List.of(car, bike), "type");
        TestContext context = createContext(Map.of(
                "Car", car,
                "Bike", bike,
                "Vehicle", vehicle
        ));

        TypeInfo.UnionTypeInfo info = assertInstanceOf(TypeInfo.UnionTypeInfo.class, context.typeInfoResolver().get(vehicle));

        assertThat(info.discriminatorProperty()).isEqualTo("type");
        assertThat(info.variants()).extracting(TypeInfo.UnionVariantInfo::discriminatorValue).containsExactly("car", "bike");
    }

    @Test
    void unionWithAnyUnresolvedDiscriminatorClearsAllDiscriminatorInfo() {
        SimpleSchema car = new SimpleObjectSchema(
                false,
                List.of(
                        new ObjectProperty("type", new StringEnumSchema(false, List.of("car"))),
                        new ObjectProperty("brand", new SimpleStringSchema(false))
                ),
                Set.of(),
                Optional.empty()
        );
        SimpleSchema bike = new SimpleObjectSchema(
                false,
                List.of(
                        new ObjectProperty("type", new SimpleStringSchema(false)),
                        new ObjectProperty("model", new SimpleStringSchema(false))
                ),
                Set.of(),
                Optional.empty()
        );
        SimpleSchema vehicle = new UnionSchema(false, List.of(car, bike), "type");
        TestContext context = createContext(Map.of(
                "Car", car,
                "Bike", bike,
                "Vehicle", vehicle
        ));

        TypeInfo.UnionTypeInfo info = assertInstanceOf(TypeInfo.UnionTypeInfo.class, context.typeInfoResolver().get(vehicle));

        assertThat(info.discriminatorProperty()).isNull();
        assertThat(info.variants()).extracting(TypeInfo.UnionVariantInfo::discriminatorValue).containsOnlyNulls();
        assertEquals(2, info.variants().size());
    }

    private TestContext createContext(Map<String, SimpleSchema> components) {
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                new LinkedHashMap<>(components),
                Map.of(),
                List.of(),
                Map.of()
        );
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeInfoResolver typeInfoResolver = TypeInfoResolver.resolve(schemaTypes, "com.example.models");
        return new TestContext(schemaTypes, typeInfoResolver);
    }

    private record TestContext(SchemaTypes schemaTypes, TypeInfoResolver typeInfoResolver) {
    }
}
