package nl.stijlaartit.spring.oas.generator.engine.model;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.spring.oas.generator.engine.domain.FieldModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.ModelFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.RecordModel;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InlineAllOfExternalUrlsTest {

    @Test
    void modelResolverUsesRefTypeForSingleAllOfProperty() {
        List<ModelFile> models = resolveModels();

        assertTrue(models.stream().noneMatch(model -> model.name().equals("AlbumBaseExternalUrls")));

        RecordModel albumBase = findRecord(models, "AlbumBase");
        FieldModel externalUrls = findField(albumBase, "externalUrls");
        assertInstanceOf(TypeDescriptor.ComplexType.class, externalUrls.type());
        TypeDescriptor.ComplexType type = (TypeDescriptor.ComplexType) externalUrls.type();
        assertEquals(new JavaTypeName.Generated("ExternalUrlObject"), type.modelName());
    }

    @Test
    void modelWriterEmitsRefTypeForSingleAllOfProperty() {
        List<ModelFile> models = resolveModels();
        RecordModel albumBase = findRecord(models, "AlbumBase");

        RecordModelSerializer writer = new RecordModelSerializer("com.example.models", RecordModelWriterConfig.defaultConfig(), ImplementsByMapping.empty());
        String source = writer.toJavaFile(albumBase).toString();

        assertTrue(source.contains("ExternalUrlObject externalUrls"));
        assertFalse(source.contains("AlbumBaseExternalUrls"));
    }

    private List<ModelFile> resolveModels() {
        OpenAPI openAPI = parseSpec();
        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeDescriptorFactory typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes);
        ModelResolver resolver = new ModelResolver(schemaTypes, typeDescriptorFactory);
        return resolver.resolve();
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
                .filter(model -> model.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private FieldModel findField(RecordModel model, String name) {
        return model.fields().stream()
                .filter(field -> field.name().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
