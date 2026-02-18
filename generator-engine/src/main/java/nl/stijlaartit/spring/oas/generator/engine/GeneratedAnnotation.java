package nl.stijlaartit.spring.oas.generator.engine;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class GeneratedAnnotation {

    public static final String VALUE = "nl.stijlaartit.generator.engine.Generator";

    private static final ClassName GENERATED =
            ClassName.get("javax.annotation.processing", "Generated");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private GeneratedAnnotation() {
    }

    public static AnnotationSpec spec() {
        return AnnotationSpec.builder(GENERATED)
                .addMember("value", "$S", VALUE)
                .addMember("date", "$S", currentDate())
                .build();
    }

    public static String sourceLine() {
        return "@Generated(value = \"" + VALUE + "\", date = \"" + currentDate() + "\")";
    }

    private static String currentDate() {
        return ZonedDateTime.now().format(DATE_FORMATTER);
    }
}
