/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.text.SimpleDateFormat;
import java.sql.Date;

/**
 *
 * @author HUY0406
 */
public class User {

    private int userId;
    private String fullName;
    private String SDT;
    private Date ngaySinh;
    private String gioiTinh;

    public User() {
    }

    // Constructor cho đăng ký 
    public User(String fullName, String SDT, String gioiTinh) {
        this.fullName = fullName;
        this.SDT = SDT;
        this.gioiTinh = gioiTinh;
    }

    // Constructor đầy đủ
    public User(int userId, String fullName, String email, String SDT) {
        this.userId = userId;
        this.fullName = fullName;
        this.SDT = SDT;
    }

    // --- HÀM XỬ LÝ NGÀY SINH TỪ COMBOBOX ---
    public void setNgaySinhFromPicker(String ngay, String thang, String nam) {
        try {
            String dateStr = ngay + "/" + thang + "/" + nam;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false); // Chặn ngày không hợp lệ (VD: 31/02)
            java.util.Date uDate = sdf.parse(dateStr);
            this.ngaySinh = new java.sql.Date(uDate.getTime());
        } catch (Exception e) {
            this.ngaySinh = null;
        }
    }

    // --- GETTERS & SETTERS ---
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSDT() {
        return SDT;
    }

    public void setSDT(String SDT) {
        this.SDT = SDT;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }
}
