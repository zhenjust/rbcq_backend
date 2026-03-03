package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.OutputBidRTDEntity;
import org.apache.tomcat.jni.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutputBidRTDRepository extends JpaRepository<OutputBidRTDEntity,Long> {

    List<OutputBidRTDEntity> findById_TimeIntervalBetweenAndCommodityTypeNotIgnoreCaseAndResourceTypeNot(
            LocalDateTime start, LocalDateTime end, String commodityType, String resourceType
    );


}
