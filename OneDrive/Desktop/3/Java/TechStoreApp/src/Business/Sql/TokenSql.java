/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Business.Sql;

import Common.DB.ConnectionUtils;
import java.sql.*;

/**
 *
 * @author HUY0406
 */
public class TokenSql {

    public void createToken(int accountId, String token) {
        String sql = "INSERT INTO ACCOUNT_TOKEN (ACCOUNT_ID, TOKEN_VALUE, EXPIRES_AT, IS_REVOKED) "
                + "VALUES (?, ?, ADD_MONTHS(SYSDATE, 2), 'N')";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, token);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String checkStatus(String token) {
        String sql = "SELECT IS_REVOKED FROM ACCOUNT_TOKEN WHERE TOKEN_VALUE = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("IS_REVOKED"); //bốc lấy giá trị thực tế trong ô đó.
            }
        } catch (Exception e) {
        }
        return "Y";
    }

    // Hàm này dùng khi User chủ động bấm Đăng xuất (Khóa token lại luôn)
    public void revokeToken(String tokenValue) {
        String sql = "UPDATE ACCOUNT_TOKEN SET IS_REVOKED = 'Y' WHERE TOKEN_VALUE = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tokenValue);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
