/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Service.AuthService;
import Util.HashUtil;
import View.auth.LoginForm;
import View.auth.RegisterForm;
import javax.swing.JOptionPane;

/**
 *
 * @author HUY0406
 */
public class RegisterController {

    private RegisterForm view;
    private AuthService service = new AuthService();

    public RegisterController(RegisterForm view) {
        this.view = view;
    }

    public void handleRegister() {

        String name = view.nameField.getText();
        String user = view.userNameField.getText();
        String pass = view.passwordField.getText();
        String pass2 = view.password2Field.getText();
        String sdt = view.phoneNumberField.getText();

        String gioiTinh = "Nam";
        if (view.rbNu.isSelected()) {
            gioiTinh = "Nu";
        }

        String ngay = view.cboNgay.getSelectedItem().toString();
        String thang = view.cboThang.getSelectedItem().toString();
        String nam = view.cboNam.getSelectedItem().toString();

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || sdt.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(view, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!(pass2.equals(pass))) {
            javax.swing.JOptionPane.showMessageDialog(view, "Mật khẩu nhập lại không khớp");
            return;
        }

        String passwordHash = HashUtil.hashPassword(pass);
        try {
            AuthService registerProcess = new AuthService();
            registerProcess.excuteRegister(name, sdt, gioiTinh, ngay, thang, nam, user, passwordHash);

            JOptionPane.showMessageDialog(view, "Đăng ký thành công!");
            LoginForm login = new LoginForm();
            login.setVisible(true);
            view.dispose();

        } catch (java.sql.SQLException e) {
            if (e.getErrorCode() == 1) {
                JOptionPane.showMessageDialog(view, "Số điện thoại này đã tồn tại trong hệ thống!");
            } else {
                JOptionPane.showMessageDialog(view, "Lỗi Database: " + e.getMessage());
            }
        } catch (Exception e) {
            // Các lỗi code khác
            JOptionPane.showMessageDialog(view, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

}
