package nl.stijlaartit.spring.oas.generator.engine.client;

import nl.stijlaartit.spring.oas.generator.domain.file.ApiFile;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiHttpMethod;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiOperation;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.domain.file.ParameterLocation;
import nl.stijlaartit.spring.oas.generator.domain.file.ParameterModel;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.model.TypeDescriptorFactory;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaMethodName;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
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
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClientResolverTest {

    private List<ApiFile> resolveClient(OpenAPI openAPI) {
        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        final var typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes, "com.example.models");
        return new ClientResolver(Logger.noOp(), typeDescriptorFactory).resolve(openAPI);
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

            List<ApiFile> clients = resolveClient(openAPI)
                    .stream()
                    .sorted(Comparator.comparing(ApiFile::name))
                    .toList();

            assertEquals(2, clients.size());
            assertEquals("PetApi", clients.getFirst().name());
            assertEquals("StoreApi", clients.get(1).name());
            assertEquals(1, clients.getFirst().getOperations().size());
            assertEquals(new JavaMethodName("getPetById"), clients.getFirst().getOperations().getFirst().name());
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

            List<ApiFile> clients = resolveClient(openAPI);

            assertEquals(1, clients.size());
            assertEquals("UserAndAuthenticationApi", clients.getFirst().name());
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

            List<ApiFile> clients = resolveClient(openAPI);

            assertEquals(1, clients.size());
            assertEquals("DefaultApi", clients.getFirst().name());
        }

        @Test
        void generatesFallbackOperationIdWhenMissing() {
            OpenAPI openAPI = openApiWithOperation(
                    "/auth/session", "get", null, "auth",
                    List.of(), null, jsonResponse(new StringSchema())
            );

            List<ApiFile> clients = resolveClient(openAPI);

            assertEquals(1, clients.size());
            ApiOperation operation = clients.getFirst().getOperations().getFirst();
            assertEquals(new JavaMethodName("getAuthSession"), operation.name());
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

            List<ApiFile> clients = resolveClient(openAPI);
            ApiOperation op = clients.getFirst().getOperations().getFirst();

            assertEquals(1, op.parameters().size());
            ParameterModel param = op.parameters().getFirst();
            assertEquals("petId", param.name());
            assertEquals(ParameterLocation.PATH, param.location());
            assertEquals(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), param.type());
            assertTrue(param.required());
        }

        @Test
        void resolvesQueryParameter() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/findByStatus", "get", "findPetsByStatus", "pet",
                    List.of(queryParam("status", "string", null)),
                    null, null
            );

            List<ApiFile> clients = resolveClient(openAPI);
            ParameterModel param = clients.getFirst().getOperations().getFirst().parameters().getFirst();

            assertEquals("status", param.name());
            assertEquals(ParameterLocation.QUERY, param.location());
            assertEquals(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), param.type());
        }

        @Test
        void resolvesHeaderParameter() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "delete", "deletePet", "pet",
                    List.of(headerParam("api_key", "string", null)),
                    null, null
            );

            List<ApiFile> clients = resolveClient(openAPI);
            ParameterModel param = clients.getFirst().getOperations().getFirst().parameters().getFirst();

            assertEquals("api_key", param.name());
            assertEquals(ParameterLocation.HEADER, param.location());
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

            List<ApiFile> clients = resolveClient(openAPI);
            List<ParameterModel> params = clients.getFirst().getOperations().getFirst().parameters();

            assertEquals(3, params.size());
            assertEquals("tag", params.getFirst().name());
            assertEquals("offset", params.get(1).name());
            assertEquals(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer")), params.get(1).type());
            assertEquals("limit", params.get(2).name());
            assertEquals(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer")), params.get(2).type());
        }

        @Test
        void failsOnUnresolvedRefParameters() {
            Parameter refParam = new Parameter().$ref("#/components/parameters/offsetParam");
            OpenAPI openAPI = openApiWithOperation(
                    "/articles", "get", "getArticles", "articles",
                    List.of(queryParam("tag", "string", null), refParam),
                    null, null
            );

            final var exception = assertThrows(IllegalArgumentException.class, () -> resolveClient(openAPI));
            assertEquals("Parameter reference not found: #/components/parameters/offsetParam", exception.getMessage());
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

            List<ApiFile> clients = resolveClient(openAPI);
            ParameterModel param = clients.getFirst().getOperations().getFirst().parameters().getFirst();

            assertEquals(TypeDescriptor.list(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"))), param.type());
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

            List<ApiFile> clients = resolveClient(openAPI);
            ParameterModel param = clients.getFirst().getOperations().getFirst().parameters().getFirst();

            assertEquals(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), param.type());
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

            List<ApiFile> clients = resolveClient(openAPI);
            ParameterModel param = clients.getFirst().getOperations().getFirst().parameters().getFirst();

            assertEquals(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), param.type());
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
            openAPI.getComponents().addSchemas("Pet", new ObjectSchema());

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor body = clients.getFirst().getOperations().getFirst().requestBody();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")), body);
        }

        @Test
        void resolvesNullForNoBody() {
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/{petId}", "get", "getPetById", "pet",
                    List.of(), null, null
            );

            List<ApiFile> clients = resolveClient(openAPI);
            assertNull(clients.getFirst().getOperations().getFirst().requestBody());
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

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor body = clients.getFirst().getOperations().getFirst().requestBody();

            assertEquals(TypeDescriptor.qualified("org.springframework.core.io", new JavaTypeName.Generated("Resource")), body);
        }

        @Test
        void resolvesInlineObjectRequestBody() {
            Schema<?> bodySchema = new ObjectSchema()
                    .addProperty("ids", new ArraySchema().items(new StringSchema()));

            OpenAPI openAPI = openApiWithOperation(
                    "/me/albums", "put", "save-albums-user", "albums",
                    List.of(), jsonBody(bodySchema), null
            );

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor body = clients.getFirst().getOperations().getFirst().requestBody();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("SaveAlbumsUserRequest")), body);
        }

        @Test
        void prefersComponentModelWhenInlineSchemaMatches() {
            Schema<?> componentSchema = new ObjectSchema()
                    .addProperty("id", new StringSchema())
                    .addProperty("name", new StringSchema())
                    .addRequiredItem("id");

            Schema<?> inlineSchema = new ObjectSchema()
                    .addProperty("id", new StringSchema())
                    .addProperty("name", new StringSchema())
                    .addRequiredItem("id");

            OpenAPI openAPI = openApiWithOperation(
                    "/users", "post", "createUser", "users",
                    List.of(), jsonBody(inlineSchema), null
            );
            Components components = new Components();
            components.setSchemas(Map.of("User", componentSchema));
            openAPI.setComponents(components);

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor body = clients.getFirst().getOperations().getFirst().requestBody();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("User")), body);
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
            openAPI.getComponents().addSchemas("Pet", new ObjectSchema());
            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")), response);
        }

        @Test
        void resolves201ResponseType() {
            Schema<?> petRef = new Schema<>().$ref("#/components/schemas/Pet");
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("201",
                            new ApiResponse()
                                    .content(new Content()
                                            .addMediaType("application/json",
                                                    new MediaType().schema(petRef))));
            OpenAPI openAPI = openApiWithOperation(
                    "/pet", "post", "createPet", "pet",
                    List.of(), null, responses
            );
            openAPI.getComponents().addSchemas("Pet", new ObjectSchema());

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")), response);
        }

        @Test
        void ignores2xxWithoutBodyWhenSingleBodyPresent() {
            Schema<?> petRef = new Schema<>().$ref("#/components/schemas/Pet");
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("200", new ApiResponse().description("ok"))
                    .addApiResponse("204", new ApiResponse().description("no content"))
                    .addApiResponse("201",
                            new ApiResponse()
                                    .content(new Content()
                                            .addMediaType("application/json",
                                                    new MediaType().schema(petRef))));
            OpenAPI openAPI = openApiWithOperation(
                    "/pet", "post", "createPet", "pet",
                    List.of(), null, responses
            );
            openAPI.getComponents().addSchemas("Pet", new ObjectSchema());

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")), response);
        }

        @Test
        void throwsWhenMultiple2xxResponsesWithBodiesDefined() {
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("200",
                            new ApiResponse()
                                    .content(new Content()
                                            .addMediaType("application/json",
                                                    new MediaType().schema(new StringSchema()))))
                    .addApiResponse("201",
                            new ApiResponse()
                                    .content(new Content()
                                            .addMediaType("application/json",
                                                    new MediaType().schema(new IntegerSchema()))))
                    .addApiResponse("204", new ApiResponse().description("no content"));
            OpenAPI openAPI = openApiWithOperation(
                    "/pet", "post", "createPet", "pet",
                    List.of(), null, responses
            );

            assertThrows(IllegalArgumentException.class,
                    () -> resolveClient(openAPI));
        }

        @Test
        void resolvesArrayResponseType() {
            Schema<?> arraySchema = new ArraySchema()
                    .items(new Schema<>().$ref("#/components/schemas/Pet"));
            OpenAPI openAPI = openApiWithOperation(
                    "/pet/findByStatus", "get", "findPetsByStatus", "pet",
                    List.of(), null, jsonResponse(arraySchema)
            );
            openAPI.getComponents().addSchemas("Pet", new ObjectSchema());

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.list(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet"))), response);
        }

        @Test
        void resolvesMapResponseType() {
            ObjectSchema mapSchema = new ObjectSchema();
            mapSchema.setAdditionalProperties(new IntegerSchema().format("int32"));
            OpenAPI openAPI = openApiWithOperation(
                    "/store/inventory", "get", "getInventory", "store",
                    List.of(), null, jsonResponse(mapSchema)
            );

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("GetInventory200Response")), response);
        }

        @Test
        void resolvesMapResponseTypeWithAdditionalPropertiesTrue() {
            ObjectSchema mapSchema = new ObjectSchema();
            mapSchema.setAdditionalProperties(true);
            OpenAPI openAPI = openApiWithOperation(
                    "/store/inventory", "get", "getInventory", "store",
                    List.of(), null, jsonResponse(mapSchema)
            );

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("GetInventory200Response")), response);
        }

        @Test
        void normalizesComponentSchemaNamesInResponseType() {
            OpenAPI openAPI = new OpenAPI();
            openAPI.setComponents(new Components()
                    .schemas(new java.util.LinkedHashMap<>(
                            java.util.Map.of("search_SliceInfo", new ObjectSchema()
                                    .addProperty("value", new StringSchema()))
                    )));

            Paths paths = new Paths();
            Schema<?> ref = new Schema<>().$ref("#/components/schemas/search_SliceInfo");
            Operation operation = new Operation()
                    .operationId("getSliceInfo")
                    .tags(List.of("search"))
                    .responses(jsonResponse(ref));
            PathItem pathItem = new PathItem().get(operation);
            paths.addPathItem("/search/slice-info", pathItem);
            openAPI.setPaths(paths);

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("SearchSliceInfo")), response);
        }

        @Test
        void resolvesNullForVoidResponse() {
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("200", new ApiResponse().description("success"));
            OpenAPI openAPI = openApiWithOperation(
                    "/user/logout", "get", "logoutUser", "user",
                    List.of(), null, responses
            );

            List<ApiFile> clients = resolveClient(openAPI);
            assertNull(clients.getFirst().getOperations().getFirst().responseType());
        }

        @Test
        void resolvesSimpleStringResponse() {
            OpenAPI openAPI = openApiWithOperation(
                    "/user/login", "get", "loginUser", "user",
                    List.of(), null, jsonResponse(new StringSchema())
            );

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), response);
        }

        @Test
        void resolvesInlineObjectResponse() {
            Schema<?> responseSchema = new ObjectSchema()
                    .addProperty("status", new StringSchema());

            OpenAPI openAPI = openApiWithOperation(
                    "/user/status", "get", "get-user-status", "user",
                    List.of(), null, jsonResponse(responseSchema)
            );

            List<ApiFile> clients = resolveClient(openAPI);
            TypeDescriptor response = clients.getFirst().getOperations().getFirst().responseType();

            assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("GetUserStatus200Response")), response);
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

            List<ApiFile> clients = resolveClient(openAPI);
            List<ApiOperation> ops = clients.getFirst().getOperations()
                    .stream().sorted(Comparator.comparing(v -> v.method().ordinal())).toList();

            assertEquals(5, ops.size());
            assertEquals(ApiHttpMethod.GET, ops.getFirst().method());
            assertEquals(ApiHttpMethod.POST, ops.get(1).method());
            assertEquals(ApiHttpMethod.PUT, ops.get(2).method());
            assertEquals(ApiHttpMethod.DELETE, ops.get(3).method());
            assertEquals(ApiHttpMethod.PATCH, ops.get(4).method());
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
        openAPI.setComponents(new Components());
        return openAPI;
    }
}
