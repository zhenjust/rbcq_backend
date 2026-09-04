package com.pemc.crss.rbcq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RequestDTO {

    private String processType;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDatetime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDatetime;

    private String UserId;

    private List<String> regions;
}
