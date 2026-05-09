package DAO;

import config.ConnectionOracle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DonHangDAO {
    
    public List<Object[]> selectAll() {
        List<Object[]> list = new ArrayList<>();
        // Trả về trực tiếp giá trị STATUS gốc (PENDING, CONFIRMED...)
        String sql = "SELECT o.ORDER_ID, c.NAME, o.CREATED_AT, o.TOTAL, o.STATUS, " +
                     "NVL(p.PAYMENT_STATUS, 'NONE') " +
                     "FROM ORDERS o " +
                     "JOIN CUSTOMER c ON o.CUSTOMER_ID = c.CUSTOMER_ID " +
                     "LEFT JOIN PAYMENT p ON o.ORDER_ID = p.ORDER_ID " +
                     "WHERE o.IS_DELETED = 0 ORDER BY o.CREATED_AT DESC";

        try (Connection conn = ConnectionOracle.getOracleConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    "ORD" + rs.getString(1), 
                    rs.getString(2),
                    rs.getDate(3),
                    rs.getDouble(4),
                    rs.getString(5), // Status gốc: PENDING, SHIPPING...
                    rs.getString(6)  // Payment Status: SUCCESS, FAILED...
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> selectByCondition(String keyword, String status, String payment) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT o.ORDER_ID, c.NAME, o.CREATED_AT, o.TOTAL, o.STATUS, " +
            "NVL(p.PAYMENT_STATUS, 'NONE') " +
            "FROM ORDERS o JOIN CUSTOMER c ON o.CUSTOMER_ID = c.CUSTOMER_ID " +
            "LEFT JOIN PAYMENT p ON o.ORDER_ID = p.ORDER_ID WHERE o.IS_DELETED = 0 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (LOWER(c.NAME) LIKE LOWER(?) OR TO_CHAR(o.ORDER_ID) LIKE ?) ");
        }
        if (!status.equals("All")) {
            sql.append("AND o.STATUS = ? ");
        }
        if (!payment.equals("All")) {
            sql.append("AND NVL(p.PAYMENT_STATUS, 'NONE') = ? ");
        }
        
        sql.append("ORDER BY o.CREATED_AT DESC");

        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            if (!status.equals("All")) ps.setString(idx++, status);
            if (!payment.equals("All")) ps.setString(idx++, payment);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{"ORD"+rs.getString(1), rs.getString(2), rs.getDate(3), rs.getDouble(4), rs.getString(5), rs.getString(6)});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    public boolean updateStatus(int orderId, String newStatus) {
    java.sql.Connection conn = null;
    try {
        conn = config.ConnectionOracle.getOracleConnection();
        
        // Tắt Auto-Commit để bắt đầu một Transaction
        conn.setAutoCommit(false); 

        // BƯỚC 1: Cập nhật trạng thái cho bảng ORDERS
        String sqlOrder = "UPDATE ORDERS SET STATUS = ? WHERE ORDER_ID = ?";
        try (java.sql.PreparedStatement ps1 = conn.prepareStatement(sqlOrder)) {
            ps1.setString(1, newStatus);
            ps1.setInt(2, orderId);
            ps1.executeUpdate();
        }

        // BƯỚC 2: Xử lý Logic Nghiệp vụ nếu trạng thái mới là CANCELLED (Hủy đơn)
        if ("CANCELLED".equals(newStatus)) {
            // Dùng TRIM() để gọt khoảng trắng thừa trong Database
            String sqlRefund = "UPDATE PAYMENT SET PAYMENT_STATUS = 'REFUNDED' WHERE ORDER_ID = ? AND TRIM(PAYMENT_STATUS) = 'SUCCESS'";
            try (java.sql.PreparedStatement ps2 = conn.prepareStatement(sqlRefund)) {
                ps2.setInt(1, orderId);
                ps2.executeUpdate();
            }
            
            // Dùng TRIM() để gọt khoảng trắng thừa trong Database
            String sqlFail = "UPDATE PAYMENT SET PAYMENT_STATUS = 'FAILED' WHERE ORDER_ID = ? AND TRIM(PAYMENT_STATUS) = 'PENDING'";
            try (java.sql.PreparedStatement ps3 = conn.prepareStatement(sqlFail)) {
                ps3.setInt(1, orderId);
                ps3.executeUpdate();
            }
        }

        // Nếu mọi lệnh SQL đều chạy trơn tru, ta COMMIT (lưu vĩnh viễn) xuống Oracle
        conn.commit(); 
        return true;

    } catch (Exception e) {
        // Cực kỳ quan trọng: Nếu có bất kỳ lỗi gì xảy ra, ROLLBACK toàn bộ để bảo toàn dữ liệu
        if (conn != null) {
            try { 
                conn.rollback(); 
                System.out.println("Đã Rollback Transaction vì có lỗi xảy ra!");
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        e.printStackTrace();
        return false;
        
    } finally {
        // Trả kết nối về trạng thái bình thường và đóng lại
        if (conn != null) {
            try { 
                conn.setAutoCommit(true); 
                conn.close(); 
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
}