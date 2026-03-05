package nl.stijlaartit.spring.oas.generator.serialization;

import nl.stijlaartit.spring.oas.generator.domain.file.ApiFile;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiHttpMethod;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiOperation;
import nl.stijlaartit.spring.oas.generator.domain.file.ParameterLocation;
import nl.stijlaartit.spring.oas.generator.domain.file.ParameterModel;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaMethodName;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ClientSerializerTest {

    private final ClientSerializer writer = new ClientSerializer(
            "com.example.client", "com.example.models", ClientWriterConfig.defaultConfig());

    @Test
    void generatesInterfaceWithGetOperation() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("getPetById"), ApiHttpMethod.GET,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), true)),
                        null,
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertGeneratedAnnotation(source);
        assertTrue(source.contains("package com.example.client;"));
        assertTrue(source.contains("interface PetApi"));
        assertTrue(source.contains("@GetExchange(\"/pet/{petId}\")"));
        assertTrue(source.contains("Pet getPetById("));
        assertTrue(source.contains("@PathVariable Long petId"));
    }

    @Test
    void generatesPostOperationWithRequestBody() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("addPet"), ApiHttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
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
                new ApiOperation(new JavaMethodName("addPet"), ApiHttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("ResponseEntity<Pet> addPetResponseEntity("));
        assertTrue(source.contains("import org.springframework.http.ResponseEntity;"));
    }

    @Test
    void wrapsResponseTypeInMonoWhenReactive() {
        ClientSerializer reactiveWriter = new ClientSerializer(
                "com.example.client",
                "com.example.models",
                new ClientWriterConfig(ClientWriterConfig.IoMode.REACTIVE)
        );
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("addPet"), ApiHttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        false)
        ));

        String source = reactiveWriter.toJavaFile(client).toString();

        assertTrue(source.contains("Mono<Pet> addPet("));
        assertTrue(source.contains("import reactor.core.publisher.Mono;"));
    }

    @Test
    void wrapsResponseEntityInMonoWhenReactive() {
        ClientSerializer reactiveWriter = new ClientSerializer(
                "com.example.client",
                "com.example.models",
                new ClientWriterConfig(ClientWriterConfig.IoMode.REACTIVE)
        );
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("addPet"), ApiHttpMethod.POST,
                        "/pet", List.of(),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        false)
        ));

        String source = reactiveWriter.toJavaFile(client).toString();

        assertTrue(source.contains("Mono<ResponseEntity<Pet>> addPetResponseEntity("));
        assertTrue(source.contains("import org.springframework.http.ResponseEntity;"));
        assertTrue(source.contains("import reactor.core.publisher.Mono;"));
    }

    private static void assertGeneratedAnnotation(String source) {
        assertTrue(source.contains("value = \"" + GeneratedAnnotation.VALUE + "\""));
        Pattern pattern = Pattern.compile(
                "@(?:javax\\.annotation\\.processing\\.)?Generated\\(\\s*value = \".+?\"\\s*,\\s*date = \"\\d{4}-\\d{2}-\\d{2}T[^\"]+\"\\s*\\)",
                Pattern.DOTALL
        );
        assertTrue(pattern.matcher(source).find());
    }

    @Test
    void generatesVoidReturnForNoResponseBody() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("deletePet"), ApiHttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), true)),
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
                new ApiOperation(new JavaMethodName("deletePet"), ApiHttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), true)),
                        null, null,
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("ResponseEntity<Void> deletePetResponseEntity("));
        assertTrue(source.contains("import org.springframework.http.ResponseEntity;"));
    }
    @Test
    void wrapsVoidReturnTypeInMonoWhenReactive() {
        ClientSerializer reactiveWriter = new ClientSerializer(
                "com.example.client",
                "com.example.models",
                new ClientWriterConfig(ClientWriterConfig.IoMode.REACTIVE)
        );
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("deletePet"), ApiHttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), true)),
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
                new ApiOperation(new JavaMethodName("findPetsByStatus"), ApiHttpMethod.GET,
                        "/pet/findByStatus",
                        List.of(new ParameterModel("status",
                                ParameterLocation.QUERY,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true)),
                        null,
                        TypeDescriptor.list(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet"))),
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
                new ApiOperation(new JavaMethodName("deletePet"), ApiHttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(
                                new ParameterModel("api_key",
                                        ParameterLocation.HEADER,
                                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), false),
                                new ParameterModel("petId",
                                        ParameterLocation.PATH,
                                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), true)
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
                new ApiOperation(new JavaMethodName("getPlaylist"), ApiHttpMethod.GET,
                        "/playlists/{id}",
                        List.of(new ParameterModel("public",
                                ParameterLocation.QUERY,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Boolean")), false)),
                        null,
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@Nullable @RequestParam(value = \"public\", required = false) Boolean public_"));
        assertTrue(source.contains("import org.jspecify.annotations.Nullable;"));
    }

    @Test
    void sanitizesOperationIdToValidMethodName() {
        ApiFile client = new ApiFile("AlbumApi", List.of(
                new ApiOperation(new JavaMethodName("getAnAlbum"), ApiHttpMethod.GET,
                        "/albums/{id}",
                        List.of(new ParameterModel("id",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true)),
                        null,
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("String getAnAlbum("));
    }

    @Test
    void generatesMultipleOperationsInOneInterface() {
        ApiFile client = new ApiFile("StoreApi", List.of(
                new ApiOperation(new JavaMethodName("getInventory"), ApiHttpMethod.GET,
                        "/store/inventory", List.of(), null,
                        TypeDescriptor.map(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer"))),
                        false),
                new ApiOperation(new JavaMethodName("placeOrder"), ApiHttpMethod.POST,
                        "/store/order", List.of(),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Order")),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Order")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("Map<String, Integer> getInventory()"));
        assertTrue(source.contains("Order placeOrder("));
    }

    @Test
    void generatesPutExchange() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("updatePet"), ApiHttpMethod.PUT,
                        "/pet", List.of(),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@PutExchange(\"/pet\")"));
    }

    @Test
    void generatesListReturnType() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("findPetsByTags"), ApiHttpMethod.GET,
                        "/pet/findByTags",
                        List.of(new ParameterModel("tags",
                                ParameterLocation.QUERY,
                                TypeDescriptor.list(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"))), true)),
                        null,
                        TypeDescriptor.list(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet"))),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("List<Pet> findPetsByTags("));
        assertTrue(source.contains("@RequestParam List<String> tags"));
    }

    @Test
    void generatesPatchExchange() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("patchPet"), ApiHttpMethod.PATCH,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), true)),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")),
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@PatchExchange(\"/pet/{petId}\")"));
    }

    @Test
    void marksDeprecatedOperation() {
        ApiFile client = new ApiFile("PetApi", List.of(
                new ApiOperation(new JavaMethodName("deletePet"), ApiHttpMethod.DELETE,
                        "/pet/{petId}",
                        List.of(new ParameterModel("petId",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long")), true)),
                        null, null,
                        true)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@Deprecated"));
        assertTrue(source.contains("void deletePet("));
    }

    @Test
    void includesAcceptPropertyWhenProvided() {
        ApiFile client = new ApiFile("FileApi", List.of(
                new ApiOperation(new JavaMethodName("getFileBinary"), ApiHttpMethod.GET,
                        "/files/{id}",
                        List.of(new ParameterModel("id",
                                ParameterLocation.PATH,
                                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true)),
                        null,
                        TypeDescriptor.qualified("org.springframework.core.io", new JavaTypeName.Generated("Resource")),
                        "application/octet-stream",
                        false)
        ));

        String source = writer.toJavaFile(client).toString();

        assertTrue(source.contains("@GetExchange("));
        assertTrue(source.contains("accept = \"application/octet-stream\""));
    }
}
