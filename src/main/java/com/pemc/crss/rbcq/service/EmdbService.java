package com.pemc.crss.rbcq.service;

import com.pemc.crss.rbcq.dto.SelectedAPInterval;
import com.pemc.crss.rbcq.repository.EMDBRepository;
import com.pemc.crss.rbcq.util.SecurityAuditorAware;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EmdbService {

    private final EMDBRepository emdbRepository;
    private final SecurityAuditorAware securityAuditorAware;

    ///*****************ASIE*****************///

    @Transactional
    public void ASIEreserve(LocalDateTime start, LocalDateTime end) {

        String linkedUserName = securityAuditorAware.getCurrentUsername()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        int rowsInserted =
                emdbRepository.asie_reserve(
                        start,
                        end,
                        linkedUserName
                );

        log.info("GROUPED rows inserted = {}", rowsInserted);
    }

    @Transactional
    public void deleteASIEreserve(
            LocalDateTime start,
            LocalDateTime end,
            List<SelectedAPInterval> selectedAPIntervals) {

        List<LocalDateTime> selectedIntervals =
                selectedAPIntervals.stream()
                        .map(SelectedAPInterval::getDispatchInterval)
                        .collect(Collectors.toList());

        List<String> selectedRegions =
                selectedAPIntervals.stream()
                        .map(SelectedAPInterval::getRegion)
                        .collect(Collectors.toList());

        int rowsDeleted =
                emdbRepository.deleteASIEreserveWithAP(
                        start,
                        end,
                        selectedIntervals,
                        selectedRegions
                );

        log.info("ASIE reserve rows deleted = {}", rowsDeleted);
    }

    @Transactional
    public void ASIEReserveAP(
            LocalDateTime start,
            LocalDateTime end,
            List<SelectedAPInterval> selectedAPIntervals) {

        String linkedUserName = securityAuditorAware.getCurrentUsername()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        List<LocalDateTime> selectedIntervals =
                selectedAPIntervals.stream()
                        .map(SelectedAPInterval::getDispatchInterval)
                        .collect(Collectors.toList());

        List<String> selectedRegions =
                selectedAPIntervals.stream()
                        .map(SelectedAPInterval::getRegion)
                        .collect(Collectors.toList());

        int rowsInserted =
                emdbRepository.ASIEreserveWithAP(
                        start,
                        end,
                        selectedIntervals,
                        selectedRegions,
                        linkedUserName
                );

        log.info("ASIE reserve rows inserted = {}", rowsInserted);
    }
}