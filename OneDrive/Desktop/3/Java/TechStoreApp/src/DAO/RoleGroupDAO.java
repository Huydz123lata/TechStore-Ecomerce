/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.RoleModel;
import Model.RoleGroupModel;
import config.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

/**
 *
 * @author HUY0406
 */
public class RoleGroupDAO {

    public List<RoleGroupModel> getAllRoleGroupName() {
        List<RoleGroupModel> list = new ArrayList<>();

        String sql = "SELECT ROLE_GROUP_ID, NAME_ROLE_GROUP FROM ROLE_GROUP WHERE IS_DELETED = 0";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RoleGroupModel r = new RoleGroupModel(
                        rs.getInt("ROLE_GROUP_ID"),
                        rs.getString("NAME_ROLE_GROUP")
                );
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void clearOldAssignment(int roleGroupId, Connection con) throws SQLException {
        String sql = "DELETE FROM ROLE_GROUP_ASSIGN_ROLE WHERE ROLE_GROUP_ID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleGroupId);
            ps.executeUpdate();
        }
    }

    public void assignRoleToGroup(int roleGroupId, int roleId, Connection con) throws SQLException {
        String sql = "INSERT INTO ROLE_GROUP_ASSIGN_ROLE (ROLE_GROUP_ID, ROLE_ID) VALUES (?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleGroupId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    public boolean insertRoleGroup(String groupName) {
        String sql = "INSERT INTO ROLE_GROUP (NAME_ROLE_GROUP) VALUES (?)";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, groupName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RoleModel> getRolesByGroupId(int groupId) {
        List<RoleModel> list = new ArrayList<>();
        // SQL Join để lấy cả Function Name và các quyền
        String sql = "SELECT r.ROLE_ID, f.FUNCTION_ID, r.ROLE_NAME, f.NAME_FUNCTION, "
                + "r.ADD_PERM, r.EDIT_PERM, r.DELETE_PERM, r.DOWNLOAD_PERM, r.VIEW_PERM "
                + "FROM ROLE_GROUP_ASSIGN_ROLE ar "
                + "JOIN ROLE r ON ar.ROLE_ID = r.ROLE_ID "
                + "JOIN FUNCTION f ON r.FUNCTION_ID = f.FUNCTION_ID "
                + "WHERE ar.ROLE_GROUP_ID = ? AND r.IS_DELETED = 0";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                RoleModel role = new RoleModel(
                        rs.getInt("ROLE_ID"),
                        rs.getInt("FUNCTION_ID"),
                        rs.getString("ROLE_NAME"),
                        rs.getString("NAME_FUNCTION"),
                        rs.getInt("ADD_PERM"),
                        rs.getInt("EDIT_PERM"),
                        rs.getInt("DELETE_PERM"),
                        rs.getInt("VIEW_PERM"),
                        rs.getInt("DOWNLOAD_PERM")
                );

                list.add(role);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
