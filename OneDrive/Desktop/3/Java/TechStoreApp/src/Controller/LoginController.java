/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Model.AccountModel;
import Model.UserModel;
import Service.AuthService;
import Util.HashUtil;
import Util.TokenMonitorManager;
import View.User.AdminForm;
import View.User.CustomerForm;
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
        // Mã hóa mật khẩu trước khi gửi đi kiểm tra
        String passwordHash = HashUtil.hashPassword(password);

        // Gọi service xử lý đăng nhập
        AccountModel acc = service.executeLogin(username, passwordHash);

        if (acc != null) {
            JOptionPane optionPane = new JOptionPane("Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog("Thành công");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);

            UserModel userInfo = acc.getUserInfo();

            if (userInfo != null) {
                // Nếu có userInfo, tiến hành lấy role và mở form tương ứng
                String role = userInfo.getUserType();

                if ("admin".equalsIgnoreCase(role)) {
                    AdminForm adminForm = new AdminForm();
                    adminForm.setVisible(true);
                } else {
                    // Nếu là customer hoặc các quyền khác
                    CustomerForm customerForm = new CustomerForm();
                    customerForm.setVisible(true);
                }

                TokenMonitorManager.start();
                view.dispose(); // Đóng form đăng nhập

            } else {
                // NẾU RƠI VÀO ĐÂY: Có nghĩa là đăng nhập đúng nhưng UserInfo bị null
                JOptionPane.showMessageDialog(null,
                        "Lỗi hệ thống: Tài khoản hợp lệ nhưng không tìm thấy thông tin chi tiết (UserInfo bị null).\n"
                        + "Vui lòng kiểm tra lại file AuthDAO.java!",
                        "Cảnh báo Dữ Liệu",
                        JOptionPane.WARNING_MESSAGE);
            }

        } else {
            JOptionPane optionPane = new JOptionPane("Đăng nhập thất bại. Vui lòng kiểm tra lại!", JOptionPane.ERROR_MESSAGE);
            JDialog dialog = optionPane.createDialog("Thất Bại");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
        }
    }

}
