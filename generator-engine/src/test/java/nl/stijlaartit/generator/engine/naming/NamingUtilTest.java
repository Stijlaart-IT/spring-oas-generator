package nl.stijlaartit.generator.engine.naming;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.generator.engine.schemas.SchemaParent;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NamingUtilTest {

    @Test
    void camelAndPascalCaseConversion() {
        assertEquals("userName", NamingUtil.toCamelCase("user_name"));
        assertEquals("userName", NamingUtil.toCamelCase("User-Name"));
        assertEquals("UserName", NamingUtil.toPascalCase("user_name"));
        assertEquals("UserName", NamingUtil.toPascalCase("user-name"));
    }

    @Test
    void validateNameAcceptsValidIdentifiers() {
        assertEquals("Pet", NamingUtil.validateName("Pet"));
    }

    @Test
    void validateNameRejectsInvalidIdentifiers() {
        assertThrows(IllegalStateException.class, () -> NamingUtil.validateName("_Pet"));
        assertThrows(IllegalStateException.class, () -> NamingUtil.validateName("pet"));
        assertThrows(IllegalStateException.class, () -> NamingUtil.validateName("Object"));
        assertThrows(IllegalStateException.class, () -> NamingUtil.validateName("Pet-Name"));
    }

    @Test
    void resolvesShortestComponentPath() {
        SchemaInstance root = new SchemaInstance(
                new StringSchema(),
                new SchemaParent.ComponentParent("pet"),
                "#/components/schemas/Pet"
        );
        SchemaInstance child = new SchemaInstance(
                new StringSchema(),
                new SchemaParent.SchemaInstanceParent(root, new SchemaParent.SchemaRelation.PropertyRelation("name")),
                "#/components/schemas/Pet/properties/name"
        );

        NamingUtil.PathName path = NamingUtil.findShortestComponentPath(List.of(child));

        assertEquals("Pet", path.base());
        assertEquals(List.of("name"), path.segments());
        assertEquals("PetName", path.toName());
    }

    @Test
    void resolvesOperationParameterPath() {
        Operation operation = new Operation().operationId("getPet");
        SchemaInstance root = new SchemaInstance(
                new StringSchema(),
                new SchemaParent.OperationParameterParent(
                        operation,
                        PathItem.HttpMethod.GET,
                        "/pet/{petId}",
                        "petId",
                        "path"
                ),
                "#/paths/~1pet~1{petId}/parameters/0"
        );
        SchemaInstance child = new SchemaInstance(
                new StringSchema(),
                new SchemaParent.SchemaInstanceParent(root, new SchemaParent.SchemaRelation.PropertyRelation("name")),
                "#/paths/~1pet~1{petId}/parameters/0/schema/properties/name"
        );

        NamingUtil.PathName path = NamingUtil.findShortestOperationPath(List.of(child));

        assertEquals("GetPetPathPetIdParameter", path.base());
        assertEquals("GetPetPathPetIdParameterName", path.toName());
    }
}
