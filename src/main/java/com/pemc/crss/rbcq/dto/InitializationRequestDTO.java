package com.pemc.crss.rbcq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.time.LocalDate;

@Data
public class InitializationRequestDTO {

    private String processType;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDatetime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDatetime;
}
