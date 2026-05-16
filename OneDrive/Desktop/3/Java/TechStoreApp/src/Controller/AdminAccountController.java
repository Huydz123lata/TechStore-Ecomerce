package Controller;

import DAO.AccountDAO;
import Model.AccountModel;
import Model.UserModel;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;

public class AdminAccountController {

    private AccountDAO dao = new AccountDAO();

    public void loadDataToTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Xóa trắng dữ liệu cũ trên bảng

        // Lấy danh sách từ DAO
        List<AccountModel> list = dao.getAllAdminAccounts();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (AccountModel acc : list) {
            UserModel user = acc.getUserInfo();

            // Xử lý dữ liệu hiển thị
            String ngaySinh = (user.getNgaySinh() != null) ? sdf.format(user.getNgaySinh()) : "";
            String vaiTro = (acc.getRoleGroup() != null && acc.getRoleGroup().getRoleGroupName() != null)
                    ? acc.getRoleGroup().getRoleGroupName() : "Chưa phân quyền";
            String trangThai = (acc.getStatus() == 1) ? "Hoạt động" : "Bị khóa";

            // Thêm hàng mới vào model khớp với thứ tự 12 cột trong thiết kế
            model.addRow(new Object[]{
                acc.getUsername(), // 0: Tên đăng nhập
                user.getFullName(), // 1: Họ và tên
                user.getSDT(), // 2: SĐT
                user.getEmail(), // 3: Email
                user.getAddress(), // 4: Địa chỉ
                ngaySinh, // 5: Ngày sinh
                user.getGioiTinh(), // 6: Giới tính
                user.getUserType(), // 7: Loại tài khoản
                vaiTro, // 8: Vai trò
                trangThai, // 9: Trạng thái
                acc.getAccountId(), // 10: AccountID (Ẩn hoặc hiện tùy ba chỉnh width)
                user.getUserId() // 11: userId (Ẩn)
            });
        }
    }

}
