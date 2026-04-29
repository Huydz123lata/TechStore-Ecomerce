/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.Role;
import config.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class RoleDAO {

    public List<String> getAllRoles() {
        List<String> listRoles = new ArrayList<>();

        String sql = "SELECT ROLE_NAME FROM ROLE WHERE IS_DELETED = 0";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listRoles.add(rs.getString("ROLE_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listRoles;
    }

    public List<String> getFunctionName() {
        List<String> listFunctions = new ArrayList<>();

        String sql = "SELECT NAME_FUNCTION FROM FUNCTION";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listFunctions.add(rs.getString("NAME_FUNCTION"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listFunctions;
    }

    public boolean savePermission(String roleName, String functionName, int add, int edit, int del, int view, int dl) {
        String checkSql = "SELECT COUNT(*) FROM ROLE WHERE ROLE_NAME = ? "
                + "AND FUNCTION_ID = (SELECT FUNCTION_ID FROM FUNCTION WHERE NAME_FUNCTION = ?)";
        String insertSql = "INSERT INTO ROLE (ROLE_NAME, FUNCTION_ID, ADD_PERM, EDIT_PERM, DELETE_PERM, VIEW_PERM, DOWNLOAD_PERM) "
                + "VALUES (?, (SELECT FUNCTION_ID FROM FUNCTION WHERE NAME_FUNCTION = ?), ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE ROLE "
                + "SET ADD_PERM = ?, "
                + "    EDIT_PERM = ?, "
                + "    DELETE_PERM = ?, "
                + "    DOWNLOAD_PERM = ?, "
                + "    VIEW_PERM = ?,"
                + "WHERE ROLE_NAME = ? "
                + "  AND FUNCTION_ID = (SELECT FUNCTION_ID FROM FUNCTION WHERE NAME_FUNCTION = ?) ";
        try (Connection con = ConnectionUtils.getMyConnection()) {
            PreparedStatement psCheck = con.prepareStatement(checkSql);
            psCheck.setString(1, roleName);
            psCheck.setString(2, functionName);
            ResultSet rs = psCheck.executeQuery();

            boolean exist = false;
            if (rs.next() && rs.getInt(1) > 0) {
                exist = true;
            }

            PreparedStatement psAction;
            if (exist) {
                psAction = con.prepareStatement(updateSql);
                psAction.setInt(1, add);
                psAction.setInt(2, edit);
                psAction.setInt(3, del);
                psAction.setInt(4, dl);
                psAction.setInt(5, view);
                psAction.setString(6, roleName);
                psAction.setString(7, functionName);
            } else {
                psAction = con.prepareStatement(insertSql);
                psAction.setString(1, roleName);
                psAction.setString(2, functionName);
                psAction.setInt(3, add);
                psAction.setInt(4, edit);
                psAction.setInt(5, del);
                psAction.setInt(6, view);
                psAction.setInt(7, dl);
            }
            return psAction.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePermission(String roleName, String functionName) {
        String sql = "UPDATE ROLE SET IS_DELETED = 1 "
                + "WHERE ROLE_NAME = ? "
                + "AND FUNCTION_ID = (SELECT FUNCTION_ID FROM FUNCTION WHERE NAME_FUNCTION = ?)";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, roleName);
            ps.setString(2, functionName);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Role> getPermissionsByAccountId(int accountId) {
        List<Role> list = new ArrayList<>();
        String sql
                = "SELECT "
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
                Role p = new Role(
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
}
