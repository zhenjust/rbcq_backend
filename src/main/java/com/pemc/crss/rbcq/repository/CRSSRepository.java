package com.pemc.crss.rbcq.repository;


import com.pemc.crss.rbcq.dto.ViewDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class CRSSRepository {

    private final JdbcTemplate settlementJdbcTemplate;
    private final JdbcTemplate registrationJdbcTemplate;


    private  ViewDTO viewDTO;

    public CRSSRepository(
            @Qualifier("settlementJdbcTemplate") JdbcTemplate settlementJdbcTemplate,
            @Qualifier("registrationJdbcTemplate") JdbcTemplate registrationJdbcTemplate
    ) {
        this.settlementJdbcTemplate = settlementJdbcTemplate;
        this.registrationJdbcTemplate = registrationJdbcTemplate;
    }

    // ===============================
    // SETTLEMENT DATABASE
    // ===============================

    public List<ViewDTO> getFinalData(Long linkedUserId, LocalDateTime startDate, LocalDateTime endDate) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean authorized = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("NGCP_SO_RBCQ"));

        String sql;
        List<Object> params = new ArrayList<>();

        if (authorized) {

            // Authorized: retrieve all data, no MTN filter
            sql = "SELECT dispatch_interval, region, mtn, category, bcq " +
                    "FROM nmms.txn_reserve_bcq " +
                    "WHERE dispatch_interval >= ? " +
                    "AND dispatch_interval <= ? " +
                    "ORDER BY dispatch_interval ASC";

            params.add(startDate);
            params.add(endDate);

        } else {

            // Unauthorized: retrieve only user's MTNs
            List<String> mtns = getMtns(linkedUserId, startDate, endDate);

            if (mtns.isEmpty()) {
                return Collections.emptyList();
            }

            String inSql = String.join(",", Collections.nCopies(mtns.size(), "?"));

            sql = "SELECT dispatch_interval, region, mtn, category, bcq " +
                    "FROM nmms.txn_reserve_bcq " +
                    "WHERE mtn IN (" + inSql + ") " +
                    "AND dispatch_interval >= ? " +
                    "AND dispatch_interval <= ? " +
                    "ORDER BY dispatch_interval ASC";

            params.addAll(mtns);
            params.add(startDate);
            params.add(endDate);
        }

        return settlementJdbcTemplate.query(
                sql,
                params.toArray(),
                (rs, rowNum) -> {
                    ViewDTO dto = new ViewDTO();
                    dto.setDispatch_interval(rs.getTimestamp("dispatch_interval").toLocalDateTime());
                    dto.setRegion(rs.getString("region"));
                    dto.setMtn(rs.getString("mtn"));
                    dto.setCategory(rs.getString("category"));
                    dto.setBcq(rs.getString("bcq"));
                    return dto;
                }
        );
    }


    // ===============================
    // REGISTRATION DATABASE
    // ===============================
    public List<String> getMtns(Long participantId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "select distinct b.name from registration.txn_metering_info a " +
        "inner join registration.txn_market_trading_node b on a.market_trading_node_id=b.id "+
        "inner join registration.txn_sein_participant_mapping c on c.facility_id=a.facility_id " +
        "inner join registration.txn_participant d on d.id = c.participant_id " +
        "inner join registration.user_applicant_link e on e.applicant_id = d.applicant_id " +
        "where e.linked_users  = ? and c.effective_start_date <= ? and (c.effective_end_date is null or c.effective_end_date >= ?); ";

        return registrationJdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("name"),
                participantId,
                startDate,
                endDate
        );
    }
}
