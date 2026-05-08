/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author HUY0406
 */
public class AccountModel {

    private int accountId;
    private int userId;
    private String username;
    private String password;
    private String status;
    private RoleGroupModel roleGroup;
    private RoleModel role;
    private UserModel userInfo;

    public AccountModel() {
    }

    // Constructor 1: Dùng cho ĐĂNG KÝ
    public AccountModel(UserModel userInfo, String username, String password) {
        this.userInfo = userInfo;
        this.username = username;
        this.password = password;
    }

    // Constructor 2: Dùng cho ĐĂNG NHẬP 
    public AccountModel(int accountId, int userId, String username, String status) {
        this.accountId = accountId;
        this.userId = userId;
        this.username = username;
        this.status = status;
    }

    //getter && setter
    public int getAccountId() {
        return accountId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public UserModel getUserInfo() {
        return userInfo;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserInfo(UserModel userInfo) {
        this.userInfo = userInfo;
    }

    public RoleGroupModel getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(RoleGroupModel roleGroup) {
        this.roleGroup = roleGroup;
    }

    public RoleModel getRole() {
        return role;
    }

    public void setRole(RoleModel role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return this.username;
    }

}
