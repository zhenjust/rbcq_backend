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



    private BigDecimal calculateOpresBcq(BigDecimal schedMw, BigDecimal bcqMw, BigDecimal opresQ) {
        if (bcqMw == null) return BigDecimal.ZERO;
        if (schedMw.compareTo(bcqMw) >= 0) return bcqMw;
        if (opresQ == null) return bcqMw;
        if (opresQ.compareTo(bcqMw) >= 0) return schedMw;
        if (opresQ.compareTo(bcqMw) < 0) return bcqMw.subtract(opresQ).add(schedMw.min(opresQ));
        return bcqMw;
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
    public void processAP(LocalDateTime from, LocalDateTime to, String userId) {
        log.info("=== Start RBCQ Finalization ===");

        calculateAndSave(from,to);
        rbcqFinalRepository.deleteByTimeIntervalRange(from,to);

        int rowsInserted = rbcqFinalRepository.insertToRbcqFinal(from,to,userId);

        log.info("Rows inserted into RBCQ_FINALIZE_TEST: {}", rowsInserted);

        log.info("=== End RBCQ Finalization ===");
    }


    /* ============================================================
       HELPERS
       ============================================================ */

    private String key(LocalDateTime t, String r, String c) {
        return t + "|" + r + "|" + c;
    }
    private double computeOpresBcqSqlExact(
            double sched,
            InitialEntity b,
            OpresRTDEntity o
    ) {
        double bcq = (b == null) ? 0.0 : b.getMw();

        if (b == null) return 0.0;

        if (sched >= bcq) return bcq;

        if (o == null) return bcq;

        double opres = o.getQuantity2();

        if (opres >= bcq) return sched;

        if (opres < bcq) {
            return (bcq - opres) + Math.min(sched, opres);
        }

        return bcq;
    }


    private String normalizeRegion(String region) {
        switch (region) {
            case "CLUZ": return "LUZON";
            case "CVIS": return "VISAYAS";
            case "CMIN": return "MINDANAO";
            default: return "NONE";  // 🔹 changed from region → NONE
        }
    }

    private String normalizeCommodity(String c) {
        if (c == null) return "NONE"; // optional safety
        switch (c.toUpperCase()) {
            case "REG": return "RU";
            case "CON": return "FR";
            case "DIS": return "DR";
            default: return c.toUpperCase(); // matches SQL
        }
    }

    private double computeOpresBqc(double sched, InitialEntity b, OpresRTDEntity o) {
        double bcq = b.getMw();

        if (sched >= bcq) return bcq;
        if (o == null) return bcq;
        if (o.getQuantity2() >= bcq) return sched;

        return (bcq - o.getQuantity2()) + Math.min(sched, o.getQuantity2());
    }

    private double computeAsBuy(double sched, double opresBcq) {
        return round(sched < opresBcq ? sched - opresBcq : 0);
    }

    private double computeAsSell(double sched, double opresBcq) {
        return round(sched > opresBcq ? sched - opresBcq : 0);
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    public static double calculateFinalOpresBcq(
            double sched,
            double opresBcq,
            BigDecimal asBuy,
            BigDecimal asSell) {

        if (
                asSell.compareTo(asBuy.abs()) < 0 &&
                        sched < opresBcq &&
                        asBuy.compareTo(BigDecimal.ZERO) != 0
        ) {

            BigDecimal result =
                    BigDecimal.valueOf(sched)
                            .add(
                                    asSell.multiply(
                                            BigDecimal.valueOf(sched - opresBcq)
                                    ).divide(asBuy, 10, BigDecimal.ROUND_HALF_UP)
                            );

            return Math.round(result.doubleValue() * 10.0) / 10.0;
        }

        return Math.round(opresBcq * 10.0) / 10.0;
    }

}
