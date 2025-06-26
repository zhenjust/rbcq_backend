package com.pemc.crss.rbcq.service;

import com.pemc.crss.rbcq.entity.AuditEntity;
import com.pemc.crss.rbcq.entity.InitialEntity;
import com.pemc.crss.rbcq.enums.Regions;
import com.pemc.crss.rbcq.repository.AuditRepository;
import com.pemc.crss.rbcq.repository.InitialRepository;
import com.pemc.crss.rbcq.util.RbcqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RbcqInitialService {

    private final AuditRepository auditRepository;
    private final InitialRepository initialRepository;

    // In-memory progress tracking
    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    public void init(String jobId) {
        progressMap.put(jobId, 0);
        log.info("Initialized jobId: {}", jobId);
    }

    public void update(String jobId, int percent) {
        progressMap.put(jobId, percent);
        log.info("Updated progress for jobId {}: {}%", jobId, percent);
    }

    public int getProgress(String jobId) {
        return progressMap.getOrDefault(jobId, 0);
    }

    public void complete(String jobId) {
        log.info("Completing jobId: {}", jobId);
        progressMap.remove(jobId);
    }

    @Transactional
    public void processInitialize(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atTime(0, 5);
        LocalDateTime endDateTime = endDate.atStartOfDay();

        initialRepository.deleteByTimeIntervalRange(startDateTime, endDateTime);

        for (Regions region : Regions.values()) {
            List<AuditEntity> results = auditRepository.findLatestEntriesForRegion(
                    region.name(),
                    startDateTime,
                    endDateTime
            );

            List<InitialEntity> initialEntities = results.stream()
                    .map(RbcqMapper::mapAuditToInitial)
                    .collect(Collectors.toList());

            initialRepository.saveAll(initialEntities);
        }
    }

    @Async
    @Transactional
    public void runProcessInBackground(LocalDate startDate, LocalDate endDate, String jobId) {
        this.init(jobId); // set 0%
        System.out.println("🚀 Async process started for job: " + jobId);

        LocalDateTime start = startDate.atTime(0, 5);
        LocalDateTime end = endDate.atStartOfDay();

        Regions[] regions = Regions.values();
        for (int i = 0; i < regions.length; i++) {
            Regions region = regions[i];

            List<AuditEntity> results = auditRepository.findLatestEntriesForRegion(
                    region.name(), start, end
            );

            List<InitialEntity> initialEntities = results.stream()
                    .map(RbcqMapper::mapAuditToInitial)
                    .collect(Collectors.toList());

            initialRepository.saveAll(initialEntities);

            int progress = (int) (((i + 1) / (double) regions.length) * 90) + 10;
            this.update(jobId, progress);

            System.out.println("✅ Progress update for job " + jobId + ": " + progress + "%");
        }

        this.update(jobId, 100); // set final progress

        // ✅ Delay cleanup so frontend can still fetch 100%
        try {
            Thread.sleep(3000); // wait 3 seconds before cleanup
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        this.complete(jobId); // optional cleanup
        System.out.println("✅ Job " + jobId + " completed and removed.");
    }


}
