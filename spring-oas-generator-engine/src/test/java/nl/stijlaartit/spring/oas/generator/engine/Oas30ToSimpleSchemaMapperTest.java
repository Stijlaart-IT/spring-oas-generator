package nl.stijlaartit.spring.oas.generator.engine;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ParamIn;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.RefSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleAnySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBinarySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleLongSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleReponse;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOperation;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Oas30ToSimpleSchemaMapperTest {

    private final Oas30ToSimpleSchemaMapper mapper = new Oas30ToSimpleSchemaMapper();

    @Test
    void resolve_mapsComponentSchemasAndComponentParameters() {
        ObjectSchema petSchema = new ObjectSchema();
        petSchema.addProperty("id", new IntegerSchema());
        petSchema.addProperty("name", new StringSchema());
        petSchema.setRequired(List.of("id"));
        petSchema.setAdditionalProperties(new StringSchema());
        petSchema.setNullable(true);

        Schema<?> unionSchema = new Schema<>()
                .oneOf(List.of(new StringSchema(), new IntegerSchema()));
        unionSchema.setDiscriminator(new Discriminator().propertyName("type"));

        LinkedHashMap<String, Schema> schemaMap = new LinkedHashMap<>();
        schemaMap.put("Pet", petSchema);
        schemaMap.put("UnionType", unionSchema);

        Components components = new Components()
                .schemas(schemaMap)
                .parameters(new LinkedHashMap<>(Map.of(
                        "Limit", new Parameter()
                                .name("limit")
                                .in("query")
                                .schema(new IntegerSchema())
                )));

        OpenAPI openAPI = new OpenAPI().components(components);

        SimplifiedOas simplified = mapper.resolve(openAPI);

        assertEquals(2, simplified.componentSchema().size());
        assertEquals(1, simplified.componentParameters().size());
        assertTrue(simplified.operations().isEmpty());
        assertTrue(simplified.pathParams().isEmpty());

        SimpleSchema pet = simplified.componentSchema().get("Pet");
        SimpleObjectSchema simplePet = assertInstanceOf(SimpleObjectSchema.class, pet);
        assertTrue(simplePet.isNullable());
        assertEquals(2, simplePet.properties().size());
        ObjectProperty idProp = simplePet.properties().stream()
                .filter(p -> p.schema() instanceof SimpleIntegerSchema)
                .findFirst()
                .orElseThrow();
        assertThat(simplePet.requiredProperties()).contains(idProp.propertyName());
        assertInstanceOf(SimpleIntegerSchema.class, idProp.schema());
        assertThat(simplePet.additionalProperties()).isPresent();
        assertInstanceOf(SimpleStringSchema.class, simplePet.additionalProperties().orElseThrow());

        SimpleSchema union = simplified.componentSchema().get("UnionType");
        UnionSchema typedUnion = assertInstanceOf(UnionSchema.class, union);
        assertEquals(2, typedUnion.variants().size());
        assertEquals("type", typedUnion.discriminatorProperty());
    }

    @Test
    void resolve_mapsOperationsResponsesRequestBodyAndPathQueryAndHeaderParams() {
        Components components = new Components()
                .schemas(Map.of("Pet", new ObjectSchema().addProperty("name", new StringSchema())));

        Parameter pathParameter = new Parameter()
                .name("petId")
                .in("path")
                .required(true)
                .schema(new StringSchema());

        RequestBody requestBody = new RequestBody()
                .content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new ObjectSchema().addProperty("query", new StringSchema()))));

        ApiResponses responses = new ApiResponses()
                .addApiResponse("200", new ApiResponse().content(new Content().addMediaType("application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/Pet")))))
                .addApiResponse("204", new ApiResponse());

        Operation getPet = new Operation()
                .operationId("getPet")
                .requestBody(requestBody)
                .responses(responses)
                .parameters(List.of(
                        new Parameter().name("expand").in("query").schema(new StringSchema()),
                        new Parameter().name("x-api-key").in("header").schema(new StringSchema())
                ));

        OpenAPI openAPI = new OpenAPI()
                .components(components)
                .paths(new Paths().addPathItem("/pets/{petId}", new PathItem()
                        .parameters(List.of(pathParameter))
                        .get(getPet)));

        SimplifiedOas simplified = mapper.resolve(openAPI);

        assertEquals(1, simplified.operations().size());
        SimplifiedOperation operation = simplified.operations().getFirst();
        assertEquals("/pets/{petId}", operation.path());
        assertEquals(HttpMethod.GET, operation.method());
        assertEquals("getPet", operation.operationId());
        assertEquals(
                List.of(
                        new SimpleParam("expand", ParamIn.Query, new SimpleStringSchema(false), false),
                        new SimpleParam("x-api-key", ParamIn.Header, new SimpleStringSchema(false), false)
                ),
                operation.params()
        );
        assertThat(simplified.pathParams()).containsKey("/pets/{petId}");
        assertEquals(List.of(new SimpleParam("petId", ParamIn.Path, new SimpleStringSchema(false), true)), simplified.pathParams().get("/pets/{petId}"));

        assertEquals(1, operation.responses().size());
        SimpleReponse ok = operation.responses().getFirst();
        assertEquals("200", ok.status());
        RefSchema responseRef = assertInstanceOf(RefSchema.class, ok.schema());
        assertEquals(new SchemaRef("schemas", "Pet"), responseRef.ref());

        assertInstanceOf(SimpleObjectSchema.class, operation.requestBody());
    }

    @Test
    void resolve_mapsAdditionalPropertiesTrueToSimpleAnySchema() {
        ObjectSchema dictionarySchema = new ObjectSchema();
        dictionarySchema.setAdditionalProperties(true);

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Dictionary", dictionarySchema)));

        SimplifiedOas simplified = mapper.resolve(openAPI);
        SimpleObjectSchema mapped = assertInstanceOf(SimpleObjectSchema.class, simplified.componentSchema().get("Dictionary"));

        assertThat(mapped.additionalProperties()).contains(new SimpleAnySchema(false));
    }

    @Test
    void resolve_resolvesParameterReferencesFromComponents() {
        Components components = new Components()
                .parameters(Map.of(
                        "LimitParam",
                        new Parameter().name("limit").in("query").schema(new IntegerSchema())
                ));

        Operation search = new Operation()
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse()))
                .parameters(List.of(new Parameter().$ref("#/components/parameters/LimitParam")));

        OpenAPI openAPI = new OpenAPI()
                .components(components)
                .paths(new Paths().addPathItem("/search", new PathItem().get(search)));

        SimplifiedOas simplified = mapper.resolve(openAPI);
        SimplifiedOperation operation = simplified.operations().getFirst();

        assertEquals(List.of(new SimpleParam("limit", ParamIn.Query, new SimpleIntegerSchema(false), false)), operation.params());
    }

    @Test
    void resolve_mapsMixedAllOfAndAnyOfToSimpleAnySchemaAndLogsWarning() {
        Schema<?> mixedSchema = new Schema<>()
                .allOf(List.of(new StringSchema()))
                .anyOf(List.of(new IntegerSchema()));
        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Mixed", mixedSchema)));

        CapturingLogger logger = new CapturingLogger();
        Oas30ToSimpleSchemaMapper localMapper = new Oas30ToSimpleSchemaMapper(logger);
        SimplifiedOas simplified = localMapper.resolve(openAPI);

        SimpleSchema mapped = simplified.componentSchema().get("Mixed");
        assertInstanceOf(SimpleAnySchema.class, mapped);
        assertThat(logger.warnMessages).hasSize(1);
        assertThat(logger.warnMessages.getFirst()).contains("Mixed schema composition");
    }

    @Test
    void resolve_mapsMixedAllOfAndOneOfToSimpleAnySchemaAndLogsWarning() {
        Schema<?> mixedSchema = new Schema<>()
                .allOf(List.of(new StringSchema()))
                .oneOf(List.of(new IntegerSchema()));
        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Mixed", mixedSchema)));

        CapturingLogger logger = new CapturingLogger();
        Oas30ToSimpleSchemaMapper localMapper = new Oas30ToSimpleSchemaMapper(logger);
        SimplifiedOas simplified = localMapper.resolve(openAPI);

        SimpleSchema mapped = simplified.componentSchema().get("Mixed");
        assertInstanceOf(SimpleAnySchema.class, mapped);
        assertThat(logger.warnMessages).hasSize(1);
    }

    @Test
    void resolve_mapsArraySchemas() {
        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "PetIds", new ArraySchema().items(new IntegerSchema())
                )));

        SimplifiedOas simplified = mapper.resolve(openAPI);

        SimpleSchema mapped = simplified.componentSchema().get("PetIds");
        SimpleArraySchema arraySchema = assertInstanceOf(SimpleArraySchema.class, mapped);
        assertInstanceOf(SimpleIntegerSchema.class, arraySchema.itemSchema());
    }

    @Test
    void resolve_mapsIntegerInt64ToLongSchema() {
        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "Id64", new IntegerSchema().format("int64"),
                        "Id32", new IntegerSchema()
                )));

        SimplifiedOas simplified = mapper.resolve(openAPI);

        assertThat(simplified.componentSchema().get("Id64"))
                .isEqualTo(new SimpleLongSchema(false));
        assertThat(simplified.componentSchema().get("Id32"))
                .isEqualTo(new SimpleIntegerSchema(false));
    }

    @Test
    void resolve_mapsPrimitiveEnumsToEnumSchemas() {
        StringSchema stringEnum = new StringSchema();
        stringEnum.setEnum(List.of("A", "B"));
        IntegerSchema integerEnum = new IntegerSchema();
        integerEnum.setEnum(List.of(1, 2));
        Schema<Object> numberEnum = new Schema<>().type("number");
        numberEnum.setEnum(List.of(1.5, 2.5));

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "StringEnum", stringEnum,
                        "IntegerEnum", integerEnum,
                        "NumberEnum", numberEnum
                )));

        SimplifiedOas simplified = mapper.resolve(openAPI);

        assertThat(simplified.componentSchema().get("StringEnum"))
                .isEqualTo(new StringEnumSchema(false, List.of("A", "B")));
        assertThat(simplified.componentSchema().get("IntegerEnum"))
                .isEqualTo(new IntegerEnumSchema(false, List.of(1, 2)));
        assertThat(simplified.componentSchema().get("NumberEnum"))
                .isEqualTo(new NumberEnumSchema(false, List.of(new BigDecimal("1.5"), new BigDecimal("2.5"))));
    }

    @Test
    void resolve_mapsOctetStreamBinaryRequestBodyToBinarySchema() {
        RequestBody requestBody = new RequestBody()
                .content(new Content().addMediaType(
                        "application/octet-stream",
                        new MediaType().schema(new StringSchema().format("binary"))
                ));
        Operation upload = new Operation()
                .operationId("upload")
                .requestBody(requestBody)
                .responses(new ApiResponses().addApiResponse("204", new ApiResponse()));

        OpenAPI openAPI = new OpenAPI()
                .paths(new Paths().addPathItem("/upload", new PathItem().post(upload)));

        SimplifiedOas simplified = mapper.resolve(openAPI);
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
