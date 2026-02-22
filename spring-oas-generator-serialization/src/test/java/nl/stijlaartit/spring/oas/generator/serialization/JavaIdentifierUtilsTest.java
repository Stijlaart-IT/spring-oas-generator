package nl.stijlaartit.spring.oas.generator.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaIdentifierUtilsTest {

    @Test
    void sanitizesNullAndBlankToDefault() {
        assertEquals("value", JavaIdentifierUtils.sanitize(""));
        assertEquals("value", JavaIdentifierUtils.sanitize("   "));
    }

    @Test
    void sanitizesInvalidCharactersAndLeadingDigits() {
        assertEquals("_1abc", JavaIdentifierUtils.sanitize("1abc"));
        assertEquals("abc_def", JavaIdentifierUtils.sanitize("abc-def"));
        assertEquals("value", JavaIdentifierUtils.sanitize("_"));
        assertEquals("value", JavaIdentifierUtils.sanitize("@"));
    }

    @Test
    void appendsUnderscoreForReservedWords() {
        assertEquals("class_", JavaIdentifierUtils.sanitize("class"));
        assertEquals("record_", JavaIdentifierUtils.sanitize("record"));
    }
}
