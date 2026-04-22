/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Business.Sql;

import Common.DB.ConnectionUtils;
import Model.Account;
import java.sql.*;

public class AccountSql {

    //xử lý đăng nhập
    public Account checkLogin(String user, String pass) {
        String SQL = "SELECT ACCOUNT_ID, USER_ID, USERNAME, STATUS "
                + "FROM ACCOUNT "
                + "WHERE USERNAME = ? AND PASSWORD_HASH = ? AND IS_DELETED = 0";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {

            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Account( //accountModel
                        rs.getInt("ACCOUNT_ID"),
                        rs.getInt("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("STATUS")
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // xử lý đăng ký
    public boolean register(Account acc) throws Exception {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            String sqlUser = "INSERT INTO \"USER\" (FULL_NAME, SDT, NGAY_SINH, GIOI_TINH) VALUES (?, ?, ?, ?)";
            PreparedStatement ps1 = con.prepareStatement(sqlUser, new String[]{"USER_ID"});

            ps1.setString(1, acc.getUserInfo().getFullName());
            ps1.setString(2, acc.getUserInfo().getSDT());

            // Xử lý ngày sinh (đề phòng bị null nếu ngày không hợp lệ)
            if (acc.getUserInfo().getNgaySinh() != null) {
                ps1.setDate(3, acc.getUserInfo().getNgaySinh());
            } else {
                ps1.setNull(3, java.sql.Types.DATE);
            }

            ps1.setString(4, acc.getUserInfo().getGioiTinh());

            ps1.executeUpdate();

            int idVuaTao = -1;
            ResultSet rs = ps1.getGeneratedKeys();
            if (rs.next()) {
                idVuaTao = rs.getInt(1);
            }

            String sqlAcc = "INSERT INTO ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, STATUS) VALUES (?, ?, ?, 'ACTIVE')";
            PreparedStatement ps2 = con.prepareStatement(sqlAcc);
            ps2.setInt(1, idVuaTao);
            ps2.setString(2, acc.getUsername());
            ps2.setString(3, acc.getPassword());
            ps2.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception ex) {
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void createToken(int maTaiKhoan, String token) {
        String tmp = java.util.UUID.randomUUID().toString().replace("-", "");
        String sql = "INSERT INTO ACCOUNT_TOKEN (ACCOUNT_ID, TOKEN_VALUE, EXPIRES_AT) VALUES (?, ?, ?)";

        // Sử dụng kết nối từ package Common.DB của bạn
        try (Connection con = Common.DB.ConnectionUtils.getMyConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maTaiKhoan);
            ps.setString(2, tmp);
            ps.setObject(3, java.time.LocalDate.now().plusDays(60));

            ps.executeUpdate();
            System.out.println("Đã tạo token trong AccountSql cho ID: " + maTaiKhoan);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
