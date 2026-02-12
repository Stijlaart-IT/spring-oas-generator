package nl.stijlaartit.generator.engine.model;

import nl.stijlaartit.generator.engine.domain.EnumModel;
import nl.stijlaartit.generator.engine.domain.EnumValueType;
import nl.stijlaartit.generator.engine.domain.ModelFile;
import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.UnionModelFile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumModelWriterTest {

    private final EnumModelWriter writer = new EnumModelWriter("com.example.models");

    @Test
    void generatesEnumWithJsonPropertyValues() {
        EnumModel model = new EnumModel("PetStatus", List.of(
                "available",
                "pending",
                "sold"
        ), EnumValueType.STRING);

        String source = writer.toJavaFile(model, Map.of()).toString();

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

        String source = writer.toJavaFile(model, Map.of()).toString();

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

        String source = writer.toJavaFile(model, implementsByModel(List.of(model, union))).toString();

        assertTrue(source.contains("enum Mode"));
        assertTrue(source.contains("implements ModeWrapper"));
    }

    private static Map<String, List<String>> implementsByModel(List<ModelFile> models) {
        Map<String, List<String>> implementsByModel = new HashMap<>();
        for (ModelFile model : models) {
            if (!(model instanceof UnionModelFile unionModel)) {
                continue;
            }
            for (OneOfVariant variant : unionModel.getVariants()) {
                implementsByModel
                        .computeIfAbsent(variant.getModelName(), key -> new ArrayList<>())
                        .add(unionModel.getName());
            }
        }
        for (Map.Entry<String, List<String>> entry : implementsByModel.entrySet()) {
            List<String> distinct = new ArrayList<>(new LinkedHashSet<>(entry.getValue()));
            entry.setValue(distinct);
        }
        return implementsByModel;
    }
}
