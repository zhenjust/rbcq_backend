package com.pemc.crss.rbcq.resource;

import com.pemc.crss.rbcq.dto.RequestDTO;
import com.pemc.crss.rbcq.dto.ViewDTO;
import com.pemc.crss.rbcq.entity.FinalizeEntity;
import com.pemc.crss.rbcq.service.RbcqAuditService;
import com.pemc.crss.rbcq.service.RbcqFinalizeService;
import com.pemc.crss.rbcq.service.RbcqInitialService;
import com.pemc.crss.rbcq.util.FilenameValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rbcq")
@AllArgsConstructor
@Slf4j
public class RbcqResource {

    private final RbcqAuditService rbcqAuditService;
    private final RbcqInitialService rbcqInitialService;
    private final RbcqFinalizeService rbcqFinalizeService;

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

            LocalDate startDateValue = result.getDateStart();
            LocalDateTime startDate = startDateValue.atTime(0, 5, 0);
            LocalDateTime endDate = startDateValue.plusDays(1).atStartOfDay();

            rbcqAuditService.importFromCsv(file.getInputStream(), fileName);
            rbcqInitialService.runInitialization(startDate,endDate,"SYSTEM");
            log.info("✅ RBCQ Initial completed.");

            rbcqFinalizeService.finalizeRbcq(startDate, endDate, "SYSTEM");
            log.info("✅ RBCQ Finalization completed.");
            return ResponseEntity.ok("CSV import successful: " + fileName);

        } catch (Exception e) {
            Throwable rootCause = getRootCause(e);
            log.error("CSV import failed", e);
            return ResponseEntity.status(500).body("CSV import failed: " + rootCause.getMessage());
        }
    }

//    @PostMapping("/initialize")
//    public ResponseEntity<String> processInitialize(@RequestBody InitializationRequestDTO request) {
//        try {
//
//
//            LocalDateTime start =
//                    request.getStartDatetime().atTime(0, 5);   // ✅ 00:05
//
//            LocalDateTime end =
//                    request.getEndDatetime()
//                            .plusDays(1)
//                            .atStartOfDay();
//            String jobId = UUID.randomUUID().toString();
//            rbcqInitialService.runAsyncInitialization(start, end, jobId);
//
//
//
//            return ResponseEntity.ok(jobId);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Initialization failed: " + ex.getMessage());
//        }
//    }

    @PostMapping("/finalize")
    public ResponseEntity<String> finalizeRbcq(
            @RequestBody RequestDTO request) {

        LocalDateTime from =
                request.getStartDatetime();

        LocalDateTime to =
                request.getEndDatetime();

        String userId = request.getUserId();

        rbcqFinalizeService.finalizeRbcq(from, to, userId);

        return ResponseEntity.ok("RBCQ finalized successfully");
    }


    @PostMapping("/initialize")
    public ResponseEntity<String> initializeRbcq(
            @RequestBody RequestDTO request) {

        LocalDateTime from =
                request.getStartDatetime();

        LocalDateTime to =
                request.getEndDatetime();

        String userId = request.getUserId();

        rbcqInitialService.runInitialization(from, to, userId);

        return ResponseEntity.ok("RBCQ initialize successfully");
    }

    @PostMapping("/ap_flag")
    public ResponseEntity<String> processApFlag(
            @RequestBody RequestDTO request) {



        LocalDateTime from =
                request.getStartDatetime();

        LocalDateTime to =
                request.getEndDatetime();

        String userId = request.getUserId();
        log.info("Regions: {}", request.getRegions());
        List<String> regions = request.getRegions();
        String region = String.join(",", regions);
        log.info("Region parameter: {}", region);

        rbcqFinalizeService.processAP(from, to, userId,region);

        return ResponseEntity.ok("AP Flagging successfully");
    }


    @GetMapping("/initialize/progress/{jobId}")
    public ResponseEntity<Integer> getProgress(@PathVariable String jobId) {
        int progress = rbcqInitialService.getProgress(jobId);
        return ResponseEntity.ok(progress);
    }

//    @GetMapping("/finalized")
//    public ResponseEntity<List<FinalizeEntity>> getFinalizedData(
//            @RequestParam String from,
//            @RequestParam String to,
//            @RequestParam(defaultValue = "ALL") String region,
//            @RequestParam String userId
//    ) {
//
//        LocalDateTime fromDate = LocalDateTime.parse(from);
//        LocalDateTime toDate = LocalDateTime.parse(to);
//
//        return ResponseEntity.ok(
//                rbcqFinalizeService.viewFinalize(fromDate, toDate, region, userId)
//        );
//    }

    @GetMapping("/finalized")
    public List<ViewDTO> getMtns(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {

        LocalDateTime fromDate = LocalDateTime.parse(startDate);
        LocalDateTime toDate = LocalDateTime.parse(endDate);

        List<ViewDTO> result =
                rbcqFinalizeService.getFinalData(fromDate, toDate);

        System.out.println("================================");
        System.out.println("START DATE: " + fromDate);
        System.out.println("END DATE: " + toDate);
        System.out.println("RESULT SIZE: " + result.size());
        System.out.println("RESULT: " + result);
        System.out.println("================================");

        return result;
    }




    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return getRootCause(cause);
        }
        return throwable;
    }

}
