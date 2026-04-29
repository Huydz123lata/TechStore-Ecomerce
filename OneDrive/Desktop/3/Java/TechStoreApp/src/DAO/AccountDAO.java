/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import config.ConnectionUtils;
import Model.AccountModel;
import Model.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AccountDAO {

    public void assignGroupToAccount(int accountId, int roleGroupId) {
        String deleteSql = "DELETE FROM ACCOUNT_ASSIGN_ROLE_GROUP WHERE ACCOUNT_ID = ?";
        String insertSql = "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP (ACCOUNT_ID, ROLE_GROUP_ID) VALUES (?, ?)";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement psDel = con.prepareStatement(deleteSql); PreparedStatement psIns = con.prepareStatement(insertSql)) {

            psDel.setInt(1, accountId);
            psDel.executeUpdate();

            psIns.setInt(1, accountId);
            psIns.setInt(2, roleGroupId);
            psIns.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Object[]> getAllAccounts() {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT ACCOUNT_ID, USERNAME FROM ACCOUNT";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                // Đã đổi thành USERNAME
                list.add(new Object[]{rs.getInt("ACCOUNT_ID"), rs.getString("USERNAME")});

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
