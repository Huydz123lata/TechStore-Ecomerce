/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import config.ConnectionUtils;
import Model.AccountModel;
import Model.RoleGroupModel;
import Model.RoleModel;
import Model.UserModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AccountDAO {

    public boolean insertAccount(AccountModel acc) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // Bước 1: Thêm vào bảng APP_USER
            String sqlUser = "INSERT INTO APP_USER (FULL_NAME, PHONE_NUMBER, ADDRESS, BIRTH, GENDER, USER_TYPE) VALUES (?, ?, ?, ?, ?, ?)";
            String[] generatedColumns = {"USER_ID"};
            int generatedUserId = 0;

            try (PreparedStatement psUser = con.prepareStatement(sqlUser, generatedColumns)) {
                UserModel u = acc.getUserInfo();
                psUser.setString(1, u.getFullName());
                psUser.setString(2, u.getSDT());
                psUser.setString(3, u.getAddress());
                psUser.setDate(4, u.getNgaySinh());
                psUser.setString(5, u.getGioiTinh());
                psUser.setString(6, u.getUserType());

                psUser.executeUpdate();
                ResultSet rs = psUser.getGeneratedKeys();
                if (rs.next()) {
                    generatedUserId = rs.getInt(1);
                }
            }

            if (generatedUserId == 0) {
                throw new Exception("Lỗi: Không lấy được USER_ID");
            }

            // Bước 2: Thêm vào bảng ACCOUNT
            String sqlAcc = "INSERT INTO ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, STATUS) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psAcc = con.prepareStatement(sqlAcc)) {
                psAcc.setInt(1, generatedUserId);
                psAcc.setString(2, acc.getUsername());
                psAcc.setString(3, acc.getPasswordHash());
                psAcc.setString(4, "ACTIVE");
                psAcc.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) try {
                con.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {
            }
        }
    }

    public List<AccountModel> getAllAdminAccounts() {
        List<AccountModel> list = new ArrayList();
        String sql = "SELECT a.ACCOUNT_ID, a.USER_ID, a.USERNAME, a.STATUS, "
                + "u.FULL_NAME, u.PHONE_NUMBER, u.GENDER, u.BIRTH, u.ADDRESS, rg.NAME_ROLE_GROUP "
                + "FROM ACCOUNT a "
                + "JOIN APP_USER u ON a.USER_ID = u.USER_ID "
                + "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON aarg.ACCOUNT_ID = a.ACCOUNT_ID "
                + "LEFT JOIN ROLE_GROUP rg ON rg.ROLE_GROUP_ID = aarg.ROLE_GROUP_ID "
                + "WHERE a.IS_DELETED = 0 AND u.USER_TYPE IN ('ADMIN','STAFF')";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserModel u = new UserModel();
                u.setFullName(rs.getString("FULL_NAME"));
                u.setSDT(rs.getString("PHONE_NUMBER"));
                u.setAddress(rs.getString("ADDRESS"));
                u.setNgaySinh(rs.getDate("BIRTH"));
                u.setGioiTinh(rs.getString("GENDER"));

                RoleGroupModel rgm = new RoleGroupModel();
                rgm.setRoleGroupName(rs.getString("NAME_ROLE_GROUP"));

                AccountModel acc = new AccountModel(
                        rs.getInt("ACCOUNT_ID"),
                        rs.getInt("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("STATUS")
                );
                acc.setUserInfo(u);
                acc.setRoleGroup(rgm);
                list.add(acc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AccountModel> getAllCustomerAccounts() {
        List<AccountModel> list = new ArrayList();
        String sql = "SELECT a.ACCOUNT_ID, a.USER_ID, a.USERNAME, a.STATUS,u.FULL_NAME, u.PHONE_NUMBER, u.ADDRESS "
                + "FROM ACCOUNT a "
                + "JOIN APP_USER u ON a.USER_ID = u.USER_ID "
                + "WHERE a.IS_DELETED = 0 AND USER_TYPE IN('CUSTOMER')";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserModel u = new UserModel();
                u.setFullName(rs.getString("FULL_NAME"));
                u.setSDT(rs.getString("PHONE_NUMBER"));
                u.setAddress(rs.getString("ADDRESS"));

                AccountModel acc = new AccountModel(
                        rs.getInt("ACCOUNT_ID"),
                        rs.getInt("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("STATUS")
                );
                acc.setUserInfo(u);
                list.add(acc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AccountModel> getRoleGroupAssignedAccounts(int filterId) {
        List<AccountModel> list = new ArrayList<>();
        String sql = "SELECT a.ACCOUNT_ID, a.USERNAME, rg.NAME_ROLE_GROUP, rg.ROLE_GROUP_ID "
                + "FROM ACCOUNT a "
                + " JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON a.ACCOUNT_ID = aarg.ACCOUNT_ID AND aarg.IS_DELETED = 0 "
                + " JOIN ROLE_GROUP rg ON aarg.ROLE_GROUP_ID = rg.ROLE_GROUP_ID AND rg.IS_DELETED = 0 "
                + "WHERE a.IS_DELETED = 0";

        if (filterId > 0) {
            sql += " AND a.ACCOUNT_ID = ?";
        }

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            if (filterId > 0) {
                ps.setInt(1, filterId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                //sử dụng constructor mặc định
                AccountModel acc = new AccountModel();
                acc.setAccountId(rs.getInt("ACCOUNT_ID"));
                acc.setUsername(rs.getString("USERNAME"));

                RoleGroupModel rg = new RoleGroupModel();
                rg.setRoleGroupName(rs.getString("NAME_ROLE_GROUP"));
                rg.setRoleGroupId(rs.getInt("ROLE_GROUP_ID"));
                acc.setRoleGroup(rg);

                list.add(acc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean assignRoleGroup(int accountId, int roleGroupId) {

        String deleteSql = "DELETE FROM ACCOUNT_ASSIGN_ROLE_GROUP WHERE ACCOUNT_ID = ?";

        String insertSql = "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP (ACCOUNT_ID, ROLE_GROUP_ID, CREATED_AT, UPDATED_AT, IS_DELETED) "
                + "VALUES (?, ?, SYSDATE, SYSDATE, 0)";

        try (Connection con = ConnectionUtils.getMyConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement psDel = con.prepareStatement(deleteSql); PreparedStatement psIns = con.prepareStatement(insertSql)) {

                // 1. Xóa gán quyền cũ
                psDel.setInt(1, accountId);
                psDel.executeUpdate();

                // 2. Thêm gán quyền mới
                psIns.setInt(1, accountId);
                psIns.setInt(2, roleGroupId);
                psIns.executeUpdate();

                con.commit(); // Lưu thay đổi
                return true;
            } catch (Exception e) {
                con.rollback(); // Hủy bỏ nếu một trong hai lệnh lỗi
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRoleAssigned(int accountId, int roleId) {
        String sql = "SELECT COUNT(*) "
                + "FROM ACCOUNT_ASSIGN_ROLE "
                + "WHERE ACCOUNT_ID = ? "
                + "AND ROLE_ID = ? ";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, roleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean assignRole(int accountId, int roleId) {
        String sql = "INSERT INTO ACCOUNT_ASSIGN_ROLE (ACCOUNT_ID, ROLE_ID) VALUES (?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, roleId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<AccountModel> getRoleAssignedAccounts(int filterId) {
        List<AccountModel> list = new ArrayList<>();
        String sql = "SELECT a.ACCOUNT_ID, a.USERNAME, r.ROLE_NAME, r.ROLE_ID "
                + "FROM ACCOUNT a "
                + " JOIN ACCOUNT_ASSIGN_ROLE aar ON a.ACCOUNT_ID = aar.ACCOUNT_ID AND aar.IS_DELETED = 0 "
                + " JOIN ROLE r ON aar.ROLE_ID = r.ROLE_ID AND r.IS_DELETED = 0 "
                + "WHERE a.IS_DELETED = 0";

        if (filterId > 0) {
            sql += " AND a.ACCOUNT_ID = ?";
        }

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            if (filterId > 0) {
                ps.setInt(1, filterId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                //sử dụng constructor mặc định
                AccountModel acc = new AccountModel();
                acc.setAccountId(rs.getInt("ACCOUNT_ID"));
                acc.setUsername(rs.getString("USERNAME"));

                RoleModel r = new RoleModel();
                r.setRoleName(rs.getString("ROLE_NAME"));
                r.setRoleID(rs.getInt("ROLE_ID"));
                acc.setRole(r);

                list.add(acc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean revokeRoleGroup(int accountId, int roleGroupId) {
        String sql = "UPDATE ACCOUNT_ASSIGN_ROLE_GROUP SET IS_DELETED = 1 WHERE ACCOUNT_ID = ? AND ROLE_GROUP_ID = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, roleGroupId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean revokeRole(int accountId, int roleId) {
        String sql = "DELETE FROM ACCOUNT_ASSIGN_ROLE WHERE ACCOUNT_ID = ? AND ROLE_ID = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, roleId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
