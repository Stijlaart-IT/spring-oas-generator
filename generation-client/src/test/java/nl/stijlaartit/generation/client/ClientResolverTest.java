package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.model.TypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientResolverTest {

    private ClientResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ClientResolver();
    }

    @Nested
    class OperationGrouping {

        @Test
        void groupsOperationsByTag() {
            OpenAPI openAPI = new OpenAPI();
            Paths paths = new Paths();

            PathItem petPath = new PathItem();
            Operation getPet = new Operation()
                    .operationId("getPetById")
                    .tags(List.of("pet"))
                    .responses(new ApiResponses());
            petPath.setGet(getPet);
            paths.addPathItem("/pet/{petId}", petPath);

            PathItem storePath = new PathItem();
            Operation getOrder = new Operation()
                    .operationId("getOrderById")
                    .tags(List.of("store"))
                    .responses(new ApiResponses());
            storePath.setGet(getOrder);
            paths.addPathItem("/store/order/{orderId}", storePath);

            openAPI.setPaths(paths);

            List<ClientDescriptor> clients = resolver.resolve(openAPI);

            assertEquals(2, clients.size());
            assertEquals("PetApi", clients.get(0).name());
            assertEquals("StoreApi", clients.get(1).name());
            assertEquals(1, clients.get(0).operations().size());
            assertEquals("getPetById", clients.get(0).operations().get(0).name());
        }

        @Test
        void convertsTagWithSpacesToPascalCase() {
            OpenAPI openAPI = new OpenAPI();
            Paths paths = new Paths();

            PathItem pathItem = new PathItem();
            Operation operation = new Operation()
                    .operationId("login")
                    .tags(List.of("User and Authentication"))
                    .responses(new ApiResponses());
            pathItem.setPost(operation);
            paths.addPathItem("/users/login", pathItem);
            openAPI.setPaths(paths);

            List<ClientDescriptor> clients = resolver.resolve(openAPI);

            assertEquals(1, clients.size());
            assertEquals("UserAndAuthenticationApi", clients.get(0).name());
        }

        @Test
        void usesDefaultTagWhenMissing() {
            OpenAPI openAPI = new OpenAPI();
            Paths paths = new Paths();

            PathItem pathItem = new PathItem();
            Operation operation = new Operation()
                    .operationId("someOperation")
                    .responses(new ApiResponses());
            pathItem.setGet(operation);
            paths.addPathItem("/some/path", pathItem);

            openAPI.setPaths(paths);

            List<ClientDescriptor> clients = resolver.resolve(openAPI);

            assertEquals(1, clients.size());
            assertEquals("DefaultApi", clients.get(0).name());
        }
    }

    @Nested
    class ParameterResolution {

        @Test
        void resolvesPathParameter() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "get", "getPetById", "pet",
                    List.of(pathParam("petId", "integer", "int64")),
                    null, null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            OperationDescriptor op = clients.get(0).operations().get(0);

            assertEquals(1, op.parameters().size());
            ParameterDescriptor param = op.parameters().get(0);
            assertEquals("petId", param.name());
            assertEquals(ParameterDescriptor.ParameterLocation.PATH, param.location());
            assertEquals(TypeDescriptor.simple("java.lang.Long"), param.type());
            assertTrue(param.required());
        }

        @Test
        void resolvesQueryParameter() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/findByStatus", "get", "findPetsByStatus", "pet",
                    List.of(queryParam("status", "string", null)),
                    null, null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            ParameterDescriptor param = clients.get(0).operations().get(0).parameters().get(0);

            assertEquals("status", param.name());
            assertEquals(ParameterDescriptor.ParameterLocation.QUERY, param.location());
            assertEquals(TypeDescriptor.simple("java.lang.String"), param.type());
        }

        @Test
        void resolvesHeaderParameter() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "delete", "deletePet", "pet",
                    List.of(headerParam("api_key", "string", null)),
                    null, null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            ParameterDescriptor param = clients.get(0).operations().get(0).parameters().get(0);

            assertEquals("api_key", param.name());
            assertEquals(ParameterDescriptor.ParameterLocation.HEADER, param.location());
        }

        @Test
        void skipsUnresolvedRefParameters() {
            Parameter refParam = new Parameter().$ref("#/components/parameters/offsetParam");
            OpenAPI openAPI = openApiWithOperation(
                    "/articles", "get", "getArticles", "articles",
                    List.of(queryParam("tag", "string", null), refParam),
                    null, null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            List<ParameterDescriptor> params = clients.get(0).operations().get(0).parameters();

            assertEquals(1, params.size());
            assertEquals("tag", params.get(0).name());
        }

        @Test
        void resolvesArrayQueryParameter() {
            Parameter tagsParam = new Parameter()
                    .name("tags")
                    .in("query")
                    .required(true)
                    .schema(new ArraySchema().items(new StringSchema()));

            OpenAPI openAPI = openApiWithOperation(
                    "/pet/findByTags", "get", "findPetsByTags", "pet",
                    List.of(tagsParam), null, null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            ParameterDescriptor param = clients.get(0).operations().get(0).parameters().get(0);

            assertEquals(TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), param.type());
        }
    }

    @Nested
    class RequestBodyResolution {

        @Test
        void resolvesJsonRequestBody() {
            Schema<?> petRef = new Schema<>().$ref("#/components/schemas/Pet");
            OpenAPI openAPI = openApiWithOperation(
                    "/pet", "post", "addPet", "pet",
                    List.of(), jsonBody(petRef), null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            TypeDescriptor body = clients.get(0).operations().get(0).requestBody();

            assertEquals(TypeDescriptor.complex("Pet"), body);
        }

        @Test
        void resolvesNullForNoBody() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "get", "getPetById", "pet",
                    List.of(), null, null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            assertNull(clients.get(0).operations().get(0).requestBody());
        }

        @Test
        void resolvesBinaryUpload() {
            RequestBody requestBody = new RequestBody()
                    .content(new Content()
                            .addMediaType("application/octet-stream",
                                    new MediaType().schema(
                                            new StringSchema().format("binary"))));

            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}/uploadImage", "post", "uploadFile", "pet",
                    List.of(), requestBody, null
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            TypeDescriptor body = clients.get(0).operations().get(0).requestBody();

            assertEquals(TypeDescriptor.simple("org.springframework.core.io.Resource"), body);
        }
    }

    @Nested
    class ResponseResolution {

        @Test
        void resolves200ResponseType() {
            Schema<?> petRef = new Schema<>().$ref("#/components/schemas/Pet");
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "get", "getPetById", "pet",
                    List.of(), null, jsonResponse(petRef)
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            TypeDescriptor response = clients.get(0).operations().get(0).responseType();

            assertEquals(TypeDescriptor.complex("Pet"), response);
        }

        @Test
        void resolvesArrayResponseType() {
            Schema<?> arraySchema = new ArraySchema()
                    .items(new Schema<>().$ref("#/components/schemas/Pet"));
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/findByStatus", "get", "findPetsByStatus", "pet",
                    List.of(), null, jsonResponse(arraySchema)
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            TypeDescriptor response = clients.get(0).operations().get(0).responseType();

            assertEquals(TypeDescriptor.list(TypeDescriptor.complex("Pet")), response);
        }

        @Test
        void resolvesMapResponseType() {
            ObjectSchema mapSchema = new ObjectSchema();
            mapSchema.setAdditionalProperties(new IntegerSchema().format("int32"));
            OpenAPI openAPI = openApiWithOperation(
                    "/store/inventory", "get", "getInventory", "store",
                    List.of(), null, jsonResponse(mapSchema)
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            TypeDescriptor response = clients.get(0).operations().get(0).responseType();

            assertEquals(TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")), response);
        }

        @Test
        void resolvesNullForVoidResponse() {
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("200", new ApiResponse().description("success"));
            OpenAPI openAPI = openApiWithOperation(
                    "/user/logout", "get", "logoutUser", "user",
                    List.of(), null, responses
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            assertNull(clients.get(0).operations().get(0).responseType());
        }

        @Test
        void resolvesSimpleStringResponse() {
            OpenAPI openAPI = openApiWithOperation(
                    "/user/login", "get", "loginUser", "user",
                    List.of(), null, jsonResponse(new StringSchema())
            );

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            TypeDescriptor response = clients.get(0).operations().get(0).responseType();

            assertEquals(TypeDescriptor.simple("java.lang.String"), response);
        }
    }

    @Nested
    class HttpMethodMapping {

        @Test
        void mapsAllHttpMethods() {
            OpenAPI openAPI = new OpenAPI();
            Paths paths = new Paths();

            PathItem pathItem = new PathItem();
            pathItem.setGet(taggedOp("getOp", "test"));
            pathItem.setPost(taggedOp("postOp", "test"));
            pathItem.setPut(taggedOp("putOp", "test"));
            pathItem.setDelete(taggedOp("deleteOp", "test"));
            pathItem.setPatch(taggedOp("patchOp", "test"));
            paths.addPathItem("/test", pathItem);

            openAPI.setPaths(paths);

            List<ClientDescriptor> clients = resolver.resolve(openAPI);
            List<OperationDescriptor> ops = clients.get(0).operations();

            assertEquals(5, ops.size());
            assertEquals(OperationDescriptor.HttpMethod.GET, ops.get(0).method());
            assertEquals(OperationDescriptor.HttpMethod.POST, ops.get(1).method());
            assertEquals(OperationDescriptor.HttpMethod.PUT, ops.get(2).method());
            assertEquals(OperationDescriptor.HttpMethod.DELETE, ops.get(3).method());
            assertEquals(OperationDescriptor.HttpMethod.PATCH, ops.get(4).method());
        }
    }

    // --- Helper methods ---

    private static Operation taggedOp(String operationId, String tag) {
        return new Operation()
                .operationId(operationId)
                .tags(List.of(tag))
                .responses(new ApiResponses());
    }

    private static Parameter pathParam(String name, String type, String format) {
        Schema<?> schema = new Schema<>().type(type).format(format);
        return new Parameter().name(name).in("path").required(true).schema(schema);
    }

    private static Parameter queryParam(String name, String type, String format) {
        Schema<?> schema = new Schema<>().type(type).format(format);
        return new Parameter().name(name).in("query").required(true).schema(schema);
    }

    private static Parameter headerParam(String name, String type, String format) {
        Schema<?> schema = new Schema<>().type(type).format(format);
        return new Parameter().name(name).in("header").required(false).schema(schema);
    }

    private static RequestBody jsonBody(Schema<?> schema) {
        return new RequestBody()
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(schema)));
    }

    private static ApiResponses jsonResponse(Schema<?> schema) {
        return new ApiResponses()
                .addApiResponse("200",
                        new ApiResponse()
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(schema))));
    }

    private static OpenAPI openApiWithOperation(
            String path, String method, String operationId, String tag,
            List<Parameter> parameters, RequestBody requestBody,
            ApiResponses responses) {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        Operation operation = new Operation()
                .operationId(operationId)
                .tags(List.of(tag))
                .parameters(parameters.isEmpty() ? null : parameters)
                .requestBody(requestBody)
                .responses(responses != null ? responses : new ApiResponses());

        PathItem pathItem = new PathItem();
        switch (method) {
            case "get" -> pathItem.setGet(operation);
            case "post" -> pathItem.setPost(operation);
            case "put" -> pathItem.setPut(operation);
            case "delete" -> pathItem.setDelete(operation);
            case "patch" -> pathItem.setPatch(operation);
        }
        paths.addPathItem(path, pathItem);
        openAPI.setPaths(paths);

        return openAPI;
    }
}
