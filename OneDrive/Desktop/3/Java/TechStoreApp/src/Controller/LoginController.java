/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Model.AccountModel;
import Service.AuthService;
import Util.HashUtil;
import Util.TokenMonitorManager;
import View.admin.AdminForm;
import View.auth.LoginForm;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author HUY0406
 */
public class LoginController {

    private LoginForm view;
    private AuthService service = new AuthService();

    public LoginController(LoginForm view) {
        this.view = view;
    }

    public void handleLogin() {
        String username = view.userNameField.getText();
        String password = view.passwordField.getText();
        String passwordHash = HashUtil.hashPassword(password);

        AccountModel acc = service.executeLogin(username, passwordHash);

        if (acc != null) {
            JOptionPane optionPane = new JOptionPane("Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog("Thành công");

            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);

            AdminForm admin = new AdminForm();
            admin.setVisible(true);

            TokenMonitorManager.start();
            view.dispose();

        } else {
            JOptionPane optionPane = new JOptionPane("Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            JDialog dialog = optionPane.createDialog("Thất Bại");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
        }

    }

}
