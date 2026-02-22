package nl.stijlaartit.spring.oas.generator.engine.naming;

import io.swagger.v3.oas.models.media.StringSchema;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamingUtilTest {

    @Test
    void camelAndPascalCaseConversion() {
        assertEquals("userName", NamingUtil.toCamelCase("user_name"));
        assertEquals("userName", NamingUtil.toCamelCase("User-Name"));
        assertEquals("UserName", NamingUtil.toPascalCase("user_name"));
        assertEquals("UserName", NamingUtil.toPascalCase("user-name"));
    }

    @Test
    void resolvesShortestComponentPath() {
        SchemaInstance first = new SchemaInstance(
                new StringSchema(),
                SchemaPath.forRoot(PathRoot.componentSchema("pet")).property("name")
        );

        SchemaInstance second = new SchemaInstance(
                new StringSchema(),
                SchemaPath.forRoot(PathRoot.componentSchema("pet")).property("name").property("other")
        );


        final var path = NamingUtil.findShortestComponentPath(List.of(first, second));

        assertThat(path).isEqualTo(first.path());
    }

    @Test
    void resolvesOperationParameterPath() {
        SchemaInstance first = new SchemaInstance(
                new StringSchema(),
                SchemaPath.forRoot(PathRoot.requestParam(OperationName.id("getPet"), "petId")).property("name")
        );
        SchemaInstance second = new SchemaInstance(
                new StringSchema(),
                SchemaPath.forRoot(PathRoot.requestParam(OperationName.id("getPet"), "petId")).property("name").property("other")
        );

        final var path = NamingUtil.findShortestOperationPath(List.of(
                first,
                second
        ));

        assertThat(path).isEqualTo(first.path());
    }

    @Test
    void convertsPartsToReservedTypeName() {
        JavaTypeName name = NamingUtil.toJavaTypeName(List.of("/enum"));

        assertInstanceOf(JavaTypeName.Reserved.class, name);
        assertEquals("Enum", name.value());
    }

    @Test
    void convertsPartsToGeneratedTypeName() {
        JavaTypeName name = NamingUtil.toJavaTypeName(List.of("GET", "RequestBody"));

        assertInstanceOf(JavaTypeName.Generated.class, name);
        assertEquals("GetRequestBody", name.value());
    }

    @Test
    void prefixesTypeWhenNameStartsWithDigit() {
        JavaTypeName name = NamingUtil.toJavaTypeName(List.of("/123/foo"));

        assertEquals("Type123Foo", name.value());
        assertTrue(name instanceof JavaTypeName.Generated);
    }

    @Test
    void stripsInvalidCharactersAndJoinsParts() {
        JavaTypeName name = NamingUtil.toJavaTypeName(List.of("/pet-id", "foo@bar"));

        assertEquals("PetIdFooBar", name.value());
    }

    @Test
    void fallsBackToTypeWhenNoValidParts() {
        JavaTypeName name = NamingUtil.toJavaTypeName(List.of("", " / ", "!!!"));

        assertEquals("Type", name.value());
    }
}
