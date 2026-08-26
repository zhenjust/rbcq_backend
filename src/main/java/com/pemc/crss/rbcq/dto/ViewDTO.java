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
public class ViewDTO {

    LocalDateTime dispatch_interval;

    String region;

    String mtn;
    String category;

    String bcq;


}
