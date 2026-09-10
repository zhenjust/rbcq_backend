package com.pemc.crss.rbcq.service;

import com.pemc.crss.rbcq.entity.AuditEntity;
import com.pemc.crss.rbcq.repository.AuditRepository;
import com.pemc.crss.rbcq.util.FilenameDateExtractor;
import com.pemc.crss.rbcq.util.FilenameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RbcqAuditService {

    private final AuditRepository auditRepository;
    private final FilenameDateExtractor filenameDateExtractor;
    public void importFromCsv(InputStream csvInputStream, String fileName) {

//        FilenameValidator.ValidationResult result = FilenameValidator.validate(fileName);
//
//
//        if (result.isValid()) {
//            System.out.println("Region: " + result.getRegion());
//            System.out.println("Start Date: " + result.getDateStart());
//            System.out.println("End Date: " + result.getDateEnd());
//        } else {
//            System.out.println("Validation failed: " + result.getError());
//        }

        List<AuditEntity> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvInputStream, StandardCharsets.UTF_8))) {

            LocalDateTime fileDateTime = filenameDateExtractor.extractDateTime(fileName);
            log.info("Extracted date from file name: {}", fileDateTime);

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");
            List<String> headerList = Arrays.stream(headers)
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<String> requiredHeaders = List.of(
                    "TIME_INTERVAL",
                    "REGION_NAME",
                    "RESOURCE_NAME",
                    "RESERVE_TYPE",
                    "RESERVE_BCQ"
            );

            if (!headerList.equals(requiredHeaders)) {
                throw new IllegalArgumentException("Invalid CSV headers" + requiredHeaders);
            }


            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatted = fileDateTime.format(outputFormatter);

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");





            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                AuditEntity entry = new AuditEntity();
                entry.setTimeInterval(LocalDateTime.parse(parts[0].trim(), inputFormatter));
                entry.setRegionName(parts[1].trim());
                entry.setResourceName(parts[2].trim());
                entry.setCommodity(parts[3].trim());
                entry.setMw(Double.parseDouble(parts[4].trim()));
                entry.setFileName(fileName); // ✅
                entry.setFileDate(fileDateTime);
                entry.setPublishDate(LocalDateTime.now());

                entries.add(entry);
            }

            try {
                auditRepository.saveAll(entries);
                log.info("Imported {} records from CSV", entries.size());
            } catch (DataIntegrityViolationException e) {
                Throwable root = getRootCause(e);
                if (root instanceof ConstraintViolationException && root.getMessage().contains("UK_AUDIT_UNIQUE_FIELDS")) {
                    throw new RuntimeException("Duplicate entry detected based on TIME_INTERVAL, RESOURCE_NAME, RESERVE_TYPE, FILE_NAME.");
                }
                log.error("Failed to import CSV due to DB constraint", e);
                throw new RuntimeException("CSV import failed: " + root.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to import CSV", e);
            throw new RuntimeException("CSV import failed: " + e.getMessage());
        }
    }


    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause != null && cause != throwable) ? getRootCause(cause) : throwable;
    }


}