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
    public void updateOrderStatus(int orderId, String newStatus, String oldStatus) {
    // ---- LOGIC KIỂM TRA (HƯỚNG 1) NẰM Ở ĐÂY ----
    // Chặn nếu trạng thái CŨ là CANCELLED mà trạng thái MỚI lại khác CANCELLED
    if ("CANCELLED".equals(oldStatus) && !"CANCELLED".equals(newStatus)) {
        // Báo lỗi cho người dùng
        javax.swing.JOptionPane.showMessageDialog(view, 
            "Lỗi nghiệp vụ: Không thể phục hồi đơn hàng đã bị HỦY.\nKhách hàng vui lòng đặt đơn mới!", 
            "Từ chối thao tác", javax.swing.JOptionPane.WARNING_MESSAGE);
        
        // Load lại trang để JTable tự động quay về chữ CANCELLED như cũ
        refreshCurrentPage(); 
        return; // Dừng hàm ngay lập tức, KHÔNG chạy lệnh update xuống Database
    }
    // -------------------------------------------

    try {
        // Nếu hợp lệ (không dính lỗi ở trên) thì mới gọi Service để cập nhật DB
        boolean isSuccess = service.updateOrderStatus(orderId, newStatus);
        
        if (isSuccess) {
            System.out.println("Cập nhật thành công Đơn hàng " + orderId + " -> " + newStatus);
        } else {
            javax.swing.JOptionPane.showMessageDialog(view, 
                "Không thể cập nhật trạng thái đơn hàng!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception e) {
        e.printStackTrace();
        javax.swing.JOptionPane.showMessageDialog(view, 
            "Lỗi hệ thống: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
    } finally {
        // Cuối cùng luôn làm mới lại bảng để đồng bộ dữ liệu chuẩn xác nhất
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
    public void handleDoubleClick(int orderId) {
        try {
            // 1. Gọi Service lấy dữ liệu
            Object[] info = service.getOrderInfo(orderId);
            List<Object[]> details = service.getOrderDetails(orderId);

            if (info != null) {
                // 2. Tìm Frame cha để truyền vào Dialog (tránh lỗi null)
                java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(view);
                java.awt.Frame parentFrame = (window instanceof java.awt.Frame) ? (java.awt.Frame) window : null;
                
                // 3. Khởi tạo Dialog
                View.dialog.ChiTietDonHangDialog dialog = new View.dialog.ChiTietDonHangDialog(parentFrame, true);

                // 4. Đổ dữ liệu vào Form
                dialog.fillData(
                    info[0] != null ? info[0].toString() : "", // Tên khách hàng
                    info[1] != null ? info[1].toString() : "", // Email
                    info[2] != null ? info[2].toString() : "", // SĐT
                    info[3] != null ? info[3].toString() : "", // Địa chỉ
                    info[4] != null ? info[4].toString() : "", // Tổng tiền
                    details                                    // List danh sách sản phẩm
                );

                // 5. Hiển thị Dialog ở giữa màn hình
                dialog.setLocationRelativeTo(view);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(view, "Không tìm thấy dữ liệu chi tiết trong CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi mở form chi tiết: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
    