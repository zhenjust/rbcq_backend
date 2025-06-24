package com.pemc.crss.rbcq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(
        name = "RBCQ_AUDIT_TEST",
        schema = "CRSS_TOD",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_audit_unique_fields",
                        columnNames = { "TIME_INTERVAL", "RESOURCE_NAME", "COMMODITY", "FILE_NAME" }
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq")
    @SequenceGenerator(
            name = "audit_seq",
            sequenceName = "CRSS_TOD.AUDIT_RBCQ_SEQ", // use schema prefix
            allocationSize = 1
    )
    private Long id;

    @Column(name = "TIME_INTERVAL")
    private LocalDateTime timeInterval;

    @Column(name = "REGION_NAME")
    private String regionName;

    @Column(name = "RESOURCE_NAME")
    private String resourceName;

    @Column(name = "COMMODITY")
    private String commodity;

    @Column(name = "MW")
    private BigDecimal mw;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_DATE")
    private LocalDateTime fileDate;

    @Column(name = "PUBLISH_DATE")
    private LocalDateTime publishDate;

    @Column(name = "PUBLISH_BY")
    private String publishBy;
}
