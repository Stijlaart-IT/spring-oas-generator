package nl.stijlaartit.spring.oas.generator.engine.client;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.spring.oas.generator.engine.GeneratedAnnotation;
import nl.stijlaartit.spring.oas.generator.engine.domain.ApiFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFileSerializer;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.ParameterLocation;
import nl.stijlaartit.spring.oas.generator.engine.domain.ParameterModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.SerializedFile;
import nl.stijlaartit.spring.oas.generator.engine.model.JavaIdentifierUtils;
import nl.stijlaartit.spring.oas.generator.engine.model.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.model.TypeNameResolver;
import nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil;
import nl.stijlaartit.spring.oas.generator.engine.naming.OperationIdNaming;

import javax.lang.model.element.Modifier;
import java.util.Objects;

public class ClientWriter implements GenerationFileSerializer<ApiFile> {

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
    private static final ClassName RESPONSE_ENTITY =
            ClassName.get("org.springframework.http", "ResponseEntity");
    private static final ClassName MONO =
            ClassName.get("reactor.core.publisher", "Mono");

    private final TypeNameResolver typeNameResolver;
    private final String clientPackage;
    private final ClientWriterConfig config;

    public ClientWriter(String clientPackage, String modelsPackage, ClientWriterConfig config) {
        this.clientPackage = clientPackage;
        this.typeNameResolver = new TypeNameResolver(modelsPackage);
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public SerializedFile serialize(ApiFile file) {
        return new SerializedFile.Ast(clientPackage, toJavaFile(file));
    }

    @Override
    public boolean supports(GenerationFile generationFile) {
        return generationFile instanceof ApiFile;
    }

    JavaFile toJavaFile(ApiFile client) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(client.name())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GeneratedAnnotation.spec());

        for (OperationModel operation : client.getOperations()) {
            interfaceBuilder.addMethod(toMethodSpec(operation, false));
            interfaceBuilder.addMethod(toMethodSpec(operation, true));
        }

        return JavaFile.builder(clientPackage, interfaceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec toMethodSpec(OperationModel operation, boolean responseEntityVariant) {
        String methodName = methodNameFromOperationName(operation.name());
        if (responseEntityVariant) {
            methodName = methodName + "ResponseEntity";
        }

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(exchangeAnnotation(operation.method(), operation.path()));

        if (operation.deprecated()) {
            methodBuilder.addAnnotation(Deprecated.class);
        }

        methodBuilder.returns(resolveReturnType(operation, responseEntityVariant));

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

    private TypeName resolveReturnType(OperationModel operation, boolean responseEntityVariant) {
        TypeName baseType = operation.responseType() == null
                ? ClassName.get(Void.class)
                : typeNameResolver.resolve(operation.responseType());

        if (responseEntityVariant) {
            TypeName boxed = baseType.isPrimitive() ? baseType.box() : baseType;
            TypeName entityType = ParameterizedTypeName.get(RESPONSE_ENTITY, boxed);
            if (config.ioMode() == ClientWriterConfig.IoMode.REACTIVE) {
                return ParameterizedTypeName.get(MONO, entityType);
            }
            return entityType;
        }

        if (operation.responseType() == null) {
            if (config.ioMode() == ClientWriterConfig.IoMode.REACTIVE) {
                return ParameterizedTypeName.get(MONO, baseType);
            }
            return TypeName.VOID;
        }

        if (config.ioMode() == ClientWriterConfig.IoMode.REACTIVE) {
            TypeName boxed = baseType.isPrimitive() ? baseType.box() : baseType;
            return ParameterizedTypeName.get(MONO, boxed);
        }
        return baseType;
    }

    private String methodNameFromOperationName(OperationName name) {
        return JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(switch (name) {
            case OperationName.Id id -> id.value();
            case OperationName.PathAndMethod pathAndMethod ->
                    OperationIdNaming.fallbackOperationId(pathAndMethod.method(), pathAndMethod.path());
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
}
