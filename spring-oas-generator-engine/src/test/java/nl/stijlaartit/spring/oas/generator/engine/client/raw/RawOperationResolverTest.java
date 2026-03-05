package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ParamIn;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ResponseMediaType;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleReponse;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOperation;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RawOperationResolverTest {

    @Test
    void resolvesMultiple2xxResponsesWithEquivalentInlineSchemas() {
        SimpleSchema schema200 = new SimpleStringSchema(false);
        SimpleSchema schema201 = new SimpleStringSchema(false);

        List<SimpleReponse> responses = List.of(
                new SimpleReponse("200", schema200, ResponseMediaType.APPLICATION_JSON),
                new SimpleReponse("201", schema201, ResponseMediaType.APPLICATION_JSON)
        );

        SimplifiedOas simplifiedOas = oasWithResponses("getThings", responses);
        RawOperationResolver resolver = new RawOperationResolver(Logger.noOp(), simplifiedOas);

        List<RawOperation> operations = resolver.resolve();
        assertThat(operations).hasSize(1);
        assertThat(operations.get(0)).isInstanceOf(GeneratableOperation.class);

        GeneratableOperation operation = (GeneratableOperation) operations.get(0);
        assertThat(operation.responseBodyType()).isInstanceOf(ResponseBodyType.SchemaType.class);

        ResponseBodyType.SchemaType responseBodyType = (ResponseBodyType.SchemaType) operation.responseBodyType();
        assertThat(responseBodyType.schema()).isEqualTo(schema200);
        assertThat(responseBodyType.mediaType()).isEqualTo(ResponseMediaType.APPLICATION_JSON);
    }

    @Test
    void failsWhenMultiple2xxResponsesDefineDifferentSchemas() {
        List<SimpleReponse> responses = List.of(
                new SimpleReponse("200", new SimpleStringSchema(false), ResponseMediaType.APPLICATION_JSON),
                new SimpleReponse("201", new SimpleIntegerSchema(false), ResponseMediaType.APPLICATION_JSON)
        );

        SimplifiedOas simplifiedOas = oasWithResponses("getThings", responses);
        RawOperationResolver resolver = new RawOperationResolver(Logger.noOp(), simplifiedOas);

        assertThatThrownBy(resolver::resolve)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Multiple 2xx responses with body defined for operation");
    }

    @Test
    void resolvesHeaderParameterLocation() {
        SimplifiedOperation operation = new SimplifiedOperation(
                "/things",
                HttpMethod.GET,
                "getThings",
                java.util.Set.of("default"),
                List.of(new SimpleParam("x-api-key", ParamIn.Header, new SimpleStringSchema(false), true)),
                List.of(new SimpleReponse("200", new SimpleStringSchema(false), ResponseMediaType.APPLICATION_JSON)),
                null
        );

        SimplifiedOas simplifiedOas = new SimplifiedOas(Map.of(), Map.of(), Map.of(), List.of(operation), Map.of());
        RawOperationResolver resolver = new RawOperationResolver(Logger.noOp(), simplifiedOas);

        List<RawOperation> operations = resolver.resolve();
        GeneratableOperation generatableOperation = (GeneratableOperation) operations.getFirst();
        assertThat(generatableOperation.parameters()).containsExactly(
                new RawParameter(
                        "x-api-key",
                        RawParameter.ParameterLocation.HEADER,
                        new SimpleStringSchema(false),
                        true
                )
        );
    }

    @Test
    void resolvesJsonAndBinaryResponsesAsSeparateOperations() {
        SimplifiedOperation operation = new SimplifiedOperation(
                "/files/{id}",
                HttpMethod.GET,
                "getFile",
                java.util.Set.of("files"),
                List.of(new SimpleParam("id", ParamIn.Path, new SimpleStringSchema(false), true)),
                List.of(
                        new SimpleReponse("200", new SimpleStringSchema(false), ResponseMediaType.APPLICATION_JSON),
                        new SimpleReponse("200", new SimpleStringSchema(false), ResponseMediaType.APPLICATION_OCTET_STREAM)
                ),
                null
        );

        SimplifiedOas simplifiedOas = new SimplifiedOas(Map.of(), Map.of(), Map.of(), List.of(operation), Map.of());
        RawOperationResolver resolver = new RawOperationResolver(Logger.noOp(), simplifiedOas);

        List<RawOperation> operations = resolver.resolve();
        assertEquals(2, operations.size());
        assertThat(operations).allMatch(op -> op instanceof GeneratableOperation);

        List<GeneratableOperation> generated = operations.stream()
                .map(op -> (GeneratableOperation) op)
                .toList();

        assertThat(generated).extracting(GeneratableOperation::operationId)
                .containsExactlyInAnyOrder("getFile", "getFileBinary");
        assertThat(generated)
                .extracting(op -> ((ResponseBodyType.SchemaType) op.responseBodyType()).mediaType())
                .containsExactlyInAnyOrder(ResponseMediaType.APPLICATION_JSON, ResponseMediaType.APPLICATION_OCTET_STREAM);
    }

    private static SimplifiedOas oasWithResponses(String operationId, List<SimpleReponse> responses) {
        SimplifiedOperation operation = new SimplifiedOperation(
                "/things",
                HttpMethod.GET,
                operationId,
                java.util.Set.of("default"),
                List.of(),
                responses,
                null
        );
        return new SimplifiedOas(Map.of(), Map.of(), Map.of(), List.of(operation), Map.of());
    }
}
