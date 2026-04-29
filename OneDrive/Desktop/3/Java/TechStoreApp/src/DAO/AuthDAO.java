/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.AccountModel;
import config.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.*;

/**
 *
 * @author HUY0406
 */
public class AuthDAO {

    //xử lý đăng nhập
    public AccountModel checkLogin(String user, String pass) {
        String SQL = "SELECT ACCOUNT_ID, USER_ID, USERNAME, STATUS "
                + "FROM ACCOUNT "
                + "WHERE USERNAME = ? AND PASSWORD_HASH = ? AND IS_DELETED = 0";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {

            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new AccountModel(
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
    public boolean register(AccountModel acc) throws Exception {

        Connection con = null;

        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // INSERT USER
            String sqlUser
                    = "INSERT INTO \"USER\" (FULL_NAME, SDT, NGAY_SINH, GIOI_TINH) "
                    + "VALUES (?, ?, ?, ?)";

            int userId = -1;

            try (PreparedStatement ps1 = con.prepareStatement(sqlUser, new String[]{"USER_ID"})) {

                ps1.setString(1, acc.getUserInfo().getFullName());
                ps1.setString(2, acc.getUserInfo().getSDT());

                if (acc.getUserInfo().getNgaySinh() != null) {
                    ps1.setDate(3, acc.getUserInfo().getNgaySinh());
                } else {
                    ps1.setNull(3, Types.DATE);
                }

                ps1.setString(4, acc.getUserInfo().getGioiTinh());

                ps1.executeUpdate();

                try (ResultSet rs = ps1.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }
            }

            // INSERT ACCOUNT
            String sqlAcc
                    = "INSERT INTO ACCOUNT "
                    + "(USER_ID, USERNAME, PASSWORD_HASH, STATUS) "
                    + "VALUES (?, ?, ?, 'ACTIVE')";

            try (PreparedStatement ps2 = con.prepareStatement(sqlAcc)) {

                ps2.setInt(1, userId);
                ps2.setString(2, acc.getUsername());
                ps2.setString(3, acc.getPassword());

                ps2.executeUpdate();
            }

            con.commit();
            return true;

        } catch (Exception e) {

            if (con != null) {
                con.rollback();
            }
            throw e;

        } finally {

            if (con != null) {
                con.close();
            }
        }
    }
}
