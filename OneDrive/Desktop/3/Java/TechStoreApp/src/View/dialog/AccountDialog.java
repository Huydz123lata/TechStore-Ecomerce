package View.dialog;

import Model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Calendar;

public class AccountDialog extends JDialog {

    private JTextField txtFullName, txtSDT, txtAddress, txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbxUserType, cbxGender, cbxDay, cbxMonth, cbxYear;
    private JButton btnSave, btnCancel;
    private boolean isSucceeded = false;
    private AccountModel accountData;

    private final Color TECH_ORANGE = new Color(255, 102, 0);

    public AccountDialog(Frame parent) {
        super(parent, "QUẢN LÝ TÀI KHOẢN", true);
        initComponents();
    }

    // --- HÀM NÀY XÀI KHI BẤM NÚT SỬA ---
    public void setModel(AccountModel acc) {
        this.setTitle("CẬP NHẬT TÀI KHOẢN: " + acc.getUsername().toUpperCase());

        // Đổ dữ liệu từ Model vào các JTextField
        txtFullName.setText(acc.getUserInfo().getFullName());
        txtSDT.setText(acc.getUserInfo().getSDT());
        txtAddress.setText(acc.getUserInfo().getAddress());
        txtUsername.setText(acc.getUsername());

        // Khóa Username không cho sửa (thông lệ quản lý account)
        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(240, 240, 240));

        // Đổ dữ liệu vào ComboBox
        cbxGender.setSelectedItem(acc.getUserInfo().getGioiTinh());
        cbxUserType.setSelectedItem(acc.getUserInfo().getUserType());

        // Xử lý tách ngày sinh đổ vào 3 ComboBox
        if (acc.getUserInfo().getNgaySinh() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(acc.getUserInfo().getNgaySinh());

            cbxDay.setSelectedItem(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
            cbxMonth.setSelectedItem(String.format("%02d", cal.get(Calendar.MONTH) + 1));
            cbxYear.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));
        }

        // Đổi chữ trên nút bấm
        btnSave.setText("CẬP NHẬT TÀI KHOẢN");
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = new JPanel();
        header.setBackground(TECH_ORANGE);
        JLabel title = new JLabel("THÔNG TIN TÀI KHOẢN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // --- Form ---
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 5, 10, 5);

        // Inputs
        txtFullName = createStyledField();
        txtSDT = createStyledField();
        txtAddress = createStyledField();
        txtUsername = createStyledField();
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(250, 35));

        cbxGender = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cbxUserType = new JComboBox<>(new String[]{"STAFF", "ADMIN", "CUSTOMER"});

        // Birth Date Picker
        JPanel pnlBirth = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlBirth.setBackground(Color.WHITE);
        cbxDay = new JComboBox<>();
        cbxMonth = new JComboBox<>();
        cbxYear = new JComboBox<>();

        for (int i = 1; i <= 31; i++) {
            cbxDay.addItem(String.format("%02d", i));
        }
        for (int i = 1; i <= 12; i++) {
            cbxMonth.addItem(String.format("%02d", i));
        }
        for (int i = 2026; i >= 1970; i--) {
            cbxYear.addItem(String.valueOf(i));
        }

        pnlBirth.add(cbxDay);
        pnlBirth.add(new JLabel("/"));
        pnlBirth.add(cbxMonth);
        pnlBirth.add(new JLabel("/"));
        pnlBirth.add(cbxYear);

        // Add rows
        int r = 0;
        addRow(main, "Họ và tên:", txtFullName, r++, g);
        addRow(main, "Số điện thoại:", txtSDT, r++, g);
        addRow(main, "Địa chỉ:", txtAddress, r++, g);
        addRow(main, "Ngày sinh:", pnlBirth, r++, g);
        addRow(main, "Giới tính:", cbxGender, r++, g);
        addRow(main, "Tên đăng nhập:", txtUsername, r++, g);
        addRow(main, "Mật khẩu:", txtPassword, r++, g);
        addRow(main, "Admin / khách hàng:", cbxUserType, r++, g);

        add(main, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btnSave = new JButton("LƯU TÀI KHOẢN");
        btnSave.setBackground(TECH_ORANGE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCancel = new JButton("HỦY");

        btnSave.addActionListener(e -> {
            if (txtFullName.getText().isEmpty() || txtUsername.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ tên và Username!");
                return;
            }

            UserModel u = new UserModel();
            u.setFullName(txtFullName.getText().trim());
            u.setSDT(txtSDT.getText().trim());
            u.setAddress(txtAddress.getText().trim());
            u.setGioiTinh(cbxGender.getSelectedItem().toString());
            u.setUserType(cbxUserType.getSelectedItem().toString());
            u.setNgaySinhFromPicker(
                    cbxDay.getSelectedItem().toString(),
                    cbxMonth.getSelectedItem().toString(),
                    cbxYear.getSelectedItem().toString()
            );

            accountData = new AccountModel();
            accountData.setUserInfo(u);
            accountData.setUsername(txtUsername.getText().trim());
            accountData.setPasswordHash(new String(txtPassword.getPassword()));
            accountData.setStatus(1); // Mặc định khi lưu/sửa

            isSucceeded = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());
        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void addRow(JPanel p, String lbl, JComponent c, int y, GridBagConstraints g) {
        g.gridx = 0;
        g.gridy = y;
        g.weightx = 0.3;
        p.add(new JLabel(lbl), g);
        g.gridx = 1;
        g.weightx = 0.7;
        p.add(c, g);
    }

    private JTextField createStyledField() {
        JTextField f = new JTextField(20);
        f.setPreferredSize(new Dimension(250, 35));
        return f;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public AccountModel getAccountData() {
        return accountData;
    }
}
