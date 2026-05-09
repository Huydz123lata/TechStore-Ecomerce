/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.RoleModel;
import config.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class RoleDAO {

    public int getOrCreateRoleId(int funcId, String roleName, int a, int e, int d, int down, int v, Connection con) throws SQLException {
        // Tìm xem đã có dòng nào giống hệt chưa
        String find = "SELECT ROLE_ID "
                + "FROM ROLE "
                + "WHERE FUNCTION_ID = ? "
                + "    AND ADD_PERM = ? "
                + "    AND EDIT_PERM = ? "
                + "    AND DELETE_PERM = ? "
                + "    AND DOWNLOAD_PERM = ? "
                + "    AND VIEW_PERM = ? "
                + "AND IS_DELETED = 0";
        try (PreparedStatement ps = con.prepareStatement(find)) {
            ps.setInt(1, funcId);
            ps.setInt(2, a);
            ps.setInt(3, e);
            ps.setInt(4, d);
            ps.setInt(5, down);
            ps.setInt(6, v);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ROLE_ID");
            }
        }

        String insertSql = "INSERT INTO ROLE (FUNCTION_ID, ROLE_NAME, ADD_PERM, EDIT_PERM, DELETE_PERM, DOWNLOAD_PERM, VIEW_PERM) VALUES (?, ?, ?, ?, ?, ?,?)";
        String generatedColumns[] = {"ROLE_ID"};
        try (PreparedStatement ps = con.prepareStatement(insertSql, generatedColumns)) {
            ps.setInt(1, funcId);
            ps.setString(2, roleName);
            ps.setInt(3, a);
            ps.setInt(4, e);
            ps.setInt(5, d);
            ps.setInt(6, down);
            ps.setInt(7, v);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public List<RoleModel> getPermissionsByAccountId(int accountId) {
        List<RoleModel> list = new ArrayList<>();
        String sql = "SELECT "
                + "    f.FUNCTION_ID, "
                + "    f.NAME_FUNCTION, "
                + "    MAX(r.ADD_PERM) AS CAN_ADD, "
                + "    MAX(r.EDIT_PERM) AS CAN_EDIT, "
                + "    MAX(r.DELETE_PERM) AS CAN_DELETE, "
                + "    MAX(r.VIEW_PERM) AS CAN_VIEW, "
                + "    MAX(r.DOWNLOAD_PERM) AS CAN_DOWNLOAD "
                + "FROM FUNCTION f "
                + "JOIN ROLE r ON f.FUNCTION_ID = r.FUNCTION_ID "
                + "LEFT JOIN ROLE_GROUP_ASSIGN_ROLE rgar ON r.ROLE_ID = rgar.ROLE_ID "
                + "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON rgar.ROLE_GROUP_ID = aarg.ROLE_GROUP_ID "
                + "LEFT JOIN ACCOUNT_ASSIGN_ROLE aar ON r.ROLE_ID = aar.ROLE_ID "
                + "WHERE (aarg.ACCOUNT_ID = ? OR aar.ACCOUNT_ID = ?) "
                + "  AND r.IS_DELETED = 0 "
                + "GROUP BY f.FUNCTION_ID, f.NAME_FUNCTION "
                + "ORDER BY f.NAME_FUNCTION";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, accountId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RoleModel p = new RoleModel(
                        0,
                        rs.getInt("FUNCTION_ID"),
                        null,
                        rs.getString("NAME_FUNCTION"),
                        rs.getInt("CAN_ADD"),
                        rs.getInt("CAN_EDIT"),
                        rs.getInt("CAN_DELETE"),
                        rs.getInt("CAN_VIEW"),
                        rs.getInt("CAN_DOWNLOAD")
                );
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RoleModel> getAllRoleName() {
        List<RoleModel> list = new ArrayList<>();
        String sql = "SELECT ROLE_ID,ROLE_NAME FROM ROLE WHERE IS_DELETED = 0";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RoleModel role = new RoleModel(rs.getInt("ROLE_ID"), 0, rs.getString("ROLE_NAME"), "", 0, 0, 0, 0, 0);
                list.add(role);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
