package com.pemc.crss.rbcq.util;

import com.pemc.crss.rbcq.entity.AuditEntity;
import com.pemc.crss.rbcq.entity.InitialEntity;

import java.time.LocalDateTime;
import java.util.Date;

public class RbcqMapper {

    public static InitialEntity mapAuditToInitial(AuditEntity audit) {
        InitialEntity initial = new InitialEntity();

        initial.setTimeInterval(audit.getTimeInterval());
        initial.setRegionName(audit.getRegionName());
        initial.setResourceName(audit.getResourceName());
        initial.setCommodity(audit.getCommodity());
        initial.setMw(audit.getMw());
        initial.setPublishDate(LocalDateTime.now());
        initial.setPublishBy("USER_ID");

        return initial;
    }
}
