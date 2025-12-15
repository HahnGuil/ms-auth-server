package br.com.hahn.auth.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for converting and formatting date-time values.
 *
 * <p>This class provides methods to format {@link Instant} objects into
 * human-readable date-time strings using the system's default time zone
 * and locale. It is designed to be thread-safe and cannot be instantiated.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     String formattedDate = DateTimeConverter.formatInstant(Instant.now());
 *     String formattedNow = DateTimeConverter.formatInstantNow();
 * </pre>
 *
 * @author HahnGuil
 */
public final class DateTimeConverter {

    /**
     * Formats the given {@link Instant} into a date-time string.
     *
     * @param instant the {@link Instant} to format; if null, an empty string is returned
     * @return a formatted date-time string, or an empty string if the input is null
     */
    public static String formatInstant(Instant instant) {
        if (Objects.isNull(instant)) {
            return "";
        }
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        return zdt.format(FORMATTER);
    }

    /**
     * Formats the current moment into a date-time string.
     *
     * @return a formatted date-time string representing the current moment
     */
    public static String formatInstantNow() {
        return formatInstant(Instant.now());
    }

    /**
     * Formatter for date-time values, using the pattern "dd/MM/yyyy HH:mm:ss z"
     * and the default locale.
     */
    static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z")
                    .withLocale(Locale.getDefault());

    private DateTimeConverter() { }
}