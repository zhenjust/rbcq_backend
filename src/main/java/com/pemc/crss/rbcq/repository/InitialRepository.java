package com.pemc.crss.rbcq.repository;

import com.pemc.crss.rbcq.entity.InitialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InitialRepository extends JpaRepository <InitialEntity,Long> {

    @Modifying
    @Query("DELETE FROM InitialEntity i WHERE i.timeInterval BETWEEN :startDateTime AND :endDateTime")
    void deleteByTimeIntervalRange(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);


}
