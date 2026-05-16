/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.Date;

/**
 *
 * @author HUY0406
 */
public class CustomerManageModel {

    private int accountId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Date birth;
    private String gender;
    private int orderCount;      // Số đơn hàng đã mua
    private double totalSpent;   // Số tiền đã chi
    private int loyaltyPoints;   // Điểm thành viên
    private String status;

    public CustomerManageModel() {
    }

    public int getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Date getBirth() {
        return birth;
    }

    public String getGender() {
        return gender;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setBirth(Date birth) {
        this.birth = birth;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
