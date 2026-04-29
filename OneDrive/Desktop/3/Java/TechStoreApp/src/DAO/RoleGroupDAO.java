/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import config.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author HUY0406
 */
public class RoleGroupDAO {

    public List<String> getRoleGroupName() {
        List<String> listRoleGroup = new ArrayList<>();

        String sql = "SELECT NAME_ROLE_GROUP FROM ROLE_GROUP WHERE IS_DELETED = 0";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listRoleGroup.add(rs.getString("NAME_ROLE_GROUP"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listRoleGroup;
    }

    public boolean saveGroupAssign(String groupName, List<String> listRolesChecked) {
        String deleteSql = "DELETE FROM ROLE_GROUP_ASSIGN_ROLE WHERE ROLE_GROUP_ID IN "
                + "(SELECT ROLE_GROUP_ID FROM ROLE_GROUP WHERE NAME_ROLE_GROUP = ?)";

        String insertSql = "INSERT INTO ROLE_GROUP_ASSIGN_ROLE (ROLE_GROUP_ID, ROLE_ID) VALUES ("
                + "(SELECT ROLE_GROUP_ID FROM ROLE_GROUP WHERE NAME_ROLE_GROUP = ? AND IS_DELETED = 0), "
                + "(SELECT ROLE_ID FROM ROLE WHERE ROLE_NAME = ? AND IS_DELETED = 0))";

        try (Connection con = ConnectionUtils.getMyConnection()) {
            PreparedStatement psDel = con.prepareStatement(deleteSql);
            psDel.setString(1, groupName);
            psDel.executeUpdate();

            PreparedStatement psIns = con.prepareStatement(insertSql);
            for (String rName : listRolesChecked) {
                psIns.setString(1, groupName);
                psIns.setString(2, rName);
                psIns.executeUpdate();
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Vector> getRoleGroupCombined() {
        List<Vector> data = new ArrayList<>();

        String sql = "SELECT rg.NAME_ROLE_GROUP, "
                + "LISTAGG(r.ROLE_NAME, ', ') WITHIN GROUP (ORDER BY r.ROLE_NAME) AS ROLES "
                + "FROM ROLE_GROUP rg "
                + "LEFT JOIN ROLE_GROUP_ASSIGN_ROLE rgar ON rg.ROLE_GROUP_ID = rgar.ROLE_GROUP_ID "
                + "LEFT JOIN ROLE r ON rgar.ROLE_ID = r.ROLE_ID "
                + "WHERE rg.IS_DELETED = 0 "
                + "GROUP BY rg.NAME_ROLE_GROUP "
                + "ORDER BY rg.NAME_ROLE_GROUP";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector row = new Vector();
                row.add(rs.getString(1)); // Cột Role_Group

                String roles = rs.getString(2);
                row.add(roles == null ? "" : roles); // Cột Role (đã có dấu phẩy)

                data.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public int getGroupIdByName(String groupName) {
        int id = -1;
        String sql = "SELECT ROLE_GROUP_ID FROM ROLE_GROUP WHERE NAME_ROLE_GROUP = ?";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }
}
