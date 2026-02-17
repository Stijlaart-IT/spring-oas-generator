package nl.stijlaartit.generator.engine.client;

import nl.stijlaartit.generator.engine.domain.ApiFile;
import nl.stijlaartit.generator.engine.domain.HttpMethod;
import nl.stijlaartit.generator.engine.domain.OperationModel;
import nl.stijlaartit.generator.engine.domain.OperationName;
import nl.stijlaartit.generator.engine.domain.ParameterLocation;
import nl.stijlaartit.generator.engine.domain.ParameterModel;
import nl.stijlaartit.generator.engine.model.TypeDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientWriterTest {

    private final ClientWriter writer = new ClientWriter(
            "com.example.client", "com.example.models");

    @Test
    void generatesInterfaceWithGetOperation() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("getPetById"), HttpMethod.GET,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        null,
                        TypeDescriptor.complex("Pet"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("package com.example.client;"));
        assertTrue(source.contains("interface PetApi"));
        assertTrue(source.contains("@GetExchange(\"/pet/{petId}\")"));
        assertTrue(source.contains("Pet getPetById("));
        assertTrue(source.contains("@PathVariable Long petId"));
    }

    @Test
    void generatesPostOperationWithRequestBody() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("addPet"), HttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.complex("Pet"),
                        TypeDescriptor.complex("Pet"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@PostExchange(\"/pet\")"));
        assertTrue(source.contains("Pet addPet("));
        assertTrue(source.contains("@RequestBody Pet body"));
    }

    @Test
    void generatesResponseEntityVariantForResponseBody() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("addPet"), HttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.complex("Pet"),
                        TypeDescriptor.complex("Pet"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("ResponseEntity<Pet> addPetResponseEntity("));
        assertTrue(source.contains("import org.springframework.http.ResponseEntity;"));
    }

    @Test
    void wrapsResponseTypeInMonoWhenReactive() {
        ClientWriter reactiveWriter = new ClientWriter(
                "com.example.client",
                "com.example.models",
                new ClientWriterConfig(ClientWriterConfig.IoMode.REACTIVE)
        );
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("addPet"), HttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.complex("Pet"),
                        TypeDescriptor.complex("Pet"),
                        false)
        ));

        String source = reactiveWriter.toJavaFile(client).toString();

        assertTrue(source.contains("Mono<Pet> addPet("));
        assertTrue(source.contains("import reactor.core.publisher.Mono;"));
    }

    @Test
    void wrapsResponseEntityInMonoWhenReactive() {
        ClientWriter reactiveWriter = new ClientWriter(
                "com.example.client",
                "com.example.models",
                new ClientWriterConfig(ClientWriterConfig.IoMode.REACTIVE)
        );
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("addPet"), HttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.complex("Pet"),
                        TypeDescriptor.complex("Pet"),
                        false)
        ));

        String source = reactiveWriter.toJavaFile(client).toString();

        assertTrue(source.contains("Mono<ResponseEntity<Pet>> addPetResponseEntity("));
        assertTrue(source.contains("import org.springframework.http.ResponseEntity;"));
        assertTrue(source.contains("import reactor.core.publisher.Mono;"));
    }

    @Test
    void generatesVoidReturnForNoResponseBody() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("deletePet"), HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        null, null,
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@DeleteExchange(\"/pet/{petId}\")"));
        assertTrue(source.contains("void deletePet("));
    }

    @Test
    void generatesResponseEntityVariantForVoidReturn() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("deletePet"), HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        null, null,
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("ResponseEntity<Void> deletePetResponseEntity("));
        assertTrue(source.contains("import org.springframework.http.ResponseEntity;"));
    }
    @Test
    void wrapsVoidReturnTypeInMonoWhenReactive() {
        ClientWriter reactiveWriter = new ClientWriter(
                "com.example.client",
                "com.example.models",
                new ClientWriterConfig(ClientWriterConfig.IoMode.REACTIVE)
        );
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("deletePet"), HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        null, null,
                        false)
        ));

        String source = reactiveWriter.toJavaFile(client).toString();

        assertTrue(source.contains("Mono<Void> deletePet("));
        assertTrue(source.contains("import reactor.core.publisher.Mono;"));
    }

    @Test
    void generatesQueryParameterAnnotation() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("findPetsByStatus"), HttpMethod.GET,
                        "/pet/findByStatus",
                        List.of(new ParameterModel("status",
                                ParameterLocation.QUERY,
                                TypeDescriptor.simple("java.lang.String"), true)),
                        null,
                        TypeDescriptor.list(TypeDescriptor.complex("Pet")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@GetExchange(\"/pet/findByStatus\")"));
        assertTrue(source.contains("List<Pet> findPetsByStatus("));
        assertTrue(source.contains("@RequestParam(required = true) String status"));
    }

    @Test
    void generatesHeaderParameterWithOriginalName() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("deletePet"), HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(
                                new ParameterModel("api_key",
                                        ParameterLocation.HEADER,
                                        TypeDescriptor.simple("java.lang.String"), false),
                                new ParameterModel("petId",
                                        ParameterLocation.PATH,
                                        TypeDescriptor.simple("java.lang.Long"), true)
                        ),
                        null, null,
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@RequestHeader(\"api_key\") String apiKey"));
        assertTrue(source.contains("@PathVariable Long petId"));
    }

    @Test
    void sanitizesKeywordParameterName() {
        ApiFile client = new ApiFile("PlaylistApi", List.of(
                new OperationModel(OperationName.id("getPlaylist"), HttpMethod.GET,
                        "/playlists/{id}",
                        List.of(new ParameterModel("public",
                                ParameterLocation.QUERY,
                                TypeDescriptor.simple("java.lang.Boolean"), false)),
                        null,
                        TypeDescriptor.simple("java.lang.String"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@Nullable @RequestParam(value = \"public\", required = false) Boolean public_"));
        assertTrue(source.contains("import org.jspecify.annotations.Nullable;"));
    }

    @Test
    void sanitizesOperationIdToValidMethodName() {
        ApiFile client = new ApiFile("AlbumApi", List.of(
                new OperationModel(OperationName.id("get-an-album"), HttpMethod.GET,
                        "/albums/{id}",
                        List.of(new ParameterModel("id",
                                ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.String"), true)),
                        null,
                        TypeDescriptor.simple("java.lang.String"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("String getAnAlbum("));
    }

    @Test
    void generatesMultipleOperationsInOneInterface() {
        ApiFile client = new ApiFile("StoreApi", List.of(
                new OperationModel(OperationName.id("getInventory"), HttpMethod.GET,
                        "/store/inventory", List.of(), null,
                        TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")),
                        false),
                new OperationModel(OperationName.id("placeOrder"), HttpMethod.POST,
                        "/store/order", List.of(),
                        TypeDescriptor.complex("Order"),
                        TypeDescriptor.complex("Order"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("Map<String, Integer> getInventory()"));
        assertTrue(source.contains("Order placeOrder("));
    }

    @Test
    void generatesPutExchange() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("updatePet"), HttpMethod.PUT,
                        "/pet", List.of(),
                        TypeDescriptor.complex("Pet"),
                        TypeDescriptor.complex("Pet"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@PutExchange(\"/pet\")"));
    }

    @Test
    void generatesListReturnType() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("findPetsByTags"), HttpMethod.GET,
                        "/pet/findByTags",
                        List.of(new ParameterModel("tags",
                                ParameterLocation.QUERY,
                                TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), true)),
                        null,
                        TypeDescriptor.list(TypeDescriptor.complex("Pet")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("List<Pet> findPetsByTags("));
        assertTrue(source.contains("@RequestParam List<String> tags"));
    }

    @Test
    void generatesPatchExchange() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("patchPet"), HttpMethod.PATCH,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        TypeDescriptor.complex("Pet"),
                        TypeDescriptor.complex("Pet"),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@PatchExchange(\"/pet/{petId}\")"));
    }

    @Test
    void marksDeprecatedOperation() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new OperationModel(OperationName.id("deletePet"), HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        null, null,
                        true)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@Deprecated"));
        assertTrue(source.contains("void deletePet("));
    }
}
