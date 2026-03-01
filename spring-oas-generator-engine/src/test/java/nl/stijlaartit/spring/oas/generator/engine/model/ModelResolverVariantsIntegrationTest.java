package nl.stijlaartit.spring.oas.generator.engine.model;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.spring.oas.generator.domain.file.ModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordField;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordModel;
import nl.stijlaartit.spring.oas.generator.domain.file.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.engine.OasSimplifier;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelResolverVariantsIntegrationTest {

    @Test
    void resolvesRequiredAndNullable_optionalNullableIsNullableAndNotRequired() {
        List<ModelFile> models = resolveModels(parseVariantsSpec());
        RecordModel requiredAndNullable = findRecord(models, "RequiredAndNullable").orElseThrow();
        RecordField optionalNullable = findField(requiredAndNullable, "optionalNullable");

        assertFalse(optionalNullable.required());
        assertTrue(optionalNullable.nullable());
    }

    @Test
    void resolvesPagingModels_generatesPagingObjectAndPagingThingObject() {
        List<ModelFile> models = resolveModels(parseVariantsSpec());

        assertThat(models)
                .extracting(ModelFile::name)
                .contains("PagingObject", "PagingThingObject");

        RecordModel pagingObject = models.stream().filter(v -> v.name().equals("PagingObject")).map((v) -> (RecordModel) v).findFirst().orElseThrow();
        RecordModel pagingThingObject = models.stream().filter(v -> v.name().equals("PagingThingObject")).map((v) -> (RecordModel) v).findFirst().orElseThrow();

        assertThat(pagingObject.fields()).allMatch(RecordField::required, "Field should be required");
        assertThat(pagingThingObject.fields()).allMatch(RecordField::required, "Field should be required");
    }

    @Test
    void resolvesVehicleUnion_discriminatorIsType() {
        List<ModelFile> models = resolveModels(parseVariantsSpec());

        UnionModelFile vehicle = models.stream()
                .filter(model -> model instanceof UnionModelFile)
                .map(model -> (UnionModelFile) model)
                .filter(model -> model.name().equals("Vehicle"))
                .findFirst()
                .orElseThrow();

        assertThat(vehicle.discriminatorProperty()).isEqualTo("type");
        assertThat(vehicle.variants()).extracting(v -> v.modelName().value()).containsExactly("Car", "Bike");
        assertThat(vehicle.variants()).extracting(v -> v.discriminatorValue()).containsExactly("car", "bike");
    }

    private List<ModelFile> resolveModels(OpenAPI openAPI) {
        OasSimplifier oasSimplifier = new OasSimplifier(Logger.noOp());
        SimplifiedOas simplifiedOas = oasSimplifier.simplify(openAPI);
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeInfoResolver typeInfoResolver = TypeInfoResolver.resolve(schemaTypes, "com.example.models");
        ModelResolver resolver = new ModelResolver(schemaTypes, typeInfoResolver, Logger.noOp());
        return resolver.resolve();
    }

    private OpenAPI parseVariantsSpec() {
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation("../examples/variants.yml", null, null);
        return Objects.requireNonNull(result.getOpenAPI(), "Could not parse ../examples/variants.yml");
    }

    private Optional<RecordModel> findRecord(List<ModelFile> models, String name) {
        return models.stream()
                .filter(model -> model instanceof RecordModel)
                .map(model -> (RecordModel) model)
                .filter(model -> model.name().equals(name))
                .findFirst();
    }

    private RecordField findField(RecordModel model, String name) {
        return model.fields().stream()
                .filter(field -> field.name().value().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
