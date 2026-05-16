/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.CustomerDAO;
import Model.CustomerManageModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class CustomerController {

    private CustomerDAO customerDAO = new CustomerDAO();

    public void loadDataToTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        List<CustomerManageModel> list = customerDAO.getAllCustomers();

        DecimalFormat dfMoney = new DecimalFormat("#,### VND");
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

        for (CustomerManageModel c : list) {
            // Định dạng ngày sinh (kiểm tra null vì có thể khách không nhập)
            String birthStr = (c.getBirth() != null) ? sdfDate.format(c.getBirth()) : "Chưa cập nhật";

            // Nếu giới tính bị null
            String genderStr = (c.getGender() != null) ? c.getGender() : "Khác";

            // Add vào mảng Object CỰC KỲ chuẩn xác theo 11 cột trong Customizer của ba:
            model.addRow(new Object[]{
                c.getAccountId(), // 0: AccountID
                c.getUsername(), // 1: Tên đăng nhập
                c.getFullName(), // 2: Họ và tên
                c.getEmail(), // 3: Email
                c.getPhoneNumber(), // 4: SĐT
                birthStr, // 5: Ngày sinh
                genderStr, // 6: Giới tính
                c.getOrderCount(), // 7: Số đơn hàng đã mua
                dfMoney.format(c.getTotalSpent()), // 8: Số tiền đã chi
                c.getLoyaltyPoints(), // 9: Điểm thành viên
                c.getStatus() // 10: Trạng thái
            });
        }
    }

    public void filterCustomers(JTable table, List<CustomerManageModel> currentList, String searchText) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        DecimalFormat df = new DecimalFormat("#,### VNĐ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (currentList == null) {
            return;
        }

        String search = (searchText == null) ? "" : searchText.toLowerCase().trim();

        for (CustomerManageModel c : currentList) {
            // Chỉ lọc theo Tên, Username hoặc Số điện thoại
            boolean matchesSearch = c.getFullName().toLowerCase().contains(search)
                    || c.getUsername().toLowerCase().contains(search)
                    || c.getPhoneNumber().contains(search);

            if (matchesSearch) {
                model.addRow(new Object[]{
                    c.getAccountId(),
                    c.getUsername(),
                    c.getFullName(),
                    c.getEmail(),
                    c.getPhoneNumber(),
                    c.getBirth() != null ? sdf.format(c.getBirth()) : "",
                    c.getGender(),
                    c.getOrderCount(),
                    df.format(c.getTotalSpent()),
                    c.getLoyaltyPoints(),
                    c.getStatus()
                });
            }
        }
    }

}
