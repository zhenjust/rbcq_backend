package com.pemc.crss.rbcq.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ASIEProjection {

    LocalDateTime getTimeInterval();

    String getRegionName();

    String getResourceName();

    String getCommodityType();

    BigDecimal getMwh();

    String getRemarks();

    LocalDateTime getCreatedDate();

    String getPublishedBy();
}