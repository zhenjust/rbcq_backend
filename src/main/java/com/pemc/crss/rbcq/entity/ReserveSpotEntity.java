package com.pemc.crss.rbcq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "TEMP_RSPOTQ_AGG_TEST",
        schema = "CRSS_TOD"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveSpotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq")
    @SequenceGenerator(
            name = "audit_seq",
            sequenceName = "CRSS_TOD.AUDIT_RBCQ_SEQ", // use schema prefix
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "TIME_INTERVAL")
    private LocalDateTime timeInterval;
    @Column(name = "REGION_NAME")
    private String regionName;

    @Column(name = "COMMODITY_TYPE")
    private String commodity;
    @Column(name = "AS_BUY")
    private BigDecimal asBuy;

    @Column(name = "AS_SELL")
    private BigDecimal asSell;

}
