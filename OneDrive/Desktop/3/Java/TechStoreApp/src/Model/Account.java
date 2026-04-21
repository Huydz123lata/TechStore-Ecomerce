/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author HUY0406
 */
public class Account {

    private int accountId;
    private int userId;
    private String username;
    private String password;
    private String status;
    public User userInfo;

    // Constructor 1: Dùng cho ĐĂNG KÝ
    public Account(User userInfo, String username, String password) {
        this.userInfo = userInfo;
        this.username = username;
        this.password = password;
    }

    // Constructor 2: Dùng cho ĐĂNG NHẬP 
    public Account(int accountId, int userId, String username, String status) {
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

    public User getUserInfo() {
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

    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }

}
