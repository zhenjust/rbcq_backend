package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.dto.RspotAggResult;
import com.pemc.crss.rbcq.entity.ReserveSpotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ReserveSpotRepository extends JpaRepository <ReserveSpotEntity,Long> {


    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM TEMP_RSPOTQ_AGG_TEST " +
                    "WHERE TIME_INTERVAL >= :from " +
                    "AND TIME_INTERVAL <= :to",
            nativeQuery = true
    )
    int deleteTempAggBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );




    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO TEMP_RSPOTQ_AGG_TEST " +
                            "SELECT TIME_INTERVAL, REGION_NAME, COMMODITY_TYPE, SUM(AS_BUY) AS_BUY, SUM(AS_SELL) AS_SELL " +
                            "FROM (SELECT TIME_INTERVAL, REGION_NAME, COMMODITY_TYPE, SCHED_MW, OPRES_BCQ, " +
                                "ROUND(CASE WHEN SCHED_MW < OPRES_BCQ THEN SCHED_MW - OPRES_BCQ ELSE 0 END, 1) AS_BUY, " +
                                "ROUND(CASE WHEN SCHED_MW > OPRES_BCQ THEN SCHED_MW - OPRES_BCQ ELSE 0 END, 1) AS_SELL " +
                                "FROM  (SELECT r.TIME_INTERVAL, " +
                            "    CASE WHEN r.REGION_NAME = 'CLUZ' THEN 'LUZON' WHEN r.REGION_NAME = 'CVIS' THEN 'VISAYAS' WHEN r.REGION_NAME = 'CMIN' THEN 'MINDANAO' ELSE 'NONE' END REGION_NAME,  " +
                            "    r.RESOURCE_NAME, r.COMMODITY_TYPE, r.SCHED_MW, " +
                            "    CASE WHEN b.BCQ_MW IS NULL THEN 0 " +
                            "        WHEN r.SCHED_MW >= b.BCQ_MW THEN b.BCQ_MW" +
                            "        WHEN o.OPRESQ IS NULL THEN b.BCQ_MW " +
                            "        WHEN o.OPRESQ >= b.BCQ_MW THEN r.SCHED_MW " +
                            "        WHEN o.OPRESQ < b.BCQ_MW THEN (b.BCQ_MW - o.OPRESQ) + (CASE WHEN r.SCHED_MW < o.OPRESQ THEN r.SCHED_MW ELSE o.OPRESQ END) " +
                            "        ELSE b.BCQ_MW END OPRES_BCQ " +
                            "     FROM (SELECT TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, UPPER(COMMODITY_TYPE) COMMODITY_TYPE, ROUND(SCHED_MW_S, 1) SCHED_MW FROM EMM_HISV_OUTPUT_BID_RTD " +
                            "       WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to AND RESOURCE_TYPE != 'NL' AND COMMODITY_TYPE != 'En') r " +

                            "     LEFT JOIN (SELECT TIME_INTERVAL, " +
                            "       REGION_NAME, " +
                            "       RESOURCE_NAME, " +
                            "       CASE WHEN COMMODITY_TYPE = 'REG' THEN 'RU' WHEN COMMODITY_TYPE = 'CON' THEN 'FR' WHEN COMMODITY_TYPE = 'DIS' THEN 'DR' ELSE COMMODITY_TYPE END COMMODITY_TYPE,  " +
                            "       CASE WHEN COMMODITY_TYPE = 'REG' THEN ROUND(MW/2, 1) ELSE MW END BCQ_MW " +
                            "       FROM RBCQ_INITIAL_TEST " +
                            "       WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to " +

                            "       UNION ALL " +

                            "       SELECT TIME_INTERVAL, " +
                            "       REGION_NAME, " +
                            "       RESOURCE_NAME, " +
                            "       'RD' COMMODITY_TYPE, " +
                            "       ROUND(MW/2, 1) BCQ_MW " +
                            "       FROM RBCQ_INITIAL_TEST " +
                            "       WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to " +
                            "       AND COMMODITY_TYPE = 'REG') b " +

                            "     ON r.TIME_INTERVAL = b.TIME_INTERVAL " +
                            "     AND r.RESOURCE_NAME =  b.RESOURCE_NAME " +
                            "     AND r.COMMODITY_TYPE = b.COMMODITY_TYPE " +

                            "     LEFT JOIN (SELECT TIME_INTERVAL, RESOURCE_NAME, UPPER(COMMODITY_TYPE) COMMODITY_TYPE, QUANTITY2 OPRESQ FROM OPRES_RTD_TEST" +
                            "                WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to AND COMMODITY_TYPE != 'En' AND PRICE2 = 0) o" +
                            "     ON r.TIME_INTERVAL = o.TIME_INTERVAL  " +
                            "     AND r.RESOURCE_NAME =  o.RESOURCE_NAME " +
                            "     AND r.COMMODITY_TYPE = o.COMMODITY_TYPE) a) " +



                            "GROUP BY TIME_INTERVAL, REGION_NAME, COMMODITY_TYPE",
            nativeQuery = true
    )
    int insertToRspotAgg(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );



}
