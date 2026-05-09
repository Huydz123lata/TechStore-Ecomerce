package View.dialog;

import Model.AccountModel;
import Model.UserModel;
import static java.awt.AWTEventMulticaster.add;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AccountDialog extends JDialog {

    // Các trường nhập liệu
    private JTextField txtFullName, txtPhone, txtAddress, txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbxUserType;
    private JButton btnSave, btnCancel;
    private boolean isSucceeded = false;

    public AccountDialog(Frame parent) {
        super(parent, "Thêm Tài Khoản Mới", true); // true để làm modal dialog
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Khởi tạo component
        txtFullName = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        txtUsername = new JTextField();
        txtPassword = new JPasswordField("123456"); // Mặc định
        cbxUserType = new JComboBox<>(new String[]{"STAFF", "ADMIN", "CUSTOMER"});

        // Thêm vào panel
        panel.add(new JLabel("Họ và tên:"));
        panel.add(txtFullName);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(txtPhone);
        panel.add(new JLabel("Địa chỉ:"));
        panel.add(txtAddress);
        panel.add(new JLabel("Tên đăng nhập:"));
        panel.add(txtUsername);
        panel.add(new JLabel("Mật khẩu:"));
        panel.add(txtPassword);
        panel.add(new JLabel("Vai trò:"));
        panel.add(cbxUserType);

        // Nút bấm
        JPanel btnPanel = new JPanel();
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {
            if (validateInput()) {
                isSucceeded = true;
                dispose();
            }
        });

        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        add(panel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private boolean validateInput() {
        if (txtFullName.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên và Tên đăng nhập không được để trống!");
            return false;
        }
        return true;
    }

    // Getter để lấy dữ liệu ra ngoài
    public AccountModel getAccountData() {
        UserModel user = new UserModel();
        user.setFullName(txtFullName.getText());
        user.setSDT(txtPhone.getText());
        user.setAddress(txtAddress.getText());
        user.setUserType(cbxUserType.getSelectedItem().toString());

        AccountModel acc = new AccountModel();
        acc.setUserInfo(user);
        acc.setUsername(txtUsername.getText());
        acc.setPasswordHash(new String(txtPassword.getPassword()));
        acc.setStatus("ACTIVE");

        return acc;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }
}
