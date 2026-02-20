package nl.stijlaartit.spring.oas.generator.engine.naming;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.StringSchema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaParent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        SchemaInstance root = new SchemaInstance(
                new StringSchema(),
                new SchemaParent.ComponentParent("pet")
        );
        SchemaInstance child = new SchemaInstance(
                new StringSchema(),
                new SchemaParent.SchemaInstanceParent(root, new SchemaParent.SchemaRelation.PropertyRelation("name"))
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
                )
        );
        SchemaInstance child = new SchemaInstance(
                new StringSchema(),
                new SchemaParent.SchemaInstanceParent(root, new SchemaParent.SchemaRelation.PropertyRelation("name"))
        );

        NamingUtil.PathName path = NamingUtil.findShortestOperationPath(List.of(child));

        assertEquals("GetPetPathPetIdParameter", path.base());
        assertEquals("GetPetPathPetIdParameterName", path.toName());
    }
}
