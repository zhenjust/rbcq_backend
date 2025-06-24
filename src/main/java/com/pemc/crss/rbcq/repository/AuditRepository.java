package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.AuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface AuditRepository extends JpaRepository<AuditEntity,Long> {
    boolean existsByTimeIntervalAndResourceNameAndCommodityAndFileName(
            LocalDateTime timeInterval,
            String resourceName,
            String commodity,
            String fileName
    );
}
