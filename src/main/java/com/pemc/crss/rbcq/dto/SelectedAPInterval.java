package com.pemc.crss.rbcq.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class SelectedAPInterval {
    private LocalDateTime dispatchInterval;
    private String region;
    private String flag;
}
