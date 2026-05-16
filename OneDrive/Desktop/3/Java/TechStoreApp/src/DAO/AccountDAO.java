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

    public boolean addAdminStaffAccount(AccountModel acc) {
        UserModel user = acc.getUserInfo();
        // Chèn SYSDATE vào CREATED_AT nếu ba có cột đó
        String sqlUser = "INSERT INTO APP_USER (FULL_NAME, PHONE_NUMBER, EMAIL, ADDRESS, BIRTH, GENDER, USER_TYPE, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE)";
        String sqlAcc = "INSERT INTO ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT) VALUES (?, ?, ?, ?, SYSDATE)";

        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // Oracle yêu cầu chỉ định rõ tên cột ID để trả về
            PreparedStatement psUser = con.prepareStatement(sqlUser, new String[]{"USER_ID"});
            psUser.setString(1, user.getFullName());
            psUser.setString(2, user.getSDT());
            psUser.setString(3, user.getEmail());
            psUser.setString(4, user.getAddress());
            psUser.setDate(5, user.getNgaySinh());
            psUser.setString(6, user.getGioiTinh());
            psUser.setString(7, user.getUserType());
            psUser.executeUpdate();

            ResultSet rsUser = psUser.getGeneratedKeys();
            int generatedUserId = 0;
            if (rsUser.next()) {
                // Dùng getBigDecimal cho chắc ăn với Oracle
                generatedUserId = rsUser.getBigDecimal(1).intValue();
            }

            PreparedStatement psAcc = con.prepareStatement(sqlAcc);
            psAcc.setInt(1, generatedUserId);
            psAcc.setString(2, acc.getUsername());
            psAcc.setString(3, acc.getPasswordHash());
            psAcc.setInt(4, acc.getStatus());
            psAcc.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean updateAdminStaffAccount(AccountModel acc) {
        UserModel user = acc.getUserInfo();
        // Oracle dùng SYSDATE để lấy thời gian hiện tại
        String sqlUser = "UPDATE APP_USER SET FULL_NAME=?, PHONE_NUMBER=?, EMAIL=?, ADDRESS=?, BIRTH=?, GENDER=?, USER_TYPE=?, UPDATED_AT=SYSDATE WHERE USER_ID=?";
        String sqlAcc = "UPDATE ACCOUNT SET USERNAME=?, STATUS=?, UPDATED_AT=SYSDATE WHERE ACCOUNT_ID=?";

        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false); // Chạy giao dịch (Transaction) để đảm bảo sửa là sửa cả hai bảng

            // 1. Cập nhật thông tin cá nhân ở bảng APP_USER
            PreparedStatement psUser = con.prepareStatement(sqlUser);
            psUser.setString(1, user.getFullName());
            psUser.setString(2, user.getSDT());
            psUser.setString(3, user.getEmail());
            psUser.setString(4, user.getAddress());
            psUser.setDate(5, user.getNgaySinh());
            psUser.setString(6, user.getGioiTinh());
            psUser.setString(7, user.getUserType());
            psUser.setInt(8, user.getUserId());
            psUser.executeUpdate();

            // 2. Cập nhật thông tin đăng nhập ở bảng ACCOUNT
            PreparedStatement psAcc = con.prepareStatement(sqlAcc);
            psAcc.setString(1, acc.getUsername());
            psAcc.setInt(2, acc.getStatus());
            psAcc.setInt(3, acc.getAccountId());
            psAcc.executeUpdate();

            con.commit(); // Chốt dữ liệu
            return true;
        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean deleteAccount(int accountId) {
        // Chỉ cập nhật cờ IS_DELETED thành 1 và ghi nhận thời gian xóa
        String sql = "UPDATE ACCOUNT SET IS_DELETED = 1, UPDATED_AT = SYSDATE WHERE ACCOUNT_ID = ?";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);

            int rowAffected = ps.executeUpdate();
            return rowAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AccountModel> getAllAdminAccounts() {
        List<AccountModel> list = new ArrayList<>();
        // Đã bổ sung u.EMAIL và u.USER_TYPE vào câu lệnh SELECT
        String sql = "SELECT a.ACCOUNT_ID, a.USER_ID, a.USERNAME, a.STATUS, "
                + "u.FULL_NAME, u.PHONE_NUMBER, u.EMAIL, u.GENDER, u.BIRTH, u.ADDRESS, u.USER_TYPE, rg.NAME_ROLE_GROUP "
                + "FROM ACCOUNT a "
                + "JOIN APP_USER u ON a.USER_ID = u.USER_ID "
                + "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON aarg.ACCOUNT_ID = a.ACCOUNT_ID "
                + "LEFT JOIN ROLE_GROUP rg ON rg.ROLE_GROUP_ID = aarg.ROLE_GROUP_ID "
                + "WHERE a.IS_DELETED = 0 AND u.USER_TYPE IN ('ADMIN','STAFF') "
                + "ORDER BY a.ACCOUNT_ID DESC"; // Thêm Order By để tài khoản mới tạo lên đầu cho dễ nhìn nha ba

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserModel u = new UserModel();
                u.setFullName(rs.getString("FULL_NAME"));
                u.setSDT(rs.getString("PHONE_NUMBER"));
                u.setEmail(rs.getString("EMAIL"));         // Thêm lấy Email
                u.setAddress(rs.getString("ADDRESS"));
                u.setNgaySinh(rs.getDate("BIRTH"));
                u.setGioiTinh(rs.getString("GENDER"));
                u.setUserType(rs.getString("USER_TYPE"));  // Thêm lấy Loại tài khoản

                RoleGroupModel rgm = new RoleGroupModel();
                rgm.setRoleGroupName(rs.getString("NAME_ROLE_GROUP")); // Null an toàn

                AccountModel acc = new AccountModel(
                        rs.getInt("ACCOUNT_ID"),
                        rs.getInt("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getInt("STATUS")
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

    public int getUserIdByAccountId(int accountId) {
        String sql = "SELECT USER_ID FROM ACCOUNT WHERE ACCOUNT_ID = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("USER_ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
