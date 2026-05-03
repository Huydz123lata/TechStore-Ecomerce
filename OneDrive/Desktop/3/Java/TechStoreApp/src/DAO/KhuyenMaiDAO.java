package DAO;

import config.ConnectionOracle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    // Hàm tự động tính trạng thái theo thời gian thực
    private String getStatusLogicSQL() {
        return "CASE " +
               "  WHEN IS_ACTIVE = 0 THEN 'CANCELLED' " +
               "  WHEN END_AT < TRUNC(SYSDATE) THEN 'EXPIRED' " +
               "  WHEN START_AT > TRUNC(SYSDATE) THEN 'UPCOMING' " +
               "  ELSE 'ACTIVE' " +
               "END";
    }

    // ==============================================================
    // HÀM MỚI: Tự động gắn hậu tố % hoặc VNĐ, và phẩy hàng nghìn
    // ==============================================================
    private String formatDiscountValue(double value, String type) {
        // "#,###.##" giúp bỏ số 0 vô nghĩa ở đuôi, thêm dấu phẩy hàng nghìn
        DecimalFormat formatter = new DecimalFormat("#,###.##");
        if ("PERCENT".equals(type) || "PERCENTAGE".equals(type)) {
            return formatter.format(value) + " %";
        } else {
            return formatter.format(value) + " VNĐ";
        }
    }

    public List<Object[]> selectAll() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT CODE, DESCRIPTION, DISCOUNT_TYPE, DISCOUNT_VALUE, START_AT, END_AT, " +
                     getStatusLogicSQL() + " AS CURRENT_STATUS " +
                     "FROM COUPON WHERE IS_DELETED = 0 ORDER BY START_AT DESC";

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                String type = rs.getString("DISCOUNT_TYPE");
                if ("PERCENTAGE".equals(type)) type = "PERCENT";

                // ÉP FORMAT DỮ LIỆU Ở ĐÂY TRƯỚC KHI ĐẨY LÊN BẢNG
                String formattedValue = formatDiscountValue(rs.getDouble("DISCOUNT_VALUE"), type);

                list.add(new Object[]{
                    rs.getString("CODE"),
                    rs.getString("DESCRIPTION"),
                    type,
                    formattedValue, // <--- Dữ liệu đã được format đẹp đẽ
                    rs.getDate("START_AT"),
                    rs.getDate("END_AT"),
                    rs.getString("CURRENT_STATUS")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> selectByCondition(String keyword, String status) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT CODE, DESCRIPTION, DISCOUNT_TYPE, DISCOUNT_VALUE, START_AT, END_AT, " +
            getStatusLogicSQL() + " AS CURRENT_STATUS " +
            "FROM COUPON WHERE IS_DELETED = 0 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (LOWER(CODE) LIKE LOWER(?) OR LOWER(DESCRIPTION) LIKE LOWER(?)) ");
        }
        if (!status.equals("All")) {
            sql.append("AND (").append(getStatusLogicSQL()).append(") = ? ");
        }
        sql.append("ORDER BY START_AT DESC");

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
                    String type = rs.getString("DISCOUNT_TYPE");
                    if ("PERCENTAGE".equals(type)) type = "PERCENT";

                    // ÉP FORMAT DỮ LIỆU Ở ĐÂY CHO PHẦN TÌM KIẾM
                    String formattedValue = formatDiscountValue(rs.getDouble("DISCOUNT_VALUE"), type);

                    list.add(new Object[]{
                        rs.getString("CODE"), rs.getString("DESCRIPTION"), type, 
                        formattedValue, // <--- Dữ liệu đã được format đẹp đẽ
                        rs.getDate("START_AT"), rs.getDate("END_AT"), 
                        rs.getString("CURRENT_STATUS")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(String promoCode, String newStatus) {
        int isActive = "CANCELLED".equals(newStatus) ? 0 : 1;
        String sql = "UPDATE COUPON SET IS_ACTIVE = ? WHERE TRIM(CODE) = ?";
        
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, isActive);
            ps.setString(2, promoCode.trim());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public void insertPromotion(String code, String name, String type, double value, java.util.Date startDate, java.util.Date endDate, String status) throws Exception {
        String dbType = "PERCENT".equals(type) ? "PERCENTAGE" : "AMOUNT";
        
        String sql = "INSERT INTO COUPON (CODE, DESCRIPTION, DISCOUNT_TYPE, DISCOUNT_VALUE, START_AT, END_AT, IS_ACTIVE, IS_DELETED) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 1, 0)";
                     
        try (Connection conn = config.ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name); 
            ps.setString(3, dbType);
            ps.setDouble(4, value);
            ps.setDate(5, new java.sql.Date(startDate.getTime()));
            ps.setDate(6, new java.sql.Date(endDate.getTime()));
            ps.executeUpdate();
        }
    }
}