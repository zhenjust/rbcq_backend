package com.pemc.crss.rbcq.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDTO {

    private LocalDateTime time_interval;
    private String region_name;
    private String resource_name;
    private String commodity_type;
    private BigDecimal mw;
    private String file_name;
    private LocalDateTime file_date;
    private LocalDateTime publish_date;
    private String published_by;
}
