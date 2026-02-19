package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.engine.GeneratedAnnotation;
import nl.stijlaartit.spring.oas.generator.engine.domain.EnumModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.EnumValueType;
import nl.stijlaartit.spring.oas.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.engine.domain.UnionModelFile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumModelSerializerTest {

    private final EnumModelSerializer writer = new EnumModelSerializer("com.example.models", ImplementsByMapping.empty());

    @Test
    void generatesEnumWithJsonPropertyValues() {
        EnumModel model = new EnumModel("PetStatus", List.of(
                "available",
                "pending",
                "sold"
        ), EnumValueType.STRING);

        String source = writer.toJavaFile(model).toString();

        assertGeneratedAnnotation(source);
        assertTrue(source.contains("enum PetStatus"));
        assertTrue(source.contains("@JsonProperty(\"available\")"));
        assertTrue(source.contains("@JsonProperty(\"pending\")"));
        assertTrue(source.contains("@JsonProperty(\"sold\")"));
    }

    @Test
    void generatesNumericEnumWithLiteralJsonProperty() {
        EnumModel model = new EnumModel("Mode", List.of(
                "-1",
                "0",
                "1"
        ), EnumValueType.NUMBER);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonValue"));
        assertTrue(source.contains("new BigDecimal(\"-1\")"));
        assertTrue(source.contains("new BigDecimal(\"0\")"));
        assertTrue(source.contains("new BigDecimal(\"1\")"));
    }

    @Test
    void enumImplementsInterfaceWhenListedInUnionVariants() {
        EnumModel model = new EnumModel("Mode", List.of("A", "B"), EnumValueType.STRING);
        UnionModelFile union = new UnionModelFile(
                "ModeWrapper",
                List.of(new OneOfVariant("Mode", "mode")),
                "type"
        );


        String source = new EnumModelSerializer("com.example.models", ImplementsByMapping.create(List.of(model, union)))
                .toJavaFile(model).toString();

        assertTrue(source.contains("enum Mode"));
        assertTrue(source.contains("implements ModeWrapper"));
    }

    private static void assertGeneratedAnnotation(String source) {
        assertTrue(source.contains("value = \"" + GeneratedAnnotation.VALUE + "\""));
        Pattern pattern = Pattern.compile(
                "@(?:javax\\.annotation\\.processing\\.)?Generated\\(\\s*value = \".+?\"\\s*,\\s*date = \"\\d{4}-\\d{2}-\\d{2}T[^\"]+\"\\s*\\)",
                Pattern.DOTALL
        );
        assertTrue(pattern.matcher(source).find());
    }
}
