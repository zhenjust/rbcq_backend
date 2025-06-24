package com.pemc.crss.rbcq.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FilenameDateExtractor {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("_(\\d{8})_(\\d{14})\\.csv$");



    public LocalDateTime extractDateTime(String fileName) {
        Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        if (matcher.find()) {
            String dateTimeStr = matcher.group(2); // e.g., "20250310115707"
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        throw new IllegalArgumentException("No valid datetime found in filename: " + fileName);
    }
}
