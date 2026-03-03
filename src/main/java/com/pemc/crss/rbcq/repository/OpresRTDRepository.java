package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.AuditEntity;
import com.pemc.crss.rbcq.entity.OpresRTDEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OpresRTDRepository  extends JpaRepository <OpresRTDEntity, Long> {

    List<OpresRTDEntity> findByTimeIntervalBetweenAndCommodityTypeNotAndPrice2(
            LocalDateTime start, LocalDateTime end, String commodityType, Double price2
    );



    @Query(value = "SELECT * " +
            " FROM OPRES_RTD_TEST o " +
            "WHERE o.TIME_INTERVAL BETWEEN :start AND :end " +
            "AND o.COMMODITY_TYPE <> 'En'" +
            "AND o.PRICE2 = 0"
            ,
            nativeQuery = true)
    List<OpresRTDEntity> findByTimeIntervalBetween(

            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
