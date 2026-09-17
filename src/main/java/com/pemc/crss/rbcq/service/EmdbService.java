package com.pemc.crss.rbcq.service;

import com.pemc.crss.rbcq.dto.ASIEProjection;
import com.pemc.crss.rbcq.repository.EMDBRepository;
import com.pemc.crss.rbcq.util.SecurityAuditorAware;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EmdbService {

    private final EMDBRepository emdbRepository;
    private final SecurityAuditorAware securityAuditorAware;

    ///*****************ASIE*****************///

    @Transactional
    public void ASIEreserve(LocalDateTime start, LocalDateTime end) {

        String linkedUserName = securityAuditorAware.getCurrentUsername()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        emdbRepository.deleteASIEreserve(start,end);

        int rowsInserted =
                emdbRepository.asie_reserve(
                        start,
                        end,
                        linkedUserName
                );

        log.info("GROUPED rows inserted = {}", rowsInserted);
    }

    @Transactional
    public void deleteASIEreserveAP(
            LocalDateTime start,
            LocalDateTime end
         ) {

        int rowsDeleted =
                emdbRepository.deleteASIEreserveWithAP(
                        start,
                        end
                );

        log.info("ASIE reserve rows deleted = {}", rowsDeleted);
    }

    @Transactional
    public void ASIEReserveAP(
            LocalDateTime start,
            LocalDateTime end
          ) {

        String linkedUserName = securityAuditorAware.getCurrentUsername()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));



        int rowsInserted =
                emdbRepository.ASIEreserveWithAP(
                        start,
                        end,
                        linkedUserName
                );


        log.info("ASIE reserve rows inserted = {}", rowsInserted);
    }


    public byte[] downloadASIEToCsv(
            LocalDateTime start,
            LocalDateTime end) {

        List<ASIEProjection> data =
                emdbRepository.getASIEreserveWithAP(start, end);

        StringBuilder csv = new StringBuilder();

        csv.append("TIME_INTERVAL,REGION_NAME,RESOURCE_NAME,COMMODITY_TYPE,MWH,REMARKS,CREATED_DATE,PUBLISHED_BY");
        csv.append("\n");

        for (ASIEProjection row : data) {

            csv.append(csv(row.getTimeInterval())).append(",");
            csv.append(csv(row.getRegionName())).append(",");
            csv.append(csv(row.getResourceName())).append(",");
            csv.append(csv(row.getCommodityType())).append(",");
            csv.append(csv(row.getMwh())).append(",");
            csv.append(csv(row.getRemarks())).append(",");
            csv.append(csv(row.getCreatedDate())).append(",");
            csv.append(csv(row.getPublishedBy()));
            csv.append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(Object value) {

        if (value == null) {
            return "";
        }

        String text = value.toString();

        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}