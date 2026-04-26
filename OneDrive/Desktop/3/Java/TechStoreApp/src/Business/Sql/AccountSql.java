/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Business.Sql;

import Common.DB.ConnectionUtils;
import Model.Account;
import Model.Permission;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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

    public List<Permission> getPermissionsByAccountId(int accountId) {
        List<Permission> list = new ArrayList<>();
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
                Permission p = new Permission(
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
