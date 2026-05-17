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
        String sql = "SELECT o.ORDER_ID, u.FULL_NAME, o.CREATED_AT, o.TOTAL, o.STATUS, "
                + "NVL(p.PAYMENT_STATUS, 'PENDING') "
                + "FROM ORDERS o "
                + "JOIN APP_USER u ON o.USER_ID = u.USER_ID "
                + "LEFT JOIN PAYMENT p ON o.ORDER_ID = p.ORDER_ID "
                + "WHERE o.IS_DELETED = 0 ORDER BY o.CREATED_AT DESC";

        try (Connection conn = ConnectionOracle.getOracleConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    "ORD" + rs.getString(1),
                    rs.getString(2),
                    rs.getDate(3),
                    rs.getDouble(4),
                    rs.getString(5), 
                    rs.getString(6) 
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
                "SELECT o.ORDER_ID, u.FULL_NAME, o.CREATED_AT, o.TOTAL, o.STATUS, "
                + "NVL(p.PAYMENT_STATUS, 'PENDING') "
                + "FROM ORDERS o "
                + "JOIN APP_USER u ON o.USER_ID = u.USER_ID "
                + "LEFT JOIN PAYMENT p ON o.ORDER_ID = p.ORDER_ID "
                + "WHERE o.IS_DELETED = 0 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (LOWER(u.FULL_NAME) LIKE LOWER(?) OR TO_CHAR(o.ORDER_ID) LIKE ?) ");
        }
        if (!status.equals("All")) {
            sql.append("AND o.STATUS = ? ");
        }
        if (!payment.equals("All")) {
            sql.append("AND NVL(p.PAYMENT_STATUS, 'PENDING') = ? ");
        }

        sql.append("ORDER BY o.CREATED_AT DESC");

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
            if (!payment.equals("All")) {
                ps.setString(idx++, payment);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    "ORD" + rs.getString(1), 
                    rs.getString(2), 
                    rs.getDate(3), 
                    rs.getDouble(4), 
                    rs.getString(5), 
                    rs.getString(6)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int orderId, String newStatus) {
        Connection conn = null;
        try {
            conn = ConnectionOracle.getOracleConnection();
            conn.setAutoCommit(false);

            String sqlOrder = "UPDATE ORDERS SET STATUS = ? WHERE ORDER_ID = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sqlOrder)) {
                ps1.setString(1, newStatus);
                ps1.setInt(2, orderId);
                ps1.executeUpdate();
            }

            if ("CANCELLED".equals(newStatus)) {
                String sqlRefund = "UPDATE PAYMENT SET PAYMENT_STATUS = 'REFUNDED' WHERE ORDER_ID = ? AND PAYMENT_STATUS = 'SUCCESS'";
                try (PreparedStatement ps2 = conn.prepareStatement(sqlRefund)) {
                    ps2.setInt(1, orderId);
                    ps2.executeUpdate();
                }

                String sqlFail = "UPDATE PAYMENT SET PAYMENT_STATUS = 'FAILED' WHERE ORDER_ID = ? AND PAYMENT_STATUS = 'PENDING'";
                try (PreparedStatement ps3 = conn.prepareStatement(sqlFail)) {
                    ps3.setInt(1, orderId);
                    ps3.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    // Lấy thông tin khách hàng và tổng tiền của 1 đơn hàng cụ thể
    public Object[] getOrderInfoForDialog(int orderId) {
        Object[] info = null;
        // Lấy Email từ APP_USER, các thông tin giao hàng lấy từ bảng ORDERS
        String sql = "SELECT o.RECEIVER_NAME, u.EMAIL, o.RECEIVER_PHONE, o.SHIPPING_ADDRESS, o.TOTAL " +
                     "FROM ORDERS o JOIN APP_USER u ON o.USER_ID = u.USER_ID " +
                     "WHERE o.ORDER_ID = ?";
                     
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                info = new Object[]{
                    rs.getString(1), // RECEIVER_NAME
                    rs.getString(2), // EMAIL
                    rs.getString(3), // RECEIVER_PHONE
                    rs.getString(4), // SHIPPING_ADDRESS
                    String.format("%,.0f VNĐ", rs.getDouble(5)) // TOTAL đã format
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    // Lấy danh sách các sản phẩm thuộc về đơn hàng đó
    public List<Object[]> getOrderDetailProducts(int orderId) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT p.NAME, od.PRICE, od.QUANTITY, od.LINE_TOTAL " +
                     "FROM ORDER_DETAIL od JOIN PRODUCT p ON od.PRODUCT_ID = p.PRODUCT_ID " +
                     "WHERE od.ORDER_ID = ?";
                     
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString(1), // Tên sản phẩm
                    String.format("%,.0f", rs.getDouble(2)), // Đơn giá
                    rs.getInt(3),    // Số lượng
                    String.format("%,.0f", rs.getDouble(4))  // Thành tiền
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}