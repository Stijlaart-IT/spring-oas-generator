package nl.stijlaartit.generator.engine.client;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.engine.domain.ApiFile;
import nl.stijlaartit.generator.engine.domain.GenerationFileWriter;
import nl.stijlaartit.generator.engine.domain.HttpMethod;
import nl.stijlaartit.generator.engine.domain.OperationModel;
import nl.stijlaartit.generator.engine.domain.OperationName;
import nl.stijlaartit.generator.engine.domain.ParameterLocation;
import nl.stijlaartit.generator.engine.domain.ParameterModel;
import nl.stijlaartit.generator.engine.domain.WriteReport;
import nl.stijlaartit.generator.engine.model.JavaIdentifierUtils;
import nl.stijlaartit.generator.engine.model.TypeDescriptor;
import nl.stijlaartit.generator.engine.model.TypeNameResolver;
import nl.stijlaartit.generator.engine.naming.NamingUtil;
import nl.stijlaartit.generator.engine.naming.OperationIdNaming;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ClientWriter implements GenerationFileWriter<ApiFile> {

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

    @Override
    public WriteReport writeAll(List<ApiFile> clients, Path outputDirectory) throws IOException {
        WriteReport report = new WriteReport();
        report.recordFile(writePackageInfo(outputDirectory));
        for (ApiFile client : clients) {
            write(client, outputDirectory);
            report.recordFile(clientPath(outputDirectory, client.name()));
        }
        return report;
    }

    public void write(ApiFile client, Path outputDirectory) throws IOException {
        toJavaFile(client).writeTo(outputDirectory);
    }

    JavaFile toJavaFile(ApiFile client) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(client.name())
                .addModifiers(Modifier.PUBLIC);

        for (OperationModel operation : client.getOperations()) {
            interfaceBuilder.addMethod(toMethodSpec(operation));
        }

        return JavaFile.builder(clientPackage, interfaceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec toMethodSpec(OperationModel operation) {
        String methodName = methodNameFromOperationName(operation.name());

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(exchangeAnnotation(operation.method(), operation.path()));

        if (operation.deprecated()) {
            methodBuilder.addAnnotation(Deprecated.class);
        }

        if (operation.responseType() != null) {
            methodBuilder.returns(typeNameResolver.resolve(operation.responseType()));
        }

        for (ParameterModel param : operation.parameters()) {
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

    private String methodNameFromOperationName(OperationName name) {
        return JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(switch (name) {
            case OperationName.Id id -> id.value();
            case OperationName.PathAndMethod pathAndMethod -> OperationIdNaming.fallbackOperationId(pathAndMethod.method(), pathAndMethod.path());
        }));
    }

    private AnnotationSpec exchangeAnnotation(HttpMethod method, String path) {
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

    private ParameterSpec toParameterSpec(ParameterModel param) {
        ClassName annotation = switch (param.location()) {
            case PATH -> PATH_VARIABLE;
            case QUERY -> REQUEST_PARAM;
            case HEADER -> REQUEST_HEADER;
        };

        String javaName = JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(param.name()));

        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(annotation);
        if (!javaName.equals(param.name())) {
            annotationBuilder.addMember("value", "$S", param.name());
        }
        if (param.location() == ParameterLocation.QUERY) {
            if (!param.required()) {
                annotationBuilder.addMember("required", "$L", false);
            } else if (!(param.type() instanceof TypeDescriptor.ListType)) {
                annotationBuilder.addMember("required", "$L", true);
            }
        }

        ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                typeNameResolver.resolve(param.type()),
                javaName
        );

        if (param.location() == ParameterLocation.QUERY && !param.required()) {
            paramBuilder.addAnnotation(AnnotationSpec.builder(NULLABLE).build());
        }

        return paramBuilder.addAnnotation(annotationBuilder.build()).build();
    }

    private Path writePackageInfo(Path outputDirectory) throws IOException {
        Path packageDir = outputDirectory.resolve(clientPackage.replace('.', '/'));
        Files.createDirectories(packageDir);
        Path packageInfo = packageDir.resolve("package-info.java");
        Files.writeString(packageInfo, packageInfoSource(clientPackage));
        return packageInfo;
    }

    private Path clientPath(Path outputDirectory, String clientName) {
        return outputDirectory.resolve(clientPackage.replace('.', '/'))
                .resolve(clientName + ".java");
    }

    private static String packageInfoSource(String packageName) {
        return "@NullMarked\n"
                + "package " + packageName + ";\n\n"
                + "import org.jspecify.annotations.NullMarked;\n";
    }
}
