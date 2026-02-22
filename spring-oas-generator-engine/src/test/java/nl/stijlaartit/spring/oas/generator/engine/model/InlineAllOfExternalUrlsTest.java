package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordField;
import nl.stijlaartit.spring.oas.generator.domain.file.ModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordModel;
import nl.stijlaartit.spring.oas.generator.serialization.ImplementsByMapping;
import nl.stijlaartit.spring.oas.generator.serialization.RecordModelSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.RecordModelWriterConfig;
import nl.stijlaartit.spring.oas.generator.serialization.SerializedFile;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
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
        RecordField externalUrls = findField(albumBase, "externalUrls");
        assertEquals(new JavaTypeName.Generated("ExternalUrlObject"), externalUrls.type().modelName());
    }

    @Test
    void modelWriterEmitsRefTypeForSingleAllOfProperty() {
        List<ModelFile> models = resolveModels();
        RecordModel albumBase = findRecord(models, "AlbumBase");

        RecordModelSerializer writer = new RecordModelSerializer("com.example.models", RecordModelWriterConfig.defaultConfig(), ImplementsByMapping.empty());
        SerializedFile serialized = writer.serialize(albumBase);
        String source = ((SerializedFile.Ast) serialized).javaFile().toString();

        assertTrue(source.contains("ExternalUrlObject externalUrls"));
        assertFalse(source.contains("AlbumBaseExternalUrls"));
    }

    private List<ModelFile> resolveModels() {
        OpenAPI openAPI = parseSpec();
        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeDescriptorFactory typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes, "com.example.models");
        ModelResolver resolver = new ModelResolver(schemaTypes, typeDescriptorFactory, Logger.noOp());
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

    private RecordField findField(RecordModel model, String name) {
        return model.fields().stream()
                .filter(field -> field.name().value().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
