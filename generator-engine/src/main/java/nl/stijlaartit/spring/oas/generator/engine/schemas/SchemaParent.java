package nl.stijlaartit.spring.oas.generator.engine.schemas;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

public sealed interface SchemaParent permits
        SchemaParent.ComponentParent,
        SchemaParent.ComponentParameterParent,
        SchemaParent.OperationParameterParent,
        SchemaParent.OperationRequestParent,
        SchemaParent.OperationResponseParent,
        SchemaParent.SchemaInstanceParent {

    sealed interface SchemaRelation permits
            SchemaRelation.PropertyRelation,
            SchemaRelation.ListItemRelation,
            SchemaRelation.OneOfRelation,
            SchemaRelation.AllOfRelation,
            SchemaRelation.AnyOfRelation,
            SchemaRelation.AdditionalPropertiesRelation {

        record PropertyRelation(String propertyName) implements SchemaRelation {
        }

        record ListItemRelation() implements SchemaRelation {
        }

        record OneOfRelation(int index) implements SchemaRelation {
        }

        record AllOfRelation(int index) implements SchemaRelation {
        }

        record AnyOfRelation(int index) implements SchemaRelation {
        }

        record AdditionalPropertiesRelation() implements SchemaRelation {
        }
    }

    record ComponentParent(String componentName) implements SchemaParent {
    }

    record ComponentParameterParent(String parameterName) implements SchemaParent {
    }

    record OperationParameterParent(Operation operation,
                                    PathItem.HttpMethod method,
                                    String path,
                                    String parameterName,
                                    String parameterIn) implements SchemaParent {
    }

    record OperationRequestParent(Operation operation, PathItem.HttpMethod method, String path) implements SchemaParent {
    }

    record OperationResponseParent(Operation operation, String statusCode, PathItem.HttpMethod method, String path)
            implements SchemaParent {
    }

    record SchemaInstanceParent(SchemaInstance parent, SchemaRelation relation) implements SchemaParent {
    }
}
