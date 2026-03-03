package com.pemc.crss.rbcq.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class rbcqFinalDTO {
    private LocalDateTime timeInterval;
    private String regionName;
    private String resourceName;
    private String commodityType;
    private double opresBcq;
    private LocalDateTime publishedDate;
    private String publishedBy;
}
