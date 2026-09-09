package com.pemc.crss.rbcq.dto;

import java.time.LocalDateTime;

public interface APProjection {

    LocalDateTime getDispatchInterval();

    String getRegion();

    String getFlag();
}