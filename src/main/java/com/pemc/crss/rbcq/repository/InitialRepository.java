package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.InitialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InitialRepository extends JpaRepository <InitialEntity,Long> {

    @Modifying
    @Query("DELETE FROM InitialEntity i WHERE i.timeInterval BETWEEN :startDateTime AND :endDateTime")
    void deleteByTimeIntervalRange(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    List<InitialEntity> findByTimeIntervalBetween(LocalDateTime start, LocalDateTime end);

    InitialEntity findFirstByTimeIntervalAndResourceName(
            LocalDateTime timeInterval,
            String resourceName
    );


    @Modifying
    @Transactional
    @Query(
            value =
                    "INSERT INTO RBCQ_INITIAL_TEST " +

                            "SELECT TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, COMMODITY_TYPE, " +
                            "ROUND(MW, 1) AS MW, " +
                            "SYSDATE AS PUBLISHED_DATE, " +
                            ":userId AS PUBLISHED_BY " +
                            "FROM RBCQ_AUDIT_TEST " +
                            "WHERE TIME_INTERVAL >= :from " +
                            "AND TIME_INTERVAL <= :to  " +
                            "AND FILE_DATE IN ( " +
                            "   SELECT MAX(FILE_DATE) FROM ( " +
                            "       SELECT TO_NUMBER(TO_CHAR(TIME_INTERVAL - 5/1440, 'YYYYMMDD')) AS TRADING_DAY, FILE_DATE " +
                            "       FROM RBCQ_AUDIT_TEST " +
                            "       WHERE TIME_INTERVAL >= :from " +
                            "       AND TIME_INTERVAL <= :to  " +
                            "       AND REGION_NAME = 'LUZON' " +
                            "   ) GROUP BY TRADING_DAY " +
                            ") " +
                            "AND REGION_NAME = 'LUZON' " +
                            "AND (COMMODITY_TYPE = 'REG' OR COMMODITY_TYPE = 'CON' OR COMMODITY_TYPE = 'DIS') " +

                            "UNION ALL " +

                            "SELECT TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, COMMODITY_TYPE, " +
                            "ROUND(MW, 1) AS MW, " +
                            "SYSDATE AS PUBLISHED_DATE, " +
                            ":userId AS PUBLISHED_BY " +
                            "FROM RBCQ_AUDIT_TEST " +
                            "WHERE TIME_INTERVAL >= :from " +
                            "AND TIME_INTERVAL <= :to  " +
                            "AND FILE_DATE IN ( " +
                            "   SELECT MAX(FILE_DATE) FROM ( " +
                            "       SELECT TO_NUMBER(TO_CHAR(TIME_INTERVAL - 5/1440, 'YYYYMMDD')) AS TRADING_DAY, FILE_DATE " +
                            "       FROM RBCQ_AUDIT_TEST " +
                            "       WHERE TIME_INTERVAL >= :from " +
                            "       AND TIME_INTERVAL <= :to " +
                            "       AND REGION_NAME = 'VISAYAS' " +
                            "   ) GROUP BY TRADING_DAY " +
                            ") " +
                            "AND REGION_NAME = 'VISAYAS' " +
                            "AND (COMMODITY_TYPE = 'REG' OR COMMODITY_TYPE = 'CON' OR COMMODITY_TYPE = 'DIS') " +

                            "UNION ALL " +

                            "SELECT TIME_INTERVAL, REGION_NAME, RESOURCE_NAME, COMMODITY_TYPE, " +
                            "ROUND(MW, 1) AS MW, " +
                            "SYSDATE AS PUBLISHED_DATE, " +
                            ":userId AS PUBLISHED_BY " +
                            "FROM RBCQ_AUDIT_TEST " +
                            "WHERE TIME_INTERVAL >= :from " +
                            "AND TIME_INTERVAL <= :to  " +
                            "AND FILE_DATE IN ( " +
                            "   SELECT MAX(FILE_DATE) FROM ( " +
                            "       SELECT TO_NUMBER(TO_CHAR(TIME_INTERVAL - 5/1440, 'YYYYMMDD')) AS TRADING_DAY, FILE_DATE " +
                            "       FROM RBCQ_AUDIT_TEST " +
                            "       WHERE TIME_INTERVAL >= :from " +
                            "       AND TIME_INTERVAL <= :to  " +
                            "       AND REGION_NAME = 'MINDANAO' " +
                            "   ) GROUP BY TRADING_DAY " +
                            ") " +
                            "AND REGION_NAME = 'MINDANAO' " +
                            "AND (COMMODITY_TYPE = 'REG' OR COMMODITY_TYPE = 'CON' OR COMMODITY_TYPE = 'DIS')",
            nativeQuery = true
    )
    int insertToRbcqInitial(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            @Param("userId") String userId
    );


}


