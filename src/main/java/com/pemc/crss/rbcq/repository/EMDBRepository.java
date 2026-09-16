package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.ASIncidentalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EMDBRepository extends JpaRepository<ASIncidentalEntity, Long> {

    // ASIE reserve delete
    @Modifying
    @Transactional
    @Query(
            value =
                    "DELETE FROM CRSS_TOD.TOCMS_AS_INCIDENTAL " +
                            "WHERE TIME_INTERVAL BETWEEN :startDateTime AND :endDateTime",
            nativeQuery = true
    )
    void deleteASIEreserve(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );


    // ASIE reserve query
    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO CRSS_TOD.TOCMS_AS_INCIDENTAL " +

                            "SELECT TIME_INTERVAL, " +
                            "       REGION_NAME, " +
                            "       RESOURCE_NAME, " +
                            "       COMMODITY_TYPE, " +
                            "       ROUND(ASIE_MW / 12, 11), " +
                            "       'Reserve Market' REMARKS, " +
                            "       SYSDATE PUBLISHED_DATE, " +
                            "       :userId PUBLISHED_BY " +

                            "FROM ( " +
                            "    SELECT TIME_INTERVAL, " +
                            "           CASE " +
                            "               WHEN REGION_NAME = 'CLUZ' THEN 'LUZON' " +
                            "               WHEN REGION_NAME = 'CVIS' THEN 'VISAYAS' " +
                            "               WHEN REGION_NAME = 'CMIN' THEN 'MINDANAO' " +
                            "               ELSE 'NONE' " +
                            "           END REGION_NAME, " +
                            "           RESOURCE_NAME, " +
                            "           COMMODITY_TYPE, " +
                            "           CASE " +
                            "               WHEN LOWER_LIMIT > MQ_MW THEN 0 " +
                            "               WHEN MQ_MW > UPPER_LIMIT THEN (AS_RAISE + AS_LOWER) " +
                            "               ELSE MQ_MW - LOWER_LIMIT " +
                            "           END ASIE_MW " +

                            "    FROM ( " +
                            "        SELECT a.TIME_INTERVAL, " +
                            "               a.REGION_NAME, " +
                            "               a.RESOURCE_NAME, " +
                            "               a.COMMODITY_TYPE, " +
                            "               a.EN, " +
                            "               a.AS_RAISE, " +
                            "               a.AS_LOWER, " +
                            "               m.MQ_MW, " +
                            "               m.MQ_MWH, " +
                            "               CASE " +
                            "                   WHEN a.EN < a.AS_LOWER THEN 0 " +
                            "                   ELSE (a.EN - a.AS_LOWER) " +
                            "               END LOWER_LIMIT, " +
                            "               (a.EN + a.AS_RAISE) UPPER_LIMIT " +

                            "        FROM ( " +
                            "            SELECT a.TIME_INTERVAL, " +
                            "                   a.REGION_NAME, " +
                            "                   a.RESOURCE_NAME, " +
                            "                   e.EN, " +
                            "                   CASE " +
                            "                       WHEN l.AS_LOWER IS NOT NULL THEN 'REG' " +
                            "                       WHEN r.COMMODITY_TYPE = 'Fr' THEN 'CON' " +
                            "                       WHEN r.COMMODITY_TYPE = 'Dr' THEN 'DIS' " +
                            "                       ELSE 'NONE' " +
                            "                   END COMMODITY_TYPE, " +
                            "                   CASE " +
                            "                       WHEN r.AS_RAISE IS NULL THEN 0 " +
                            "                       ELSE r.AS_RAISE " +
                            "                   END AS_RAISE, " +
                            "                   CASE " +
                            "                       WHEN l.AS_LOWER IS NULL THEN 0 " +
                            "                       ELSE l.AS_LOWER " +
                            "                   END AS_LOWER " +

                            "            FROM ( " +
                            "                SELECT DISTINCT TIME_INTERVAL, " +
                            "                                REGION_NAME, " +
                            "                                RESOURCE_NAME " +
                            "                FROM EMDB.EMM_HISV_OUTPUT_BID_RTD " +
                            "                WHERE TIME_INTERVAL >= :from " +
                            "                AND TIME_INTERVAL <= :to " +
                            "                AND COMMODITY_TYPE != 'En' " +
                            "                AND RESOURCE_TYPE != 'NL' " +
                            "                AND SCHED_MW_S > 0 " +
                            "            ) a " +

                            "            INNER JOIN ( " +
                            "                SELECT TIME_INTERVAL, " +
                            "                       RESOURCE_NAME, " +
                            "                       SCHED_MW EN " +
                            "                FROM EMDB.EMM_HISV_OUTPUT_PNODE_LMP_RTD " +
                            "                WHERE TIME_INTERVAL >= :from " +
                            "                AND TIME_INTERVAL <= :to " +
                            "                AND RESOURCE_TYPE != 'NL' " +
                            "            ) e " +

                            "            ON a.TIME_INTERVAL = e.TIME_INTERVAL " +
                            "            AND a.RESOURCE_NAME = e.RESOURCE_NAME " +

                            "            LEFT JOIN ( " +
                            "                SELECT TIME_INTERVAL, " +
                            "                       RESOURCE_NAME, " +
                            "                       SUM(SCHED_MW_S) AS_RAISE, " +
                            "                       MAX(COMMODITY_TYPE) COMMODITY_TYPE " +
                            "                FROM EMDB.EMM_HISV_OUTPUT_BID_RTD " +
                            "                WHERE TIME_INTERVAL >= :from " +
                            "                AND TIME_INTERVAL <= :to " +
                            "                AND RESOURCE_TYPE != 'NL' " +
                            "                AND COMMODITY_TYPE != 'En' " +
                            "                AND COMMODITY_TYPE != 'Rd' " +
                            "                AND SCHED_MW_S > 0 " +
                            "                GROUP BY TIME_INTERVAL, RESOURCE_NAME " +
                            "            ) r " +

                            "            ON a.TIME_INTERVAL = r.TIME_INTERVAL " +
                            "            AND a.RESOURCE_NAME = r.RESOURCE_NAME " +

                            "            LEFT JOIN ( " +
                            "                SELECT TIME_INTERVAL, " +
                            "                       RESOURCE_NAME, " +
                            "                       SCHED_MW_S AS_LOWER " +
                            "                FROM EMDB.EMM_HISV_OUTPUT_BID_RTD " +
                            "                WHERE TIME_INTERVAL >= :from " +
                            "                AND TIME_INTERVAL <= :to " +
                            "                AND RESOURCE_TYPE != 'NL' " +
                            "                AND COMMODITY_TYPE = 'Rd' " +
                            "                AND SCHED_MW_S > 0 " +
                            "            ) l " +

                            "            ON a.TIME_INTERVAL = l.TIME_INTERVAL " +
                            "            AND a.RESOURCE_NAME = l.RESOURCE_NAME " +

                            "        ) a " +

                            "        INNER JOIN ( " +
                            "            SELECT TIME_INTERVAL, " +
                            "                   RESOURCE_NAME, " +
                            "                   MWH * 12 MQ_MW, " +
                            "                   MWH MQ_MWH " +
                            "            FROM TO_PROCESSED_MQ " +
                            "            WHERE TIME_INTERVAL >= :from " +
                            "            AND TIME_INTERVAL <= :to " +
                            "        ) m " +

                            "        ON a.TIME_INTERVAL = m.TIME_INTERVAL " +
                            "        AND a.RESOURCE_NAME = m.RESOURCE_NAME " +

                            "    ) a " +

                            ")",
            nativeQuery = true
    )
    int asie_reserve(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("userId") String userId
    );

    @Modifying
    @Transactional
    @Query(
            value =
                    "DELETE FROM CRSS_TOD.TOCMS_AS_INCIDENTAL tai " +

                            "WHERE tai.TIME_INTERVAL >= :startDateTime " +
                            "AND tai.TIME_INTERVAL <= :endDateTime " +

                            "AND EXISTS ( " +
                            "    SELECT 1 " +
                            "    FROM CRSS_AP_FLAG_RTD_TEST ap " +
                            "    WHERE tai.TIME_INTERVAL = ap.TIME_INTERVAL " +
                            "    AND tai.REGION_NAME = ap.REGION " +
                            "    AND ap.FLAG = 'Y' " +
                            "    AND ap.TIME_INTERVAL >= :startDateTime " +
                            "    AND ap.TIME_INTERVAL <= :endDateTime " +
                            ") " +

                            "AND NOT ( " +
                            "    (tai.TIME_INTERVAL, tai.REGION_NAME) IN ( " +
                            "        SELECT ap2.TIME_INTERVAL, ap2.REGION " +
                            "        FROM CRSS_AP_FLAG_RTD_TEST ap2 " +
                            "        WHERE ap2.FLAG = 'Y' " +
                            "        AND ap2.TIME_INTERVAL IN (:selectedIntervals) " +
                            "        AND ap2.REGION IN (:selectedRegions) " +
                            "    ) " +
                            ")",
            nativeQuery = true
    )
    int deleteASIEreserveWithAP(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("selectedIntervals") List<LocalDateTime> selectedIntervals,
            @Param("selectedRegions") List<String> selectedRegions
    );

    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO CRSS_TOD.TOCMS_AS_INCIDENTAL " +
                            "    (TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, COMMODITY_TYPE, " +
                            "     ASIE_MWH, REMARKS, CREATED_DATE, PUBLISHED_BY) " +

                            "WITH MQ_MWH AS (" +
                            "    SELECT a.TIME_INTERVAL, b.REGION, b.PRICING_FLAG, " +
                            "           a.RESOURCE_NAME, a.MWH " +
                            "    FROM CRSS_TOD.TO_PROCESSED_MQ a " +
                            "    LEFT JOIN TOCMS_DIPC_FINAL b " +
                            "        ON a.TIME_INTERVAL = b.INTERVAL_DATETIME " +
                            "        AND a.RESOURCE_NAME = b.RESOURCE_MTN " +
                            "), " +

                            "DAAS_SCHED AS (" +
                            "    SELECT DISTINCT TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, " +
                            "           UNIT_ID, RESERVE_TYPE, CONTROL_MODE, MW/12 DAAS_MWH " +
                            "    FROM CRSS_TOD.TO_SO_DAAS" +
                            ") " +

                            "SELECT a.TIME_INTERVAL, " +
                            "       a.REGION REGION_NAME, " +
                            "       a.RESOURCE_NAME, " +
                            "       b.RESERVE_TYPE COMMODITY_TYPE, " +
                            "       ROUND(CASE " +
                            "           WHEN a.MWH = 0 THEN 0 " +
                            "           WHEN b.DAAS_MWH < a.MWH THEN b.DAAS_MWH " +
                            "           ELSE a.MWH " +
                            "       END, 11) AS ASIE_MWH, " +
                            "       'Reserve Market AP' AS REMARKS, " +
                            "       SYSDATE AS CREATED_DATE, " +
                            "       :publishedBy AS PUBLISHED_BY " +

                            "FROM MQ_MWH a " +
                            "INNER JOIN DAAS_SCHED b " +
                            "    ON b.TIME_INTERVAL = CASE " +
                            "        WHEN a.TIME_INTERVAL = TRUNC(a.TIME_INTERVAL, 'HH24') " +
                            "        THEN a.TIME_INTERVAL " +
                            "        ELSE TRUNC(a.TIME_INTERVAL, 'HH24') + INTERVAL '1' HOUR " +
                            "    END " +
                            "    AND a.RESOURCE_NAME = b.RESOURCE_NAME " +

                            "WHERE a.TIME_INTERVAL >= :startDateTime " +
                            "AND a.TIME_INTERVAL <= :endDateTime " +
                            "AND a.TIME_INTERVAL IN (:selectedIntervals) " +
                            "AND a.REGION IN (:selectedRegions) " +
                            "AND a.PRICING_FLAG = 'AP' " +

                            "ORDER BY a.TIME_INTERVAL",
            nativeQuery = true
    )
    int ASIEreserveWithAP(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("selectedIntervals") List<LocalDateTime> selectedIntervals,
            @Param("selectedRegions") List<String> selectedRegions,
            @Param("publishedBy") String publishedBy
    );
}