package com.pemc.crss.rbcq.service;

import com.jcraft.jsch.*;
import com.pemc.crss.rbcq.config.BCQPathProperties;
import com.pemc.crss.rbcq.util.FilenameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Vector;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectoryWatcherService {

    private final RbcqAuditService rbcqAuditService;
    private final BCQPathProperties bcqPathProperties;

    private final RbcqInitialService rbcqInitialService;
    private final RbcqFinalizeService rbcqFinalizeService;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void watchFolder() {

        while (true) {

            Session session = null;
            ChannelSftp sftp = null;

            try {

                JSch jsch = new JSch();

                session = jsch.getSession(
                        bcqPathProperties.getUsername(),
                        bcqPathProperties.getHost(),
                        bcqPathProperties.getPort()
                );

                session.setPassword(bcqPathProperties.getPassword());

                // For testing/internal SFTP server
                session.setConfig("StrictHostKeyChecking", "no");

                session.connect();

                Channel channel = session.openChannel("sftp");
                channel.connect();

                sftp = (ChannelSftp) channel;

                log.info("Connected to SFTP: {}", bcqPathProperties.getHost());

                while (true) {

                    Vector<ChannelSftp.LsEntry> files =
                            sftp.ls(bcqPathProperties.getUploadPath());

                    for (ChannelSftp.LsEntry file : files) {

                        String fileName = file.getFilename();

                        if (".".equals(fileName) || "..".equals(fileName)) {
                            continue;
                        }

                        if (!fileName.toLowerCase().endsWith(".csv")) {
                            continue;
                        }

                        log.info("📥 Detected new CSV file: {}", fileName);

                        try (InputStream inputStream =
                                     sftp.get(
                                             bcqPathProperties.getUploadPath()
                                                     + "/" + fileName)) {

                            // Import CSV into audit table
                            rbcqAuditService.importFromCsv(inputStream, fileName);

                            log.info("✅ Imported into RBCQ audit: {}", fileName);

                            // Move to processed folder
                            sftp.rename(
                                    bcqPathProperties.getUploadPath()
                                            + "/" + fileName,
                                    bcqPathProperties.getProcessedPath()
                                            + "/" + fileName
                            );

                            log.info("✅ Processed and moved file: {}", fileName);

                            // Filename validation
                            FilenameValidator.ValidationResult result =
                                    FilenameValidator.validate(fileName);

                            if (!result.isValid()) {
                                log.warn(
                                        "Filename validation failed: {}",
                                        result.getError()
                                );

                                // Move invalid file to rejected
                                sftp.rename(
                                        bcqPathProperties.getProcessedPath()
                                                + "/" + fileName,
                                        bcqPathProperties.getRejectedPath()
                                                + "/" + fileName
                                );

                                continue;
                            }

                            LocalDate startDateValue = result.getDateStart();

                            LocalDateTime startDate =
                                    startDateValue.atTime(0, 5, 0);

                            LocalDateTime endDate =
                                    startDateValue.plusDays(1)
                                            .atStartOfDay();

                            log.info(
                                    "Validated Region: {}, Start Date: {}, End Date: {}",
                                    result.getRegion(),
                                    result.getDateStart(),
                                    result.getDateEnd()
                            );

                            log.info("START DATE: {}", startDate);
                            log.info("END DATE: {}", endDate);

                            // Initial
                            rbcqInitialService.runInitialization(
                                    startDate,
                                    endDate,
                                    "SYSTEM"
                            );

                            log.info("✅ RBCQ Initial completed.");

                            // Finalize
                            rbcqFinalizeService.finalizeRbcq(
                                    startDate,
                                    endDate,
                                    "SYSTEM"
                            );

                            log.info("✅ RBCQ Finalization completed.");

                        } catch (Exception e) {

                            log.error(
                                    "❌ Error processing file {}",
                                    fileName,
                                    e
                            );

                            try {
                                sftp.rename(
                                        bcqPathProperties.getUploadPath()
                                                + "/" + fileName,
                                        bcqPathProperties.getRejectedPath()
                                                + "/" + fileName
                                );
                            } catch (Exception moveError) {
                                log.error(
                                        "❌ Failed to move file to rejected: {}",
                                        fileName,
                                        moveError
                                );
                            }
                        }
                    }

                    // Check SFTP every 10 seconds
                    Thread.sleep(10_000);
                }

            } catch (Exception e) {

                log.error("❌ SFTP watcher failed", e);

                // If connection drops, retry after 10 seconds
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }

            } finally {

                if (sftp != null && sftp.isConnected()) {
                    sftp.disconnect();
                }

                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }
    }
}

