package Controller;

import Service.DonHangService;
import View.panel.admin.panelDonhang;
import java.util.List;
import javax.swing.JOptionPane;

public class DonHangController {

    private final panelDonhang view;
    private final DonHangService service = new DonHangService();

    public DonHangController(panelDonhang view) {
        this.view = view;
    }

    public void loadData() {
        try {
            List<Object[]> dataList = service.getAllOrders();

            view.allData.clear();
            view.allData.addAll(dataList);

            view.totalPages = (int) Math.ceil((double) view.allData.size() / view.rowsPerPage);
            if (view.totalPages == 0) {
                view.totalPages = 1;
            }
            view.currentPage = 1;

            view.renderPage();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu đơn hàng: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleSearch() {
        String keyword = view.myTextField1.getText();
        String trangThaiDon = view.cbxDonhang.getSelectedItem().toString();
        String trangThaiThanhToan = view.cbxThanhtoan.getSelectedItem().toString();

        try {
            List<Object[]> filteredData = service.searchOrders(keyword, trangThaiDon, trangThaiThanhToan);

            view.allData.clear();
            view.allData.addAll(filteredData);

            view.totalPages = (int) Math.ceil((double) view.allData.size() / view.rowsPerPage);
            if (view.totalPages == 0) {
                view.totalPages = 1;
            }
            view.currentPage = 1;
            view.renderPage();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleExportExcel() {
        if (view.allData.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Không có dữ liệu để xuất Excel!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Chỗ này sau này chèn logic xuất file Excel
            JOptionPane.showMessageDialog(view, "Xuất file Excel thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Có lỗi khi xuất file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void updateOrderStatus(int orderId, String newStatus) {
    try {
        // Gọi Service để cập nhật DB
        boolean isSuccess = service.updateOrderStatus(orderId, newStatus);
        
        if (isSuccess) {
            System.out.println("Cập nhật thành công Đơn hàng " + orderId + " -> " + newStatus);
            refreshCurrentPage();
        } else {
            // Thay view bằng null để fix lỗi đỏ
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Không thể cập nhật trạng thái đơn hàng!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            refreshCurrentPage();
        }
    } catch (Exception e) {
        e.printStackTrace();
        // Thay view bằng null để fix lỗi đỏ
        javax.swing.JOptionPane.showMessageDialog(null, 
            "Lỗi hệ thống: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
    }   finally {
        refreshCurrentPage();
    }
}
    public void refreshCurrentPage() {
        String keyword = view.myTextField1.getText();
        String trangThaiDon = view.cbxDonhang.getSelectedItem().toString();
        String trangThaiThanhToan = view.cbxThanhtoan.getSelectedItem().toString();

        try {
            // 1. Lưu lại vị trí trang hiện tại trước khi load data
            int savedPage = view.currentPage; 

            // 2. Lấy lại dữ liệu mới nhất từ Database
            List<Object[]> filteredData = service.searchOrders(keyword, trangThaiDon, trangThaiThanhToan);
            view.allData.clear();
            view.allData.addAll(filteredData);

            // 3. Tính toán lại tổng số trang
            view.totalPages = (int) Math.ceil((double) view.allData.size() / view.rowsPerPage);
            if (view.totalPages == 0) {
                view.totalPages = 1;
            }

            // 4. Phục hồi lại trang cũ (Kiểm tra an toàn: nếu data bị xóa bớt làm thụt trang thì lùi về trang cuối)
            if (savedPage > view.totalPages) {
                view.currentPage = view.totalPages;
            } else {
                view.currentPage = savedPage; 
            }

            // 5. Vẽ lại bảng
            view.renderPage();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    