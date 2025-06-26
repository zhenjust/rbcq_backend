package com.pemc.crss.rbcq.resource;

import com.pemc.crss.rbcq.dto.InitializationRequestDTO;
import com.pemc.crss.rbcq.service.RbcqAuditService;
import com.pemc.crss.rbcq.service.RbcqInitialService;
import com.pemc.crss.rbcq.util.FilenameValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/rbcq")
@AllArgsConstructor
@Slf4j
public class RbcqResource {

    private final RbcqAuditService rbcqAuditService;
    private final RbcqInitialService rbcqInitialService;

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

            FilenameValidator.ValidationResult result = FilenameValidator.validate(fileName);
            if (!result.isValid()) {
                log.warn("Filename validation failed: {}", result.getError());
                return ResponseEntity.badRequest().body("Filename validation failed: " + result.getError());
            }

            log.info("Validated Region: {}, Start Date: {}, End Date: {}",
                    result.getRegion(), result.getDateStart(), result.getDateEnd());

            rbcqAuditService.importFromCsv(file.getInputStream(), fileName);
            return ResponseEntity.ok("CSV import successful: " + fileName);

        } catch (Exception e) {
            Throwable rootCause = getRootCause(e);
            log.error("CSV import failed", e);
            return ResponseEntity.status(500).body("CSV import failed: " + rootCause.getMessage());
        }
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> processInitialize(@RequestBody InitializationRequestDTO request) {
        try {
            LocalDate start = request.getStartDatetime();
            LocalDate end = request.getEndDatetime();
            String jobId = UUID.randomUUID().toString();
            rbcqInitialService.runProcessInBackground(start, end, jobId);

            return ResponseEntity.ok(jobId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Initialization failed: " + ex.getMessage());
        }
    }



    @GetMapping("/initialize/progress/{jobId}")
    public ResponseEntity<Integer> getProgress(@PathVariable String jobId) {
        int progress = rbcqInitialService.getProgress(jobId);
        return ResponseEntity.ok(progress);
    }




    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return getRootCause(cause);
        }
        return throwable;
    }

}
