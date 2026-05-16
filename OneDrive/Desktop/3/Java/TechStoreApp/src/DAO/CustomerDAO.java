/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.AccountModel;
import Model.CustomerManageModel;
import Model.UserModel;
import config.ConnectionUtils;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HUY0406
 */
public class CustomerDAO {

    public List<CustomerManageModel> getAllCustomers() {
        List<CustomerManageModel> list = new ArrayList<>();
        String sql = "SELECT "
                + "    a.ACCOUNT_ID, a.USERNAME, u.FULL_NAME, u.EMAIL, u.PHONE_NUMBER, u.BIRTH, u.GENDER, "
                + "    COUNT(o.ORDER_ID) AS ORDER_COUNT, "
                + "    NVL(SUM(o.TOTAL), 0) AS TOTAL_SPENT, "
                + "    NVL(lp.TOTAL_POINTS, 0) AS LOYALTY_POINTS, "
                + "    DECODE(a.STATUS, 1, 'Hoạt động', 0, 'Đã khóa') AS STATUS_TEXT " // Dịch số thành chữ
                + "FROM ACCOUNT a "
                + "JOIN APP_USER u ON a.USER_ID = u.USER_ID "
                + "LEFT JOIN ORDERS o ON u.USER_ID = o.USER_ID AND o.STATUS = 'DELIVERED' AND o.IS_DELETED = 0 "
                + "LEFT JOIN LOYALTY_POINT lp ON u.USER_ID = lp.USER_ID "
                + "WHERE a.IS_DELETED = 0 AND u.IS_DELETED = 0 AND u.USER_TYPE = 'CUSTOMER' "
                + "GROUP BY a.ACCOUNT_ID, a.USERNAME, u.FULL_NAME, u.EMAIL, u.PHONE_NUMBER, u.BIRTH, u.GENDER, a.STATUS, lp.TOTAL_POINTS "
                + "ORDER BY a.ACCOUNT_ID DESC";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CustomerManageModel cus = new CustomerManageModel();
                cus.setAccountId(rs.getInt("ACCOUNT_ID"));
                cus.setUsername(rs.getString("USERNAME"));
                cus.setFullName(rs.getString("FULL_NAME"));
                cus.setEmail(rs.getString("EMAIL"));
                cus.setPhoneNumber(rs.getString("PHONE_NUMBER"));
                cus.setBirth(rs.getDate("BIRTH"));
                cus.setGender(rs.getString("GENDER"));
                cus.setOrderCount(rs.getInt("ORDER_COUNT"));
                cus.setTotalSpent(rs.getDouble("TOTAL_SPENT"));
                cus.setLoyaltyPoints(rs.getInt("LOYALTY_POINTS"));
                cus.setStatus(rs.getString("STATUS_TEXT")); // Lưu chữ "Hoạt động" để hiện lên bảng

                list.add(cus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateCustomerStatus(int accountId, int status) {

        String sql = "UPDATE ACCOUNT SET STATUS = ?, UPDATED_AT = SYSDATE WHERE ACCOUNT_ID = ?";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, accountId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 1. Lấy tổng số lượng khách hàng đang hoạt động
    public int getTotalCustomerCount() {
        String sql = "SELECT COUNT(*) FROM APP_USER WHERE USER_TYPE = 'CUSTOMER'";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 2. Lấy tên và số tiền của khách hàng chi tiêu nhiều nhất
    public Object[] getTopSpenderInfo() {
        // Thay đổi o.TOTAL_AMOUNT thành o.TOTAL
        String sql = "SELECT * FROM ("
                + "  SELECT u.FULL_NAME, NVL(SUM(o.TOTAL), 0) as TOTAL_SPENT "
                + "  FROM APP_USER u "
                + "  LEFT JOIN ORDERS o ON u.USER_ID = o.USER_ID "
                + "  WHERE u.USER_TYPE = 'CUSTOMER' "
                + "  GROUP BY u.FULL_NAME "
                + "  ORDER BY TOTAL_SPENT DESC"
                + ") WHERE ROWNUM = 1";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Object[]{
                    rs.getString("FULL_NAME"),
                    rs.getDouble("TOTAL_SPENT")
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object[]{"Chưa có dữ liệu", 0.0};
    }
}
