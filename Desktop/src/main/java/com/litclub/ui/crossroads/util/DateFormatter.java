package com.litclub.ui.crossroads.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for formatting dates in a user-friendly way
 */
public class DateFormatter {

    /**
     * Formats a date relative to now (e.g., "today", "2 days ago", "3 weeks ago")
     */
    public static String formatRelative(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());

        if (daysAgo == 0) {
            return "today";
        } else if (daysAgo == 1) {
            return "yesterday";
        } else if (daysAgo < 7) {
            return daysAgo + " days ago";
        } else if (daysAgo < 30) {
            long weeksAgo = daysAgo / 7;
            return weeksAgo + (weeksAgo == 1 ? " week ago" : " weeks ago");
        } else if (daysAgo < 365) {
            long monthsAgo = daysAgo / 30;
            return monthsAgo + (monthsAgo == 1 ? " month ago" : " months ago");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            return dateTime.format(formatter);
        }
    }

    /**
     * Formats a date in a standard format (MMM d, yyyy)
     */
    public static String formatStandard(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return dateTime.format(formatter);
    }

    /**
     * Formats a date with time (MMM d, yyyy h:mm a)
     */
    public static String formatWithTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
        return dateTime.format(formatter);
    }
}
