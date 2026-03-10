package com.pemc.crss.rbcq.service;

import com.pemc.crss.rbcq.dto.RspotAggResult;
import com.pemc.crss.rbcq.entity.*;
import com.pemc.crss.rbcq.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RbcqFinalizeService {

    private final OutputBidRTDRepository outputRepo;
    private final InitialRepository rbcqRepo;
    private final OpresRTDRepository opresRepo;
    private final ReserveSpotRepository tempAggRepo;
    private final RbcqFinalRepository rbcqFinalRepository;

    /* ============================================================
       PHASE 1 — TEMP_RSPOTQ_AGG (AS_BUY / AS_SELL)
       ============================================================ */
    @Transactional
    public void calculateAndSave(LocalDateTime start, LocalDateTime end) {
        tempAggRepo.deleteTempAggBetween(start,end);
        int rowsInserted = tempAggRepo.insertToRspotAgg(start, end);


        log.info("GROUPED rows inserted = {}", rowsInserted);
    }

    /* ============================================================
       PHASE 2 — FINAL RBCQ (SQL FINAL SELECT)
       ============================================================ */
    @Transactional
    public void finalizeRbcq(LocalDateTime from, LocalDateTime to, String userId) {
        log.info("=== Start RBCQ Finalization ===");

        calculateAndSave(from,to);
        rbcqFinalRepository.deleteByTimeIntervalRange(from,to);

        int rowsInserted = rbcqFinalRepository.insertToRbcqFinal(from,to,userId);

        log.info("Rows inserted into RBCQ_FINALIZE_TEST: {}", rowsInserted);

        log.info("=== End RBCQ Finalization ===");
    }
    @Transactional
    public void processAP(LocalDateTime from, LocalDateTime to, String userId,String region) {
        log.info("=== Start RBCQ AP Flagging ===");

        calculateAndSave(from,to);
        rbcqFinalRepository.deleteByTimeIntervalRange(from,to);

        int rowsInserted = rbcqFinalRepository.applyAPFlag(from,to,userId);
        int rowsInsertedAP = rbcqFinalRepository.insertAPFlag(from,to,region,userId);

        log.info("Rows inserted into AP FLAG: {}", rowsInserted);

        log.info("=== End RBCQ AP FLAGGING ===");
    }



}
