package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.AuditEntity;
import com.pemc.crss.rbcq.entity.InitialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface AuditRepository extends JpaRepository<AuditEntity,Long> {
    boolean existsByTimeIntervalAndResourceNameAndCommodityAndFileName(
            LocalDateTime timeInterval,
            String resourceName,
            String commodity,
            String fileName
    );

    @Query(value = "SELECT * FROM CRSS_TOD.RBCQ_AUDIT_TEST a " +
            "WHERE a.time_interval BETWEEN :start AND :end " +
            "AND a.region_name = :region " +
            "AND a.file_date IN ( " +
            "  SELECT MAX(a2.file_date) " +
            "  FROM CRSS_TOD.RBCQ_AUDIT_TEST a2 " +
            "  WHERE a2.region_name = :region " +
            "  AND a2.time_interval BETWEEN :start AND :end " +
            "  GROUP BY TO_NUMBER(TO_CHAR(a2.time_interval - INTERVAL '5' MINUTE, 'YYYYMMDD')) " +
            ") " +
            "AND a.commodity IN ('REG', 'CON', 'DIS')",
            nativeQuery = true)
    List<AuditEntity> findLatestEntriesForRegion(
            @Param("region") String region,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );







}
