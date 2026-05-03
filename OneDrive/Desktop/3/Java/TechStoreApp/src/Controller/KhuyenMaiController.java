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
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu khuyến mãi: " + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
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
                "CẢNH BÁO: Bạn có chắc chắn muốn dừng sớm chương trình khuyến mãi này?", 
                "Xác nhận Dừng Khuyến mãi", 
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

    public void handleExportExcel() {
        if (view.allData.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Không có dữ liệu để xuất Excel!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(view, "Chức năng xuất Excel đang được xây dựng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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