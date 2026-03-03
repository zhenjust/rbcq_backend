package com.pemc.crss.rbcq.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OPRES_RTD_TEST",schema = "CRSS_TOD")
public class OpresRTDEntity {

    @Id
    private LocalDateTime timeInterval;
    private String resourceName;
    private String commodityType;
    private Double quantity2;
    private Double price2;

    private String regionName;
}
