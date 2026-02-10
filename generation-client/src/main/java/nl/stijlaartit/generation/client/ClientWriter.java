package nl.stijlaartit.generation.client;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.model.JavaIdentifierUtils;
import nl.stijlaartit.generator.model.TypeNameResolver;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ClientWriter {

    private static final ClassName GET_EXCHANGE = ClassName.get(
            "org.springframework.web.service.annotation", "GetExchange");
    private static final ClassName POST_EXCHANGE = ClassName.get(
            "org.springframework.web.service.annotation", "PostExchange");
    private static final ClassName PUT_EXCHANGE = ClassName.get(
            "org.springframework.web.service.annotation", "PutExchange");
    private static final ClassName DELETE_EXCHANGE = ClassName.get(
            "org.springframework.web.service.annotation", "DeleteExchange");
    private static final ClassName PATCH_EXCHANGE = ClassName.get(
            "org.springframework.web.service.annotation", "PatchExchange");

    private static final ClassName PATH_VARIABLE = ClassName.get(
            "org.springframework.web.bind.annotation", "PathVariable");
    private static final ClassName REQUEST_PARAM = ClassName.get(
            "org.springframework.web.bind.annotation", "RequestParam");
    private static final ClassName REQUEST_HEADER = ClassName.get(
            "org.springframework.web.bind.annotation", "RequestHeader");
    private static final ClassName REQUEST_BODY = ClassName.get(
            "org.springframework.web.bind.annotation", "RequestBody");
    private static final ClassName NULLABLE =
            ClassName.get("org.jspecify.annotations", "Nullable");

    private final TypeNameResolver typeNameResolver;
    private final String clientPackage;

    public ClientWriter(String clientPackage, String modelsPackage) {
        this.clientPackage = clientPackage;
        this.typeNameResolver = new TypeNameResolver(modelsPackage);
    }

    public void writeAll(List<ClientDescriptor> clients, Path outputDirectory) throws IOException {
        writePackageInfo(outputDirectory);
        for (ClientDescriptor client : clients) {
            write(client, outputDirectory);
        }
    }

    public void write(ClientDescriptor client, Path outputDirectory) throws IOException {
        toJavaFile(client).writeTo(outputDirectory);
    }

    JavaFile toJavaFile(ClientDescriptor client) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(client.name())
                .addModifiers(Modifier.PUBLIC);

        for (OperationDescriptor operation : client.operations()) {
            interfaceBuilder.addMethod(toMethodSpec(operation));
        }

        return JavaFile.builder(clientPackage, interfaceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec toMethodSpec(OperationDescriptor operation) {
        String methodName = JavaIdentifierUtils.sanitize(toCamelCase(operation.name()));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(exchangeAnnotation(operation.method(), operation.path()));

        if (operation.deprecated()) {
            methodBuilder.addAnnotation(Deprecated.class);
        }

        if (operation.responseType() != null) {
            methodBuilder.returns(typeNameResolver.resolve(operation.responseType()));
        }

        for (ParameterDescriptor param : operation.parameters()) {
            methodBuilder.addParameter(toParameterSpec(param));
        }

        if (operation.requestBody() != null) {
            methodBuilder.addParameter(
                    ParameterSpec.builder(
                            typeNameResolver.resolve(operation.requestBody()), "body"
                    ).addAnnotation(REQUEST_BODY).build()
            );
        }

        return methodBuilder.build();
    }

    private AnnotationSpec exchangeAnnotation(OperationDescriptor.HttpMethod method, String path) {
        ClassName annotationType = switch (method) {
            case GET -> GET_EXCHANGE;
            case POST -> POST_EXCHANGE;
            case PUT -> PUT_EXCHANGE;
            case DELETE -> DELETE_EXCHANGE;
            case PATCH -> PATCH_EXCHANGE;
        };
        return AnnotationSpec.builder(annotationType)
                .addMember("value", "$S", path)
                .build();
    }

    private ParameterSpec toParameterSpec(ParameterDescriptor param) {
        ClassName annotation = switch (param.location()) {
            case PATH -> PATH_VARIABLE;
            case QUERY -> REQUEST_PARAM;
            case HEADER -> REQUEST_HEADER;
        };

        String javaName = JavaIdentifierUtils.sanitize(toCamelCase(param.name()));

        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(annotation);
        if (!javaName.equals(param.name())) {
            annotationBuilder.addMember("value", "$S", param.name());
        }
        if (param.location() == ParameterDescriptor.ParameterLocation.QUERY) {
            annotationBuilder.addMember("required", "$L", param.required());
        }

        ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                typeNameResolver.resolve(param.type()),
                javaName
        );

        if (param.location() == ParameterDescriptor.ParameterLocation.QUERY && !param.required()) {
            paramBuilder.addAnnotation(AnnotationSpec.builder(NULLABLE).build());
        }

        return paramBuilder.addAnnotation(annotationBuilder.build()).build();
    }

    private static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_' || c == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else if (i == 0) {
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void writePackageInfo(Path outputDirectory) throws IOException {
        Path packageDir = outputDirectory.resolve(clientPackage.replace('.', '/'));
        Files.createDirectories(packageDir);
        Path packageInfo = packageDir.resolve("package-info.java");
        Files.writeString(packageInfo, packageInfoSource(clientPackage));
    }

    private static String packageInfoSource(String packageName) {
        return "@NullMarked\n"
                + "package " + packageName + ";\n\n"
                + "import org.jspecify.annotations.NullMarked;\n";
    }
}
