package com.pemc.crss.rbcq.resource;

import com.pemc.crss.rbcq.service.RbcqAuditService;
import com.pemc.crss.rbcq.util.FilenameValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rbcq")
@AllArgsConstructor
@Slf4j
public class RbcqResource {

    private final RbcqAuditService rbcqAuditService;

    @PostMapping("/import")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) {
        log.info("Received file: name={}, size={}, contentType={}",
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );

        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                return ResponseEntity.badRequest().body("Filename is missing.");
            }

            // ✅ Validate filename
            FilenameValidator.ValidationResult result = FilenameValidator.validate(fileName);
            if (!result.isValid()) {
                log.warn("Filename validation failed: {}", result.getError());
                return ResponseEntity.badRequest().body("Filename validation failed: " + result.getError());
            }

            // ✅ Log extracted values if needed
            log.info("Validated Region: {}, Start Date: {}, End Date: {}",
                    result.getRegion(), result.getDateStart(), result.getDateEnd());

            // ✅ Proceed with CSV import
            rbcqAuditService.importFromCsv(file.getInputStream(), fileName);
            return ResponseEntity.ok("CSV import successful: " + fileName);

        } catch (Exception e) {
            Throwable rootCause = getRootCause(e);
            log.error("CSV import failed", e);
            return ResponseEntity.status(500).body("CSV import failed: " + rootCause.getMessage());
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return getRootCause(cause);
        }
        return throwable;
    }

}
