package com.pemc.crss.rbcq.service;

import com.pemc.crss.rbcq.config.BCQPathProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectoryWatcherService {

    private final RbcqAuditService rbcqAuditService;
    private final BCQPathProperties bcqPathProperties;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void watchFolder() {
        try {
            Path folderPath = Paths.get(bcqPathProperties.getUploadPath());
            Path processedPath = Paths.get(bcqPathProperties.getProcessedPath());
            Path rejectedPath = Paths.get(bcqPathProperties.getRejectedPath());

            Files.createDirectories(folderPath);
            Files.createDirectories(processedPath);
            Files.createDirectories(rejectedPath);

            WatchService watchService = FileSystems.getDefault().newWatchService();
            folderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            log.info("Watching folder for CSV files: {}", folderPath);

            while (true) {
                WatchKey key = watchService.poll();
                if (key == null) {
                    try {
                        Thread.sleep(10_000); // 10 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Watcher thread interrupted", e);
                        return;
                    }
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path relativePath = (Path) event.context();
                        Path newFile = folderPath.resolve(relativePath);
                        String fileName = newFile.getFileName().toString();

                        if (fileName.toLowerCase().endsWith(".csv")) {
                            log.info("📥 Detected new CSV file: {}", fileName);
                            try (FileInputStream fis = new FileInputStream(newFile.toFile())) {

                                rbcqAuditService.importFromCsv(fis, fileName);

                                Files.move(newFile, processedPath.resolve(fileName),
                                        StandardCopyOption.REPLACE_EXISTING);

                                log.info("✅ Processed and moved file: {}", fileName);

                            } catch (Exception e) {
                                log.error("❌ Error processing file {}", fileName, e);
                                Files.move(newFile,rejectedPath.resolve(fileName),
                                        StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }

                key.reset();
            }

        } catch (Exception e) {
            log.error("❌ Folder watcher failed", e);
        }
    }
}
