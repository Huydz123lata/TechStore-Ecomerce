package DAO;

import config.ConnectionOracle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    public List<Object[]> selectAll() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT PROMO_CODE, PROMO_NAME, DISCOUNT_TYPE, DISCOUNT_VALUE, START_DATE, END_DATE, STATUS " +
                     "FROM PROMOTION ORDER BY START_DATE DESC";

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3), 
                    rs.getString(4), rs.getDate(5), rs.getDate(6), rs.getString(7)
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> selectByCondition(String keyword, String status) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT PROMO_CODE, PROMO_NAME, DISCOUNT_TYPE, DISCOUNT_VALUE, START_DATE, END_DATE, STATUS FROM PROMOTION WHERE 1=1 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (LOWER(PROMO_CODE) LIKE LOWER(?) OR LOWER(PROMO_NAME) LIKE LOWER(?)) ");
        }
        if (!status.equals("All")) {
            sql.append("AND STATUS = ? ");
        }
        sql.append("ORDER BY START_DATE DESC");

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            if (!status.equals("All")) {
                ps.setString(idx++, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3), 
                        rs.getString(4), rs.getDate(5), rs.getDate(6), rs.getString(7)
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(String promoCode, String newStatus) {
        String sql = "UPDATE PROMOTION SET STATUS = ? WHERE TRIM(PROMO_CODE) = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, promoCode.trim());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public void insertPromotion(String code, String name, String type, double value, java.util.Date startDate, java.util.Date endDate, String status) throws Exception {
        String sql = "INSERT INTO PROMOTION (PROMO_CODE, PROMO_NAME, DISCOUNT_TYPE, DISCOUNT_VALUE, START_DATE, END_DATE, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setDouble(4, value);
            ps.setDate(5, new java.sql.Date(startDate.getTime()));
            ps.setDate(6, new java.sql.Date(endDate.getTime()));
            ps.setString(7, status);
            ps.executeUpdate();
        }
    }
}