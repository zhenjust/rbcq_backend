package com.pemc.crss.rbcq.service;

import com.pemc.crss.rbcq.dto.APDTO;
import com.pemc.crss.rbcq.dto.RspotAggResult;
import com.pemc.crss.rbcq.dto.ViewDTO;
import com.pemc.crss.rbcq.entity.*;
import com.pemc.crss.rbcq.repository.*;
import com.pemc.crss.rbcq.util.SecurityAuditorAware;
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

    private final SecurityAuditorAware securityAuditorAware;

    private final CRSSRepository crssRepository;

    private ViewDTO viewDTO;

    /* ============================================================
       PHASE 1 — TEMP_RSPOTQ_AGG (AS_BUY / AS_SELL)
       ============================================================ */
    @Transactional
    public void calculateAndSave(LocalDateTime start, LocalDateTime end) {
        tempAggRepo.deleteTempAggBetween(start,end);
        int rowsInserted = tempAggRepo.insertToRspotAgg(start, end);


        log.info("GROUPED rows inserted = {}", rowsInserted);
    }


    @Transactional
    public void finalizeRbcq(LocalDateTime from, LocalDateTime to, String userId) {
        log.info("=== Start RBCQ Finalization ===");
        System.out.println(from);
        System.out.println(to);
        System.out.println(userId);
        calculateAndSave(from,to);

        log.info("RBCQ_INITIAL_TEST count = {}", rbcqFinalRepository.countRbcqInitial(from,to));
        log.info("EMM_HISV_OUTPUT_BID_RTD count = {}", rbcqFinalRepository.countOutputBid(from,to));
        log.info("OPRES_RTD_TEST count = {}", rbcqFinalRepository.countOpres(from,to));
        log.info("TEMP_RSPOTQ_AGG_TEST count = {}", rbcqFinalRepository.countTempAgg(from,to));

        rbcqFinalRepository.deleteByTimeIntervalRange(from,to);
        log.info("done delete");


        int rowsInserted = rbcqFinalRepository.insertToRbcqFinal(from,to,userId);
        log.info("start apply ap flag");
        int rowsInsertedFlag = rbcqFinalRepository.applyAPFlag(from,to,userId);
        log.info("done apply ap flag");

        log.info("Rows inserted into RBCQ_FINALIZE_TEST: {}", rowsInserted);
        log.info("Rows inserted into RBCQ_FINALIZE_TEST as AP: {}", rowsInsertedFlag);

        log.info("=== End RBCQ Finalization ===");
    }
    @Transactional
    public void processAP(LocalDateTime from, LocalDateTime to,String region) {

        log.info("=== Start RBCQ AP Flagging ===");

        calculateAndSave(from,to);
        rbcqFinalRepository.deleteByTimeIntervalRangeAP(from,to);
        log.info("start: {}", from);
        log.info("end: {}", to);
        log.info("region: {}", region);

        String linkedUserName = securityAuditorAware.getCurrentUsername()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));



        int rowsInsertedAP = rbcqFinalRepository.insertAPFlag(from,to,region,linkedUserName,"Y");

        log.info("Rows inserted into AP FLAG: {}", rowsInsertedAP);

        log.info("=== End RBCQ AP FLAGGING ===");
    }
//    public List<FinalizeEntity> viewFinalize(
//            LocalDateTime from,
//            LocalDateTime to,
//            String region,
//            String userId) {
//
//        return rbcqFinalRepository.viewFinalize(from, to, region, userId);
//    }

    public List<ViewDTO> getFinalData(LocalDateTime startDate, LocalDateTime endDate) {

        Long linkedUserId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));



        System.out.println("linkedUserId: " + linkedUserId);


        List<ViewDTO> result = crssRepository.getFinalData(linkedUserId, startDate, endDate);


        return result;
    }

    public List<APDTO> getFlaggedData(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        Long linkedUserId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        return rbcqFinalRepository.getAPData(startDate, endDate)
                .stream()
                .map(x -> APDTO.builder()
                        .dispatchInterval(x.getDispatchInterval())
                        .region(x.getRegion())
                        .flag(x.getFlag())
                        .build())
                .collect(Collectors.toList());
    }
}