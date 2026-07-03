package com.transport.tms.RoutePlanner.Repository;

import com.transport.tms.Config.SchemaConfig;

import com.transport.tms.RoutePlanner.Dto.StopProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class StopProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SchemaConfig schemas;

    public StopProductRepository(
            @Qualifier("sqlServerJdbcTemplate") JdbcTemplate jdbcTemplate,
            SchemaConfig schemas) {
        this.jdbcTemplate = jdbcTemplate;
        this.schemas = schemas;
    }

    // ── Single delivery (DROP) ─────────────────────────────────
    public List<StopProductDTO> findDeliveryLines(String docNum) {
        String sql = "SELECT * FROM " + schemas.getX3Schema() + ".XTMSDLVY_LINES_TMS WHERE DOCNUM = ? ORDER BY LINE_NUM";
        return jdbcTemplate.query(sql, (rs, n) -> mapRow(rs, "DROP"), docNum);
    }

    // ── Single pickup (PICKUP) ─────────────────────────────────
    public List<StopProductDTO> findPickupLines(String docNum) {
        String sql = "SELECT * FROM " + schemas.getX3Schema() + ".XTMSPICK_LINES_TMS WHERE DOCNUM = ? ORDER BY LINE_NUM";
        return jdbcTemplate.query(sql, (rs, n) -> mapRow(rs, "PICKUP"), docNum);
    }

    // ── Batch fetch for multiple delivery docs ─────────────────
    public List<StopProductDTO> findDeliveryLinesByDocs(List<String> docNums) {
        if (docNums == null || docNums.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(docNums.size(), "?"));
        String sql = "SELECT * FROM " + schemas.getX3Schema() + ".XTMSDLVY_LINES_TMS WHERE DOCNUM IN ("
                   + placeholders + ") ORDER BY DOCNUM, LINE_NUM";
        return jdbcTemplate.query(sql, (rs, n) -> mapRow(rs, "DROP"), docNums.toArray());
    }

    // ── Batch fetch for multiple pickup docs ───────────────────
    public List<StopProductDTO> findPickupLinesByDocs(List<String> docNums) {
        if (docNums == null || docNums.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(docNums.size(), "?"));
        String sql = "SELECT * FROM " + schemas.getX3Schema() + ".XTMSPICK_LINES_TMS WHERE DOCNUM IN ("
                   + placeholders + ") ORDER BY DOCNUM, LINE_NUM";
        return jdbcTemplate.query(sql, (rs, n) -> mapRow(rs, "PICKUP"), docNums.toArray());
    }

    // ── Row mapper — column names MUST match view aliases exactly ─
    private StopProductDTO mapRow(ResultSet rs, String stopType) throws SQLException {
        StopProductDTO dto = new StopProductDTO();
        dto.setStopType(stopType);
        dto.setDocNum(       rs.getString("DOCNUM"));
        dto.setLineNum(      rs.getObject("LINE_NUM",    Integer.class));
        dto.setCpyCode(      rs.getString("CPYCODE"));
        dto.setSite(         rs.getString("SITE"));
        dto.setItemCode(     rs.getString("ITEM_CODE"));
        dto.setItemDesc1(    rs.getString("ITEM_DESC1"));
        dto.setItemDesc2(    rs.getString("ITEM_DESC2"));
        dto.setQtyOrdered(   rs.getBigDecimal("QTY_ORDERED"));
        dto.setQtyDelivered( rs.getBigDecimal("QTY_DELIVERED"));
        dto.setStockUnit(    rs.getString("STOCK_UNIT"));
        dto.setPackUnit(     rs.getString("PACK_UNIT"));
        dto.setPackNum(      rs.getString("PACK_NUM"));
        dto.setNetWeight(    rs.getBigDecimal("NET_WEIGHT"));
        dto.setGrossWeight(  rs.getBigDecimal("GROSS_WEIGHT"));
        dto.setVolume(       rs.getBigDecimal("VOLUME"));
        dto.setWeightUnit(   rs.getString("WEIGHT_UNIT"));
        dto.setVolumeUnit(   rs.getString("VOLUME_UNIT"));
        dto.setLot(          rs.getString("LOT"));
        dto.setSerial(       rs.getString("SERIAL_NO"));   // view alias is SERIAL_NO
        dto.setLineStatus(   rs.getString("LINE_STATUS"));
        return dto;
    }
}
