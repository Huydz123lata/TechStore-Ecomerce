package Controller;

import Service.KhuyenMaiService;
import View.panel.admin.panelKhuyenmai;
import java.util.List;
import javax.swing.JOptionPane;

public class KhuyenMaiController {

    private final panelKhuyenmai view;
    private final KhuyenMaiService service = new KhuyenMaiService();

    public KhuyenMaiController(panelKhuyenmai view) {
        this.view = view;
    }

    public void loadData() {
        try {
            List<Object[]> dataList = service.getAllPromotions();
            view.allData.clear();
            view.allData.addAll(dataList);
            view.totalPages = (int) Math.ceil((double) view.allData.size() / view.rowsPerPage);
            if (view.totalPages == 0) view.totalPages = 1;
            view.currentPage = 1;
            view.renderPage();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleSearch() {
        String keyword = view.myTextField1.getText().trim();
        String status = view.cbxKhuyenmai.getSelectedItem().toString();
        try {
            List<Object[]> filteredData = service.searchPromotions(keyword, status);
            view.allData.clear();
            view.allData.addAll(filteredData);
            view.totalPages = (int) Math.ceil((double) view.allData.size() / view.rowsPerPage);
            if (view.totalPages == 0) view.totalPages = 1;
            view.currentPage = 1;
            view.renderPage();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshCurrentPage() {
        String keyword = view.myTextField1.getText().trim();
        String status = view.cbxKhuyenmai.getSelectedItem().toString();
        try {
            int savedPage = view.currentPage; 
            List<Object[]> filteredData = service.searchPromotions(keyword, status);
            view.allData.clear();
            view.allData.addAll(filteredData);
            view.totalPages = (int) Math.ceil((double) view.allData.size() / view.rowsPerPage);
            if (view.totalPages == 0) view.totalPages = 1;

            if (savedPage > view.totalPages) view.currentPage = view.totalPages;
            else view.currentPage = savedPage; 
            
            view.renderPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePromotionStatus(String promoCode, String newStatus) {
        if ("CANCELLED".equals(newStatus)) {
            int confirm = JOptionPane.showConfirmDialog(null, 
                "CẢNH BÁO: Bạn có chắc chắn muốn hủy mã này?\nKhách hàng sẽ không thể sử dụng mã này nữa.", 
                "Xác nhận Hủy Mã", 
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                refreshCurrentPage(); 
                return; 
            }
        }
        try {
            boolean isSuccess = service.updatePromotionStatus(promoCode, newStatus);
            if (!isSuccess) JOptionPane.showMessageDialog(null, "Không thể cập nhật trạng thái!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            refreshCurrentPage();
        }
    }

    public void handleAdd() {
        javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) null, "Tạo Khuyến Mãi Mới", true);
        View.panel.admin.panelFormKhuyenMai form = new View.panel.admin.panelFormKhuyenMai(this, dialog);
        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void handleDelete() {
        // Trực tiếp gọi thẳng vào bảng tblKhuyenmai của view
        int selectedRow = view.tblKhuyenmai.getSelectedRow();
        
        // Nếu người dùng chưa chọn dòng nào (selectedRow = -1)
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Vui lòng click chọn một mã giảm giá trên bảng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy mã giảm giá ở cột số 0 của dòng đang được chọn
        String promoCode = view.tblKhuyenmai.getValueAt(selectedRow, 0).toString();
        
        // Hiển thị hộp thoại xác nhận xóa
        int confirm = JOptionPane.showConfirmDialog(view, 
            "CẢNH BÁO: Bạn có chắc chắn muốn xóa mã [" + promoCode + "] không?\nDữ liệu sẽ bị đưa vào thùng rác hệ thống.", 
            "Xác nhận Xóa", 
            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            
        // Xử lý Xóa nếu người dùng chọn YES
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean isSuccess = service.deletePromotion(promoCode);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(view, "Đã xóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refreshCurrentPage(); // F5 tải lại bảng ngay lập tức
                } else {
                    JOptionPane.showMessageDialog(view, "Không thể xóa mã giảm giá này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(view, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean createNewPromotion(String code, String name, String type, double value, java.util.Date startDate, java.util.Date endDate, String status) {
        try {
            service.addPromotion(code, name, type, value, startDate, endDate, status);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("ORA-00001") || e.getMessage().contains("unique constraint")) {
                JOptionPane.showMessageDialog(null, "Mã khuyến mãi '" + code + "' đã tồn tại. Vui lòng nhập mã khác!", "Lỗi Trùng Mã", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }
}