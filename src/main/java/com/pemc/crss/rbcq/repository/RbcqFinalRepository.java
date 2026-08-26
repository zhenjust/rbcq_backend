package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.FinalizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RbcqFinalRepository extends JpaRepository<FinalizeEntity,Long> {

    @Modifying
    @Query("DELETE FROM FinalizeEntity i WHERE i.timeInterval BETWEEN :startDateTime AND :endDateTime")
    void deleteByTimeIntervalRange(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);


    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO RBCQ_FINALIZE_TEST " +
                            "SELECT a.TIME_INTERVAL, a.REGION_NAME, a.RESOURCE_NAME, a.COMMODITY_TYPE, " +
                            "ROUND( " +
                            "  CASE " +
                            "    WHEN s.AS_SELL < ABS(s.AS_BUY) " +
                            "     AND a.SCHED_MW < a.OPRES_BCQ " +
                            "    THEN a.SCHED_MW + " +
                            "         (s.AS_SELL * (a.SCHED_MW - a.OPRES_BCQ) / s.AS_BUY) " +
                            "    ELSE a.OPRES_BCQ " +
                            "  END, 1 " +
                            ") AS OPRES_BCQ, " +
                            "SYSDATE AS PUBLISHED_DATE, " +
                            ":userId AS PUBLISHED_BY " +
                            "FROM ( " +
                            "  SELECT a.TIME_INTERVAL, a.REGION_NAME, a.RESOURCE_NAME, a.COMMODITY_TYPE, r.SCHED_MW, " +
                            "         CASE " +
                            "           WHEN r.SCHED_MW >= a.BCQ_MW THEN a.BCQ_MW " +
                            "           WHEN o.OPRESQ IS NULL THEN a.BCQ_MW " +
                            "           WHEN o.OPRESQ >= a.BCQ_MW THEN r.SCHED_MW " +
                            "           WHEN o.OPRESQ < a.BCQ_MW THEN (a.BCQ_MW - o.OPRESQ) + " +
                            "                CASE WHEN r.SCHED_MW < o.OPRESQ THEN r.SCHED_MW ELSE o.OPRESQ END " +
                            "           ELSE a.BCQ_MW " +
                            "         END AS OPRES_BCQ " +
                            "  FROM ( " +
                            "    SELECT TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, " +
                            "           CASE " +
                            "             WHEN COMMODITY_TYPE = 'REG' THEN 'RU' " +
                            "             WHEN COMMODITY_TYPE = 'CON' THEN 'FR' " +
                            "             WHEN COMMODITY_TYPE = 'DIS' THEN 'DR' " +
                            "             ELSE COMMODITY_TYPE " +
                            "           END AS COMMODITY_TYPE, " +
                            "           CASE WHEN COMMODITY_TYPE = 'REG' THEN ROUND(MW/2,1) ELSE MW END AS BCQ_MW " +
                            "    FROM RBCQ_INITIAL_TEST " +
                            "    WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to  " +
                            "    UNION ALL " +
                            "    SELECT TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, " +
                            "           'RD' AS COMMODITY_TYPE, ROUND(MW/2,1) AS BCQ_MW " +
                            "    FROM RBCQ_INITIAL_TEST " +
                            "    WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to  " +
                            "      AND COMMODITY_TYPE = 'REG' " +
                            "  ) a " +
                            "  INNER JOIN ( " +
                            "    SELECT TIME_INTERVAL, RESOURCE_NAME, UPPER(COMMODITY_TYPE) AS COMMODITY_TYPE, " +
                            "           ROUND(SCHED_MW_S,1) AS SCHED_MW " +
                            "    FROM EMM_HISV_OUTPUT_BID_RTD " +
                            "    WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to  " +
                            "      AND RESOURCE_TYPE != 'NL' AND COMMODITY_TYPE != 'En' " +
                            "  ) r " +
                            "    ON a.TIME_INTERVAL = r.TIME_INTERVAL " +
                            "   AND a.RESOURCE_NAME = r.RESOURCE_NAME " +
                            "   AND a.COMMODITY_TYPE = r.COMMODITY_TYPE " +
                            "  LEFT JOIN ( " +
                            "    SELECT TIME_INTERVAL, RESOURCE_NAME, UPPER(COMMODITY_TYPE) AS COMMODITY_TYPE, " +
                            "           QUANTITY2 AS OPRESQ " +
                            "    FROM OPRES_RTD_TEST " +
                            "    WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to  " +
                            "      AND COMMODITY_TYPE != 'En' AND PRICE2 = 0 " +
                            "  ) o " +
                            "    ON a.TIME_INTERVAL = o.TIME_INTERVAL " +
                            "   AND a.RESOURCE_NAME = o.RESOURCE_NAME " +
                            "   AND a.COMMODITY_TYPE = o.COMMODITY_TYPE " +
                            ") a " +
                            "INNER JOIN TEMP_RSPOTQ_AGG_TEST s " +
                            "  ON a.TIME_INTERVAL = s.TIME_INTERVAL " +
                            " AND a.REGION_NAME = s.REGION_NAME " +
                            " AND a.COMMODITY_TYPE = s.COMMODITY_TYPE",
            nativeQuery = true
    )
    int insertToRbcqFinal(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            @Param("userId") String userId
    );

    @Query(
            value = "SELECT COUNT(*) FROM RBCQ_INITIAL_TEST " +
                    "WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to",
            nativeQuery = true
    )
    long countRbcqInitial(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(
            value = "SELECT COUNT(*) FROM EMM_HISV_OUTPUT_BID_RTD " +
                    "WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to",
            nativeQuery = true
    )
    long countOutputBid(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(
            value = "SELECT COUNT(*) FROM OPRES_RTD_TEST " +
                    "WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to",
            nativeQuery = true
    )
    long countOpres(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(
            value = "SELECT COUNT(*) FROM TEMP_RSPOTQ_AGG_TEST " +
                    "WHERE TIME_INTERVAL >= :from AND TIME_INTERVAL <= :to",
            nativeQuery = true
    )
    long countTempAgg(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );




    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO RBCQ_FINALIZE_TEST ( " +
                            "    TIME_INTERVAL, " +
                            "    REGION_NAME, " +
                            "    RESOURCE_NAME, " +
                            "    COMMODITY_TYPE, " +
                            "    MW, " +
                            "    PUBLISHED_DATE, " +
                            "    PUBLISHED_BY " +
                            ") " +

                            "SELECT " +
                            "    a.TIME_INTERVAL, " +
                            "    a.REGION_NAME, " +
                            "    a.RESOURCE_NAME, " +
                            "    a.COMMODITY_TYPE, " +
                            "    a.MW, " +
                            "    SYSDATE, " +
                            "    :userId " +

                            "FROM ( " +

                            "    SELECT " +
                            "        TIME_INTERVAL, " +
                            "        REGION_NAME, " +
                            "        RESOURCE_NAME, " +
                            "        CASE " +
                            "            WHEN COMMODITY_TYPE = 'REG' THEN 'RU' " +
                            "            WHEN COMMODITY_TYPE = 'CON' THEN 'FR' " +
                            "            WHEN COMMODITY_TYPE = 'DIS' THEN 'DR' " +
                            "            ELSE COMMODITY_TYPE " +
                            "        END AS COMMODITY_TYPE, " +
                            "        CASE " +
                            "            WHEN COMMODITY_TYPE = 'REG' THEN ROUND(MW / 2, 1) " +
                            "            ELSE MW " +
                            "        END AS MW " +
                            "    FROM RBCQ_INITIAL_TEST " +
                            "    WHERE TIME_INTERVAL >= :from " +
                            "      AND TIME_INTERVAL <= :to " +
                            "      AND ( :region = 'ALL' OR REGION_NAME = :region ) " +

                            "    UNION ALL " +

                            "    SELECT " +
                            "        TIME_INTERVAL, " +
                            "        REGION_NAME, " +
                            "        RESOURCE_NAME, " +
                            "        'RD' AS COMMODITY_TYPE, " +
                            "        ROUND(MW / 2, 1) AS MW " +
                            "    FROM RBCQ_INITIAL_TEST " +
                            "    WHERE TIME_INTERVAL >= :from " +
                            "      AND TIME_INTERVAL <= :to " +
                            "      AND COMMODITY_TYPE = 'REG' " +
                            "      AND ( :region = 'ALL' OR REGION_NAME = :region ) " +

                            ") a",
            nativeQuery = true
    )
    int applyAPFlag(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("region") String region,
            @Param("userId") String userId
    );

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO CRSS_AP_FLAG_RTD_TEST " +
                    "(TIME_INTERVAL, REGION, FLAG, PUBLISHED_BY) " +
                    "SELECT DISTINCT TIME_INTERVAL, REGION_NAME, " +
                    "CASE " +
                    "    WHEN :isAP = 'N' THEN 'N' " +
                    "    WHEN :region = 'ALL' THEN 'Y' " +
                    "    WHEN REGION_NAME = :region THEN 'Y' " +
                    "    ELSE 'N' " +
                    "END, " +
                    " :userId " +
                    "FROM RBCQ_INITIAL_TEST " +
                    "WHERE TIME_INTERVAL >= :from " +
                    "AND TIME_INTERVAL <= :to",
            nativeQuery = true
    )
    int insertAPFlag(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("region") String region,
            @Param("userId") String userId,
            @Param("isAP") String isAP
    );
    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM CRSS_AP_FLAG_RTD_TEST " +
                    "WHERE TIME_INTERVAL BETWEEN :startDateTime AND :endDateTime",
            nativeQuery = true
    )
    void deleteByTimeIntervalRangeAP(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );




}
