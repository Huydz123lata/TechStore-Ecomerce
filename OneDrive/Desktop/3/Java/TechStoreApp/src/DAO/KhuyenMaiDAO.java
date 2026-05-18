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

    // HÀM Tự động gắn hậu tố % hoặc VNĐ, và phẩy hàng nghìn
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
        // ĐÃ SỬA: Đổi MAX_DISCOUNT_AMOUNT thành MAX_DISCOUNT
        String sql = "SELECT CODE, DESCRIPTION, DISCOUNT_TYPE, DISCOUNT_VALUE, MIN_ORDER_VALUE, MAX_DISCOUNT, START_AT, END_AT, " +
                     getStatusLogicSQL() + " AS CURRENT_STATUS " +
                     "FROM COUPON WHERE IS_DELETED = 0 ORDER BY START_AT DESC";

        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                String type = rs.getString("DISCOUNT_TYPE");
                String formattedValue = formatDiscountValue(rs.getDouble("DISCOUNT_VALUE"), type);
                
                double min = rs.getDouble("MIN_ORDER_VALUE");
                // ĐÃ SỬA: Lấy đúng cột MAX_DISCOUNT
                double max = rs.getDouble("MAX_DISCOUNT"); 
                
                String minStr = (min <= 0) ? "0 VNĐ" : String.format("%,.0f VNĐ", min);
                String maxStr;
                if ("AMOUNT".equals(type)) {
                    maxStr = " - "; 
                } else {
                    maxStr = (max <= 0) ? "Không giới hạn" : String.format("%,.0f VNĐ", max);
                }

                list.add(new Object[]{
                    rs.getString("CODE"),
                    rs.getString("DESCRIPTION"),
                    type,
                    formattedValue,
                    minStr, 
                    maxStr, 
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
        // ĐÃ SỬA: MAX_DISCOUNT
        StringBuilder sql = new StringBuilder(
            "SELECT CODE, DESCRIPTION, DISCOUNT_TYPE, DISCOUNT_VALUE, MIN_ORDER_VALUE, MAX_DISCOUNT, START_AT, END_AT, " +
            getStatusLogicSQL() + " AS CURRENT_STATUS " +
            "FROM COUPON WHERE IS_DELETED = 0 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (LOWER(CODE) LIKE LOWER(?) OR LOWER(DESCRIPTION) LIKE LOWER(?)) ");
        }
        if (!status.equals("All")) {
            sql.append("AND (").append(getStatusLogicSQL()).append(") = ? ");
        }
        sql.append("ORDER BY START_AT DESC");

        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            if (!status.equals("All")) {
                ps.setString(idx++, status);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("DISCOUNT_TYPE");
                    String formattedValue = formatDiscountValue(rs.getDouble("DISCOUNT_VALUE"), type);
                    
                    double min = rs.getDouble("MIN_ORDER_VALUE");
                    // ĐÃ SỬA: MAX_DISCOUNT
                    double max = rs.getDouble("MAX_DISCOUNT"); 
                    
                    String minStr = (min <= 0) ? "0 VNĐ" : String.format("%,.0f VNĐ", min);
                    String maxStr;
                    if ("AMOUNT".equals(type)) {
                        maxStr = " - "; 
                    } else {
                        maxStr = (max <= 0) ? "Không giới hạn" : String.format("%,.0f VNĐ", max);
                    }

                    list.add(new Object[]{
                        rs.getString("CODE"),
                        rs.getString("DESCRIPTION"),
                        type,
                        formattedValue,
                        minStr, 
                        maxStr, 
                        rs.getDate("START_AT"), 
                        rs.getDate("END_AT"), 
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

    public void insertPromotion(String code, String name, String type, double value, double minOrder, double maxDiscount, java.util.Date startDate, java.util.Date endDate, String status) throws Exception {
        
        String dbType = "PERCENTAGE".equals(type) ? "PERCENTAGE" : "AMOUNT";
        
        // Thêm 2 cột MIN_ORDER_VALUE và MAX_DISCOUNT vào câu lệnh INSERT
        String sql = "INSERT INTO COUPON (CODE, DESCRIPTION, DISCOUNT_TYPE, DISCOUNT_VALUE, MIN_ORDER_VALUE, MAX_DISCOUNT, START_AT, END_AT, IS_ACTIVE, IS_DELETED) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, 0)";
                     
        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name); 
            ps.setString(3, dbType);
            ps.setDouble(4, value);
            ps.setDouble(5, minOrder);     // Set tham số Min
            ps.setDouble(6, maxDiscount);  // Set tham số Max
            ps.setDate(7, new java.sql.Date(startDate.getTime()));
            ps.setDate(8, new java.sql.Date(endDate.getTime()));
            ps.executeUpdate();
        }
    }
    public boolean deletePromotion(String promoCode) {
        String sql = "UPDATE COUPON SET IS_DELETED = 1, IS_ACTIVE = 0 WHERE TRIM(CODE) = ?";
        try (Connection conn = ConnectionOracle.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, promoCode.trim());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false; 
        }
    }
    public List<Object[]> selectAllPro() {
        List<Object[]> list = new ArrayList<>();
        // Hàm getStatusLogicSQL() là hàm tôi đã viết ở tab Coupon trước đây, ta dùng lại luôn!
        String sql = "SELECT PROMOTION_ID, PROMOTION_NAME, START_AT, END_AT, " +
                     getStatusLogicSQL() + " AS CURRENT_STATUS " +
                     "FROM PROMOTION WHERE IS_DELETED = 0 ORDER BY START_AT DESC";

        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                list.add(new Object[]{
                    "PRO" + rs.getString("PROMOTION_ID"), 
                    rs.getString("PROMOTION_NAME"),
                    rs.getDate("START_AT"),
                    rs.getDate("END_AT"),
                    rs.getString("CURRENT_STATUS"),
                    "Xem chi tiết"  // <--- Cột thứ 6: Hành động xem chi tiết
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> selectByConditionPro(String keyword, String status) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT PROMOTION_ID, PROMOTION_NAME, START_AT, END_AT, " +
            getStatusLogicSQL() + " AS CURRENT_STATUS " +
            "FROM PROMOTION WHERE IS_DELETED = 0 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (LOWER(PROMOTION_NAME) LIKE LOWER(?) OR 'PRO'||PROMOTION_ID LIKE UPPER(?)) ");
        }
        if (!status.equals("All")) {
            sql.append("AND (").append(getStatusLogicSQL()).append(") = ? ");
        }
        sql.append("ORDER BY START_AT DESC");

        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            if (!status.equals("All")) {
                ps.setString(idx++, status);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        "PRO" + rs.getString("PROMOTION_ID"), 
                        rs.getString("PROMOTION_NAME"), 
                        rs.getDate("START_AT"), 
                        rs.getDate("END_AT"), 
                        rs.getString("CURRENT_STATUS"),
                        "Xem chi tiết"
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatusPro(String proCode, String newStatus) {
        boolean isCancelled = "CANCELLED".equals(newStatus);
        String sql;
        String idStr = proCode.replace("PRO", ""); // Cắt chữ PRO đi để lấy số
        
        if (isCancelled) {
            sql = "UPDATE PROMOTION SET IS_ACTIVE = 0, " +
                  "END_AT = CASE WHEN SYSDATE < START_AT THEN START_AT + (1/24) ELSE SYSDATE END " +
                  "WHERE PROMOTION_ID = ?";
        } else {
            sql = "UPDATE PROMOTION SET IS_ACTIVE = 1 WHERE PROMOTION_ID = ?";
        }
        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idStr));
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean deletePromotionPro(String proCode) {
        String idStr = proCode.replace("PRO", "");
        // Xóa mềm: Bật cờ Deleted và Khóa Active
        String sql = "UPDATE PROMOTION SET IS_DELETED = 1, IS_ACTIVE = 0 WHERE PROMOTION_ID = ?";
        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idStr));
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    // Thêm hàm này vào KhuyenMaiDAO.java
    public boolean updateCoupon(String oldCode, String newCode, String name, String type, double value, double minOrder, double maxDiscount, java.util.Date startDate, java.util.Date endDate) {
        // Chuyển đổi định dạng Type cho khớp với Database
        String dbType = "PERCENTAGE".equals(type) ? "PERCENTAGE" : "AMOUNT";
        
        // Câu lệnh SQL cập nhật dữ liệu. Lưu ý điều kiện WHERE TRIM(CODE)=? là lấy mã cũ để tìm
        String sql = "UPDATE COUPON SET CODE=?, DESCRIPTION=?, DISCOUNT_TYPE=?, DISCOUNT_VALUE=?, MIN_ORDER_VALUE=?, MAX_DISCOUNT=?, START_AT=?, END_AT=?, UPDATED_AT=SYSDATE WHERE TRIM(CODE)=?";
        
        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newCode.trim().toUpperCase());
            ps.setString(2, name.trim());
            ps.setString(3, dbType);
            ps.setDouble(4, value);
            ps.setDouble(5, minOrder);
            ps.setDouble(6, maxDiscount);
            ps.setDate(7, new java.sql.Date(startDate.getTime()));
            ps.setDate(8, new java.sql.Date(endDate.getTime()));
            ps.setString(9, oldCode.trim()); // Mã cũ nằm ở vị trí dấu ? thứ 9
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean insertPromotionWithProducts(String name, java.util.Date startDate, java.util.Date endDate, java.util.Map<Integer, Double> productDiscounts) throws Exception {
        java.sql.Connection conn = null;
        try {
            conn = config.ConnectionOracle.getOracleConnection();
            conn.setAutoCommit(false); 

            // 1. Thêm vào bảng cha PROMOTION
            String sqlPro = "INSERT INTO PROMOTION (PROMOTION_NAME, START_AT, END_AT, IS_ACTIVE, IS_DELETED) VALUES (?, ?, ?, 1, 0)";
            int newProId = -1;
            
            try (java.sql.PreparedStatement ps1 = conn.prepareStatement(sqlPro, new String[]{"PROMOTION_ID"})) {
                ps1.setString(1, name.trim());
                ps1.setDate(2, new java.sql.Date(startDate.getTime()));
                ps1.setDate(3, new java.sql.Date(endDate.getTime()));
                ps1.executeUpdate();
                
                try (java.sql.ResultSet rs = ps1.getGeneratedKeys()) {
                    if (rs.next()) newProId = rs.getInt(1);
                }
            }

            if (newProId == -1) throw new Exception("Không thể khởi tạo ID Chương trình khuyến mãi.");

            // 2. Thêm danh sách sản phẩm vào bảng con (Lấy Mức giảm từ trong Map ra)
            String sqlDetail = "INSERT INTO PROMOTION_PRODUCT (PROMOTION_ID, PRODUCT_ID, DISCOUNT_VALUE) VALUES (?, ?, ?)";
            try (java.sql.PreparedStatement ps2 = conn.prepareStatement(sqlDetail)) {
                // Duyệt qua từng cặp [Mã SP - Mức giảm] trong Map
                for (java.util.Map.Entry<Integer, Double> entry : productDiscounts.entrySet()) {
                    ps2.setInt(1, newProId);
                    ps2.setInt(2, entry.getKey());   // PRODUCT_ID
                    ps2.setDouble(3, entry.getValue()); // DISCOUNT_VALUE riêng của SP đó
                    ps2.addBatch(); 
                }
                ps2.executeBatch(); 
            }

            conn.commit(); 
            return true;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    public List<Object[]> getProductsByPromotion(int promoId) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT p.PRODUCT_ID, p.NAME, pp.DISCOUNT_VALUE " +
                     "FROM PROMOTION_PRODUCT pp " +
                     "JOIN PRODUCT p ON pp.PRODUCT_ID = p.PRODUCT_ID " +
                     "WHERE pp.PROMOTION_ID = ?";
        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, promoId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Định dạng số cho mức giảm đẹp hơn
                    String formatVal = String.format("%,.0f", rs.getDouble("DISCOUNT_VALUE"));
                    
                    list.add(new Object[]{
                        rs.getInt("PRODUCT_ID"),
                        rs.getString("NAME"),
                        formatVal
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    // Kéo dữ liệu gốc lên để nạp vào Map
    public java.util.Map<Integer, Double> getRawProductsByPromotion(int promoId) {
        java.util.Map<Integer, Double> map = new java.util.HashMap<>();
        String sql = "SELECT PRODUCT_ID, DISCOUNT_VALUE FROM PROMOTION_PRODUCT WHERE PROMOTION_ID = ?";
        try (java.sql.Connection conn = config.ConnectionOracle.getOracleConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("PRODUCT_ID"), rs.getDouble("DISCOUNT_VALUE"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // Logic Cập nhật (Xóa cũ - Thêm mới)
    public boolean updatePromotionWithProducts(int promoId, String name, java.util.Date startDate, java.util.Date endDate, java.util.Map<Integer, Double> productDiscounts) throws Exception {
        java.sql.Connection conn = null;
        try {
            conn = config.ConnectionOracle.getOracleConnection();
            conn.setAutoCommit(false); 

            // 1. Cập nhật bảng cha
            String sqlUpdatePro = "UPDATE PROMOTION SET PROMOTION_NAME = ?, START_AT = ?, END_AT = ? WHERE PROMOTION_ID = ?";
            try (java.sql.PreparedStatement ps1 = conn.prepareStatement(sqlUpdatePro)) {
                ps1.setString(1, name.trim());
                ps1.setDate(2, new java.sql.Date(startDate.getTime()));
                ps1.setDate(3, new java.sql.Date(endDate.getTime()));
                ps1.setInt(4, promoId);
                ps1.executeUpdate();
            }

            // 2. Xóa sạch danh sách sản phẩm cũ của chương trình này
            String sqlDeleteOld = "DELETE FROM PROMOTION_PRODUCT WHERE PROMOTION_ID = ?";
            try (java.sql.PreparedStatement ps2 = conn.prepareStatement(sqlDeleteOld)) {
                ps2.setInt(1, promoId);
                ps2.executeUpdate();
            }

            // 3. Chèn lại danh sách sản phẩm mới (hoặc giữ nguyên nếu họ không sửa gì)
            String sqlInsertNew = "INSERT INTO PROMOTION_PRODUCT (PROMOTION_ID, PRODUCT_ID, DISCOUNT_VALUE) VALUES (?, ?, ?)";
            try (java.sql.PreparedStatement ps3 = conn.prepareStatement(sqlInsertNew)) {
                for (java.util.Map.Entry<Integer, Double> entry : productDiscounts.entrySet()) {
                    ps3.setInt(1, promoId);
                    ps3.setInt(2, entry.getKey());   
                    ps3.setDouble(3, entry.getValue()); 
                    ps3.addBatch(); 
                }
                ps3.executeBatch(); 
            }

            conn.commit(); 
            return true;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}