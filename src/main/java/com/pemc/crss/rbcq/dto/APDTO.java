package com.pemc.crss.rbcq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APDTO {

    private LocalDateTime dispatchInterval;
    private String region;
    private String flag;
}
