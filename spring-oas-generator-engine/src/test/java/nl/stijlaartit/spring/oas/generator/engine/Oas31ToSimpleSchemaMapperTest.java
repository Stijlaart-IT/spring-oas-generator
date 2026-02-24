package nl.stijlaartit.spring.oas.generator.engine;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ParamIn;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.RefSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleAnySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBinarySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleLongSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Oas31ToSimpleSchemaMapperTest {

    @Test
    void resolve_mapsStringUsingTypesAndNullabilityFromTypes() {
        Schema<?> schema = new Schema<>();
        schema.setTypes(new LinkedHashSet<>(List.of("string", "null")));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("Name", schema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);

        SimpleSchema mapped = simplified.componentSchema().get("Name");
        SimpleStringSchema stringSchema = assertInstanceOf(SimpleStringSchema.class, mapped);
        assertTrue(stringSchema.isNullable());
    }

    @Test
    void resolve_mapsMixedCompositionToSimpleAnySchemaAndLogsWarning() {
        Schema<?> schema = new Schema<>();
        schema.setAllOf(List.of(new Schema<>().type("string")));
        schema.setOneOf(List.of(new Schema<>().type("integer")));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("Mixed", schema)));

        CapturingLogger logger = new CapturingLogger();
        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper(logger).resolve(openAPI);

        SimpleSchema mapped = simplified.componentSchema().get("Mixed");
        assertInstanceOf(SimpleAnySchema.class, mapped);
        assertThat(logger.warnMessages).hasSize(1);
        assertThat(logger.warnMessages.getFirst()).contains("Mixed schema composition");
    }

    @Test
    void resolve_treatsNullOneOfVariantAsOptionalAndNotAsVariant() {
        Schema<?> schema = new Schema<>();
        schema.setOneOf(List.of(
                new Schema<>().type("string"),
                new Schema<>().type("null")
        ));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("NameOrNull", schema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);

        SimpleSchema mapped = simplified.componentSchema().get("NameOrNull");
        UnionSchema union = assertInstanceOf(UnionSchema.class, mapped);
        assertTrue(union.isNullable());
        assertThat(union.variants()).hasSize(1);
        assertInstanceOf(SimpleStringSchema.class, union.variants().getFirst());
    }

    @Test
    void resolve_mapsUnionDiscriminatorProperty() {
        Schema<?> schema = new Schema<>();
        schema.setOneOf(List.of(
                new Schema<>().type("string"),
                new Schema<>().type("integer")
        ));
        schema.setDiscriminator(new Discriminator().propertyName("type"));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("Union", schema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);

        SimpleSchema mapped = simplified.componentSchema().get("Union");
        UnionSchema union = assertInstanceOf(UnionSchema.class, mapped);
        assertThat(union.discriminatorProperty()).isEqualTo("type");
    }

    @Test
    void resolve_ignoresGetNullableForOas31() {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        schema.setNullable(true);

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("LegacyNullable", schema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        SimpleSchema mapped = simplified.componentSchema().get("LegacyNullable");
        SimpleStringSchema stringSchema = assertInstanceOf(SimpleStringSchema.class, mapped);
        assertTrue(!stringSchema.isNullable());
    }

    @Test
    void resolve_mapsArraySchemasUsingTypeInformation() {
        ArraySchema schema = new ArraySchema();
        schema.setItems(new Schema<>().type("integer"));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("Numbers", schema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        SimpleSchema mapped = simplified.componentSchema().get("Numbers");
        SimpleArraySchema arraySchema = assertInstanceOf(SimpleArraySchema.class, mapped);
        assertInstanceOf(SimpleIntegerSchema.class, arraySchema.itemSchema());
    }

    @Test
    void resolve_mapsIntegerInt64ToLongSchema() {
        Schema<Object> int64 = new Schema<>().type("integer").format("int64");
        Schema<Object> integerDefault = new Schema<>().type("integer");

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of(
                        "Id64", int64,
                        "Id32", integerDefault
                )));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        assertThat(simplified.componentSchema().get("Id64"))
                .isEqualTo(new SimpleLongSchema(false));
        assertThat(simplified.componentSchema().get("Id32"))
                .isEqualTo(new SimpleIntegerSchema(false));
    }

    @Test
    void resolve_mapsRefSchemaWithRefValue() {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/User");

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("UserRef", schema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        SimpleSchema mapped = simplified.componentSchema().get("UserRef");
        RefSchema refSchema = assertInstanceOf(RefSchema.class, mapped);
        assertThat(refSchema.ref()).isEqualTo(new SchemaRef("schemas", "User"));
    }

    @Test
    void resolve_mapsPrimitiveEnumsToEnumSchemas() {
        Schema<Object> stringEnum = new Schema<>().type("string");
        stringEnum.setEnum(List.of("A", "B"));
        Schema<Object> integerEnum = new Schema<>().type("integer");
        integerEnum.setEnum(List.of(1, 2));
        Schema<Object> numberEnum = new Schema<>().type("number");
        numberEnum.setEnum(List.of(1.5, 2.5));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of(
                        "StringEnum", stringEnum,
                        "IntegerEnum", integerEnum,
                        "NumberEnum", numberEnum
                )));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);

        assertThat(simplified.componentSchema().get("StringEnum"))
                .isEqualTo(new StringEnumSchema(false, List.of("A", "B")));
        assertThat(simplified.componentSchema().get("IntegerEnum"))
                .isEqualTo(new IntegerEnumSchema(false, List.of(1, 2)));
        assertThat(simplified.componentSchema().get("NumberEnum"))
                .isEqualTo(new NumberEnumSchema(false, List.of(new BigDecimal("1.5"), new BigDecimal("2.5"))));
    }

    @Test
    void resolve_mapsHeaderParameters() {
        Operation operation = new Operation()
                .operationId("search")
                .parameters(List.of(new Parameter().name("x-api-key").in("header").schema(new StringSchema())))
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse()));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .paths(new Paths().addPathItem("/search", new PathItem().get(operation)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        assertThat(simplified.operations()).hasSize(1);
        assertThat(simplified.operations().getFirst().params())
                .containsExactly(new SimpleParam("x-api-key", ParamIn.Header, new SimpleStringSchema(false), false));
    }

    @Test
    void resolve_mapsAdditionalPropertiesTrueToSimpleAnySchema() {
        Schema<Object> dictionarySchema = new Schema<>().type("object");
        dictionarySchema.setAdditionalProperties(true);

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("Dictionary", dictionarySchema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        SimpleObjectSchema mapped = assertInstanceOf(SimpleObjectSchema.class, simplified.componentSchema().get("Dictionary"));

        assertThat(mapped.additionalProperties()).contains(new SimpleAnySchema(false));
    }

    @Test
    void resolve_mapsObjectRequiredProperties() {
        Schema<Object> userSchema = new Schema<>().type("object");
        userSchema.setProperties(Map.of("id", new Schema<>().type("integer")));
        userSchema.setRequired(List.of("id"));

        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .components(new Components().schemas(Map.of("User", userSchema)));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        SimpleObjectSchema mapped = assertInstanceOf(SimpleObjectSchema.class, simplified.componentSchema().get("User"));

        assertThat(mapped.requiredProperties()).containsExactly("id");
    }

    @Test
    void resolve_mapsOctetStreamBinaryRequestBodyToBinarySchema() {
        OpenAPI openAPI = new OpenAPI()
                .openapi("3.1.0")
                .paths(new io.swagger.v3.oas.models.Paths().addPathItem(
                        "/upload",
                        new io.swagger.v3.oas.models.PathItem().post(
                                new io.swagger.v3.oas.models.Operation()
                                        .operationId("upload")
                                        .requestBody(new io.swagger.v3.oas.models.parameters.RequestBody()
                                                .content(new io.swagger.v3.oas.models.media.Content().addMediaType(
                                                        "application/octet-stream",
                                                        new io.swagger.v3.oas.models.media.MediaType().schema(
                                                                new Schema<>().type("string").format("binary")
                                                        )
                                                )))
                                        .responses(new io.swagger.v3.oas.models.responses.ApiResponses()
                                                .addApiResponse("204", new io.swagger.v3.oas.models.responses.ApiResponse()))
                        )));

        SimplifiedOas simplified = new Oas31ToSimpleSchemaMapper().resolve(openAPI);
        assertThat(simplified.operations()).hasSize(1);
        assertThat(simplified.operations().getFirst().requestBody())
                .isEqualTo(new SimpleBinarySchema(false));
    }

    private static final class CapturingLogger implements Logger {
        private final List<String> warnMessages = new ArrayList<>();

        @Override
        public void debug(String message) {
        }

        @Override
        public void debug(String message, Throwable error) {
        }

        @Override
        public void info(String message) {
        }

        @Override
        public void info(String message, Throwable error) {
        }

        @Override
        public void warn(String message) {
            warnMessages.add(message);
        }

        @Override
        public void warn(String message, Throwable error) {
            warnMessages.add(message);
        }

        @Override
        public void error(String message) {
        }

        @Override
        public void error(String message, Throwable error) {
        }
    }
}
