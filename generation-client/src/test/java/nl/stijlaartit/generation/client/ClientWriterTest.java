package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.model.TypeDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientWriterTest {

    private final ClientWriter writer = new ClientWriter(
            "com.example.client", "com.example.models");

    @Test
    void generatesInterfaceWithGetOperation() {
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("getPetById", OperationDescriptor.HttpMethod.GET,
                        "/pet/{petId}",
                        List.of(new ParameterDescriptor("petId",
                                ParameterDescriptor.ParameterLocation.PATH,
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
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("addPet", OperationDescriptor.HttpMethod.POST,
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
    void generatesVoidReturnForNoResponseBody() {
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("deletePet", OperationDescriptor.HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterDescriptor("petId",
                                ParameterDescriptor.ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        null, null,
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@DeleteExchange(\"/pet/{petId}\")"));
        assertTrue(source.contains("void deletePet("));
    }

    @Test
    void generatesQueryParameterAnnotation() {
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("findPetsByStatus", OperationDescriptor.HttpMethod.GET,
                        "/pet/findByStatus",
                        List.of(new ParameterDescriptor("status",
                                ParameterDescriptor.ParameterLocation.QUERY,
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
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("deletePet", OperationDescriptor.HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(
                                new ParameterDescriptor("api_key",
                                        ParameterDescriptor.ParameterLocation.HEADER,
                                        TypeDescriptor.simple("java.lang.String"), false),
                                new ParameterDescriptor("petId",
                                        ParameterDescriptor.ParameterLocation.PATH,
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
        ClientDescriptor client = new ClientDescriptor("PlaylistApi", List.of(
                new OperationDescriptor("getPlaylist", OperationDescriptor.HttpMethod.GET,
                        "/playlists/{id}",
                        List.of(new ParameterDescriptor("public",
                                ParameterDescriptor.ParameterLocation.QUERY,
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
        ClientDescriptor client = new ClientDescriptor("AlbumApi", List.of(
                new OperationDescriptor("get-an-album", OperationDescriptor.HttpMethod.GET,
                        "/albums/{id}",
                        List.of(new ParameterDescriptor("id",
                                ParameterDescriptor.ParameterLocation.PATH,
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
        ClientDescriptor client = new ClientDescriptor("StoreApi", List.of(
                new OperationDescriptor("getInventory", OperationDescriptor.HttpMethod.GET,
                        "/store/inventory", List.of(), null,
                        TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")),
                        false),
                new OperationDescriptor("placeOrder", OperationDescriptor.HttpMethod.POST,
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
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("updatePet", OperationDescriptor.HttpMethod.PUT,
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
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("findPetsByTags", OperationDescriptor.HttpMethod.GET,
                        "/pet/findByTags",
                        List.of(new ParameterDescriptor("tags",
                                ParameterDescriptor.ParameterLocation.QUERY,
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
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("patchPet", OperationDescriptor.HttpMethod.PATCH,
                        "/pet/{petId}",
                        List.of(new ParameterDescriptor("petId",
                                ParameterDescriptor.ParameterLocation.PATH,
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
        ClientDescriptor client = new ClientDescriptor("PetApi", List.of(
                new OperationDescriptor("deletePet", OperationDescriptor.HttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterDescriptor("petId",
                                ParameterDescriptor.ParameterLocation.PATH,
                                TypeDescriptor.simple("java.lang.Long"), true)),
                        null, null,
                        true)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@Deprecated"));
        assertTrue(source.contains("void deletePet("));
    }
}
