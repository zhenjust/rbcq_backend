package com.pemc.crss.rbcq.util;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
@Data
public class OutputBidRTDId implements Serializable {

    @Column(name = "TIME_INTERVAL")
    private LocalDateTime timeInterval;


}
