package com.transport.tms.Reports.Repository;

import com.transport.tms.Reports.Dto.VehicleReportDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VehicleReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public VehicleReportRepository(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // First "?" = total days in the requested period (divisor for utilization),
    // next two "?" = doc_date BETWEEN start AND end, optional trailing "?" = site.
    private static final String BASE_QUERY = """
            SELECT
                vehicle_code AS plate,
                COUNT(*) AS trips,
                COALESCE(SUM(NULLIF(TRIM(total_distance), '')::NUMERIC), 0) AS distance,
                ROUND((COUNT(DISTINCT doc_date)::NUMERIC / ?) * 100, 2) AS utilization
            FROM tms.xr_trip
            WHERE opti_status = 'Validated'
              AND doc_date BETWEEN ? AND ?
              %s
            GROUP BY vehicle_code
            """;

    public List<VehicleReportDTO> findVehicleReport(LocalDate startDate, LocalDate endDate, String site) {
        boolean hasSite = StringUtils.hasText(site);
        String sql = String.format(BASE_QUERY, hasSite ? "AND site = ?" : "");

        long totalDaysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        List<Object> params = new ArrayList<>();
        params.add(totalDaysInPeriod);
        params.add(startDate);
        params.add(endDate);
        if (hasSite) {
            params.add(site);
        }

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> VehicleReportDTO.builder()
                .plate(rs.getString("plate"))
                .trips(rs.getLong("trips"))
                .distance(rs.getDouble("distance"))
                .utilization(rs.getDouble("utilization"))
                .build());
    }
}