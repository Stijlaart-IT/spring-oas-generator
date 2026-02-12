package nl.stijlaartit.generator.engine.model;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.generator.engine.domain.FieldModel;
import nl.stijlaartit.generator.engine.domain.ModelFile;
import nl.stijlaartit.generator.engine.domain.RecordModel;
import nl.stijlaartit.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InlineAllOfExternalUrlsTest {

    @Test
    void modelResolverUsesRefTypeForSingleAllOfProperty() {
        List<ModelFile> models = resolveModels();

        assertTrue(models.stream().noneMatch(model -> model.getName().equals("AlbumBaseExternalUrls")));

        RecordModel albumBase = findRecord(models, "AlbumBase");
        FieldModel externalUrls = findField(albumBase, "externalUrls");
        assertTrue(externalUrls.getType() instanceof TypeDescriptor.ComplexType);
        TypeDescriptor.ComplexType type = (TypeDescriptor.ComplexType) externalUrls.getType();
        assertEquals("ExternalUrlObject", type.modelName());
    }

    @Test
    void modelWriterEmitsRefTypeForSingleAllOfProperty() {
        List<ModelFile> models = resolveModels();
        RecordModel albumBase = findRecord(models, "AlbumBase");

        ModelWriter writer = new ModelWriter("com.example.models", RecordModelWriterConfig.defaultConfig());
        String source = writer.toJavaFile(albumBase, Map.of()).toString();

        assertTrue(source.contains("ExternalUrlObject externalUrls"));
        assertFalse(source.contains("AlbumBaseExternalUrls"));
    }

    private List<ModelFile> resolveModels() {
        OpenAPI openAPI = parseSpec();
        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        SchemaTypes schemaTypes = new SchemaTypeResolver().resolve(registry);
        ModelResolver resolver = new ModelResolver(registry);
        return resolver.resolve(schemaTypes);
    }

    private OpenAPI parseSpec() {
        URL resource = InlineAllOfExternalUrlsTest.class
                .getResource("/openapi/inline-allof-external-urls.yml");
        assertNotNull(resource, "Missing test OpenAPI spec resource");
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(resource.toString(), null, null);
        if (result.getMessages() != null && !result.getMessages().isEmpty()) {
            fail("Failed to parse OpenAPI spec: " + String.join(", ", result.getMessages()));
        }
        return result.getOpenAPI();
    }

    private RecordModel findRecord(List<ModelFile> models, String name) {
        return models.stream()
                .filter(model -> model instanceof RecordModel)
                .map(model -> (RecordModel) model)
                .filter(model -> model.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private FieldModel findField(RecordModel model, String name) {
        return model.getFields().stream()
                .filter(field -> field.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
