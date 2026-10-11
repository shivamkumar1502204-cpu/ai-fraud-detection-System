package com.frauddetection.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for formatting and parsing standard ISO-8601 and presentation dates.
 */
public final class DateTimeUtil {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
        // Prevent instantiation
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    public static LocalDateTime parse(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString.trim(), DISPLAY_FORMATTER);
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(dateTimeString.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
