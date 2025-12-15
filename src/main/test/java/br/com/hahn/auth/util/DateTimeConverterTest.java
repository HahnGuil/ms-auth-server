package br.com.hahn.auth.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeConverterTest {

    @Nested
    @DisplayName("Format Instant")
    class FormatInstant {

        @Test
        @DisplayName("Returns formatted string for valid Instant")
        void returnsFormattedStringForValidInstant() {
            Instant instant = Instant.parse("2023-10-01T12:00:00Z");
            String expected = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                    .format(DateTimeConverter.FORMATTER);

            String result = DateTimeConverter.formatInstant(instant);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Returns empty string for null Instant")
        void returnsEmptyStringForNullInstant() {
            String result = DateTimeConverter.formatInstant(null);

            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Format Instant Now")
    class FormatInstantNow {

        @Test
        @DisplayName("Returns formatted string for current Instant")
        void returnsFormattedStringForCurrentInstant() {
            String result = DateTimeConverter.formatInstantNow();

            assertEquals(Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeConverter.FORMATTER), result);
        }
    }
}