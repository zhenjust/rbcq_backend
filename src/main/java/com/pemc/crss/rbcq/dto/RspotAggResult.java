package com.pemc.crss.rbcq.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RspotAggResult {


    private LocalDateTime timeInterval;
    private String region;
    private String commodityType;
    private BigDecimal asBuy;
    private BigDecimal asSell;

    public RspotAggResult() {
        this.asBuy = BigDecimal.ZERO;
        this.asSell = BigDecimal.ZERO;
    }

    // 🔥 REQUIRED by JPQL
    public RspotAggResult(
            LocalDateTime timeInterval,
            String region,
            String commodityType,
            BigDecimal asBuy,
            BigDecimal asSell
    ) {
        this.timeInterval = timeInterval;
        this.region = region;
        this.commodityType = commodityType;
        this.asBuy = asBuy;
        this.asSell = asSell;
    }


    public RspotAggResult merge(RspotAggResult other) {
        RspotAggResult merged = new RspotAggResult();

        merged.timeInterval = this.timeInterval != null ? this.timeInterval : other.timeInterval;
        merged.region = this.region != null ? this.region : other.region;
        merged.commodityType = this.commodityType != null ? this.commodityType : other.commodityType;

        merged.asBuy = this.asBuy.add(other.asBuy);
        merged.asSell = this.asSell.add(other.asSell);

        return merged;
    }

    public String groupKey() {
        return timeInterval + "|" + region + "|" + commodityType;
    }

    // getters & setters omitted for brevity
}
