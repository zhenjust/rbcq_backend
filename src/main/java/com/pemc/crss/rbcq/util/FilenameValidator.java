package com.pemc.crss.rbcq.util;

import com.pemc.crss.rbcq.enums.Regions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FilenameValidator {
    public static class ValidationResult {
        private final boolean valid;
        private final Regions region;
        private final LocalDate dateStart;
        private final LocalDateTime dateEnd;
        private final String error;

        public ValidationResult(boolean valid, Regions region, LocalDate dateStart, LocalDateTime dateEnd, String error) {
            this.valid = valid;
            this.region = region;
            this.dateStart = dateStart;
            this.dateEnd = dateEnd;
            this.error = error;
        }

        public boolean isValid() {
            return valid;
        }

        public Regions getRegion() {
            return region;
        }

        public LocalDate getDateStart() {
            return dateStart;
        }

        public LocalDateTime getDateEnd() {
            return dateEnd;
        }

        public String getError() {
            return error;
        }
    }

    public static ValidationResult validate(String filename) {
        try {
            String[] parts = filename.split("_");
            if (parts.length < 4) {
                return new ValidationResult(false, null, null, null, "Filename format is invalid.");
            }

            Regions region;
            try {
                region = Regions.valueOf(parts[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                return new ValidationResult(false, null, null, null, "Invalid region: " + parts[2]);
            }

            String dateStartStr = parts[3];
            String dateEndStr = parts[4];
            if (dateEndStr.toLowerCase().endsWith(".csv")) {
                dateEndStr = dateEndStr.substring(0, dateEndStr.length() - 4);
            }


            DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            LocalDate dateStart = LocalDate.parse(dateStartStr, startFormatter);
            LocalDateTime dateEnd = LocalDateTime.parse(dateEndStr, endFormatter);

            return new ValidationResult(true, region, dateStart, dateEnd, null);
        } catch (Exception e) {
            return new ValidationResult(false, null, null, null, "Unexpected error: " + e.getMessage());
        }
    }
}
