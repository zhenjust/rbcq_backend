package com.pemc.crss.rbcq.entity;


import com.pemc.crss.rbcq.util.OutputBidRTDId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Table(
        name = "EMM_HISV_OUTPUT_BID_RTD",
        schema = "CRSS_TOD"
)
public class OutputBidRTDEntity {



    @EmbeddedId
    private OutputBidRTDId id;
    private String regionName;
    private String resourceName;
    private String commodityType;
    @Column(name = "SCHED_MW_S")
    private BigDecimal schedMw;

    public LocalDateTime getTimeInterval() {
        return id.getTimeInterval();
    }

    public double getSchedMwRounded() {
        return schedMw == null
                ? 0.0
                : schedMw.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    private String resourceType;
}
