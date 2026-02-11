package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.domain.ApiFile;
import nl.stijlaartit.generator.domain.GenerationContext;
import nl.stijlaartit.generator.domain.HttpMethod;
import nl.stijlaartit.generator.domain.OperationModel;
import nl.stijlaartit.generator.domain.ParameterLocation;
import nl.stijlaartit.generator.domain.ParameterModel;
import nl.stijlaartit.generator.model.TypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.Components;
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);

            assertEquals(2, clients.size());
            assertEquals("PetApi", clients.get(0).getName());
            assertEquals("StoreApi", clients.get(1).getName());
            assertEquals(1, clients.get(0).getOperations().size());
            assertEquals("getPetById", clients.get(0).getOperations().get(0).getName());
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);

            assertEquals(1, clients.size());
            assertEquals("UserAndAuthenticationApi", clients.get(0).getName());
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);

            assertEquals(1, clients.size());
            assertEquals("DefaultApi", clients.get(0).getName());
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            OperationModel op = clients.get(0).getOperations().get(0);

            assertEquals(1, op.getParameters().size());
            ParameterModel param = op.getParameters().get(0);
            assertEquals("petId", param.getName());
            assertEquals(ParameterLocation.PATH, param.getLocation());
            assertEquals(TypeDescriptor.simple("java.lang.Long"), param.getType());
            assertTrue(param.isRequired());
        }

        @Test
        void resolvesQueryParameter() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/findByStatus", "get", "findPetsByStatus", "pet",
                    List.of(queryParam("status", "string", null)),
                    null, null
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            ParameterModel param = clients.get(0).getOperations().get(0).getParameters().get(0);

            assertEquals("status", param.getName());
            assertEquals(ParameterLocation.QUERY, param.getLocation());
            assertEquals(TypeDescriptor.simple("java.lang.String"), param.getType());
        }

        @Test
        void resolvesHeaderParameter() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "delete", "deletePet", "pet",
                    List.of(headerParam("api_key", "string", null)),
                    null, null
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            ParameterModel param = clients.get(0).getOperations().get(0).getParameters().get(0);

            assertEquals("api_key", param.getName());
            assertEquals(ParameterLocation.HEADER, param.getLocation());
        }

        @Test
        void resolvesRefParametersFromComponents() {
            Parameter refParam = new Parameter().$ref("#/components/parameters/offsetParam");
            Parameter limitRefParam = new Parameter().$ref("#/components/parameters/limitParam");
            OpenAPI openAPI = openApiWithOperation(
                    "/articles", "get", "getArticles", "articles",
                    List.of(queryParam("tag", "string", null), refParam, limitRefParam),
                    null, null
            );
            openAPI.setComponents(new Components()
                    .addParameters("offsetParam", new Parameter()
                            .name("offset")
                            .in("query")
                            .required(false)
                            .schema(new IntegerSchema().format("int32")))
                    .addParameters("limitParam", new Parameter()
                            .name("limit")
                            .in("query")
                            .required(false)
                            .schema(new IntegerSchema().format("int32"))));

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            List<ParameterModel> params = clients.get(0).getOperations().get(0).getParameters();

            assertEquals(3, params.size());
            assertEquals("tag", params.get(0).getName());
            assertEquals("offset", params.get(1).getName());
            assertEquals(TypeDescriptor.simple("java.lang.Integer"), params.get(1).getType());
            assertEquals("limit", params.get(2).getName());
            assertEquals(TypeDescriptor.simple("java.lang.Integer"), params.get(2).getType());
        }

        @Test
        void skipsUnresolvedRefParameters() {
            Parameter refParam = new Parameter().$ref("#/components/parameters/offsetParam");
            OpenAPI openAPI = openApiWithOperation(
                    "/articles", "get", "getArticles", "articles",
                    List.of(queryParam("tag", "string", null), refParam),
                    null, null
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            List<ParameterModel> params = clients.get(0).getOperations().get(0).getParameters();

            assertEquals(1, params.size());
            assertEquals("tag", params.get(0).getName());
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            ParameterModel param = clients.get(0).getOperations().get(0).getParameters().get(0);

            assertEquals(TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), param.getType());
        }

        @Test
        void resolvesStringSchemaWithoutExplicitType() {
            Parameter tagParam = new Parameter()
                    .name("tag")
                    .in("query")
                    .required(false)
                    .schema(new StringSchema().type(null));

            OpenAPI openAPI = openApiWithOperation(
                    "/articles", "get", "getArticles", "articles",
                    List.of(tagParam), null, null
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            ParameterModel param = clients.get(0).getOperations().get(0).getParameters().get(0);

            assertEquals(TypeDescriptor.simple("java.lang.String"), param.getType());
        }

        @Test
        void resolvesSchemaTypeFromTypesSet() {
            Schema<?> schema = new Schema<>().types(java.util.Set.of("string"));
            Parameter tagParam = new Parameter()
                    .name("tag")
                    .in("query")
                    .required(false)
                    .schema(schema);

            OpenAPI openAPI = openApiWithOperation(
                    "/articles", "get", "getArticles", "articles",
                    List.of(tagParam), null, null
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            ParameterModel param = clients.get(0).getOperations().get(0).getParameters().get(0);

            assertEquals(TypeDescriptor.simple("java.lang.String"), param.getType());
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor body = clients.get(0).getOperations().get(0).getRequestBody();

            assertEquals(TypeDescriptor.complex("Pet"), body);
        }

        @Test
        void resolvesNullForNoBody() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "get", "getPetById", "pet",
                    List.of(), null, null
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            assertNull(clients.get(0).getOperations().get(0).getRequestBody());
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor body = clients.get(0).getOperations().get(0).getRequestBody();

            assertEquals(TypeDescriptor.simple("org.springframework.core.io.Resource"), body);
        }

        @Test
        void resolvesInlineObjectRequestBody() {
            Schema<?> bodySchema = new ObjectSchema()
                    .addProperty("ids", new ArraySchema().items(new StringSchema()));

            OpenAPI openAPI = openApiWithOperation(
                    "/me/albums", "put", "save-albums-user", "albums",
                    List.of(), jsonBody(bodySchema), null
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor body = clients.get(0).getOperations().get(0).getRequestBody();

            assertEquals(TypeDescriptor.complex("SaveAlbumsUserRequest"), body);
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor response = clients.get(0).getOperations().get(0).getResponseType();

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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor response = clients.get(0).getOperations().get(0).getResponseType();

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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor response = clients.get(0).getOperations().get(0).getResponseType();

            assertEquals(TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")), response);
        }

        @Test
        void resolvesMapResponseTypeWithAdditionalPropertiesTrue() {
            ObjectSchema mapSchema = new ObjectSchema();
            mapSchema.setAdditionalProperties(true);
            OpenAPI openAPI = openApiWithOperation(
                    "/store/inventory", "get", "getInventory", "store",
                    List.of(), null, jsonResponse(mapSchema)
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor response = clients.get(0).getOperations().get(0).getResponseType();

            assertEquals(TypeDescriptor.map(TypeDescriptor.simple("java.lang.Object")), response);
        }

        @Test
        void resolvesNullForVoidResponse() {
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("200", new ApiResponse().description("success"));
            OpenAPI openAPI = openApiWithOperation(
                    "/user/logout", "get", "logoutUser", "user",
                    List.of(), null, responses
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            assertNull(clients.get(0).getOperations().get(0).getResponseType());
        }

        @Test
        void resolvesSimpleStringResponse() {
            OpenAPI openAPI = openApiWithOperation(
                    "/user/login", "get", "loginUser", "user",
                    List.of(), null, jsonResponse(new StringSchema())
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor response = clients.get(0).getOperations().get(0).getResponseType();

            assertEquals(TypeDescriptor.simple("java.lang.String"), response);
        }

        @Test
        void resolvesInlineObjectResponse() {
            Schema<?> responseSchema = new ObjectSchema()
                    .addProperty("status", new StringSchema());

            OpenAPI openAPI = openApiWithOperation(
                    "/user/status", "get", "get-user-status", "user",
                    List.of(), null, jsonResponse(responseSchema)
            );

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            TypeDescriptor response = clients.get(0).getOperations().get(0).getResponseType();

            assertEquals(TypeDescriptor.complex("GetUserStatusResponse"), response);
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

            GenerationContext context = new GenerationContext();
            resolver.resolve(openAPI, context);

            List<ApiFile> clients = context.getFiles(ApiFile.class);
            List<OperationModel> ops = clients.get(0).getOperations();

            assertEquals(5, ops.size());
            assertEquals(HttpMethod.GET, ops.get(0).getMethod());
            assertEquals(HttpMethod.POST, ops.get(1).getMethod());
            assertEquals(HttpMethod.PUT, ops.get(2).getMethod());
            assertEquals(HttpMethod.DELETE, ops.get(3).getMethod());
            assertEquals(HttpMethod.PATCH, ops.get(4).getMethod());
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
