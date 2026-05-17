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
        View.dialog.panelFormKhuyenMai form = new View.dialog.panelFormKhuyenMai(this, dialog);
        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void handleDelete() {
        // Trực tiếp gọi thẳng vào bảng tblKhuyenmai của view
        int selectedRow = view.tblCoupon.getSelectedRow();
        
        // Nếu người dùng chưa chọn dòng nào (selectedRow = -1)
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Vui lòng click chọn một mã giảm giá trên bảng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy mã giảm giá ở cột số 0 của dòng đang được chọn
        String promoCode = view.tblCoupon.getValueAt(selectedRow, 0).toString();
        
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

    public boolean createNewPromotion(String code, String name, String type, double value, double minOrder, double maxDiscount, java.util.Date startDate, java.util.Date endDate, String status) {
        try {
            service.addPromotion(code, name, type, value, minOrder, maxDiscount, startDate, endDate, status);
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
    public void loadDataPro() {
        try {
            java.util.List<Object[]> dataList = service.getAllPro();
            view.allDataPro.clear();
            view.allDataPro.addAll(dataList);
            view.totalPagesPro = (int) Math.ceil((double) view.allDataPro.size() / view.rowsPerPage);
            if (view.totalPagesPro == 0) view.totalPagesPro = 1;
            view.currentPagePro = 1;
            view.renderPagePro();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu Promotion: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleSearchPro() {
        String keyword = view.myTextField2.getText().trim();
        String status = view.cbxKhuyenmai1.getSelectedItem().toString();
        try {
            java.util.List<Object[]> filteredData = service.searchPro(keyword, status);
            view.allDataPro.clear();
            view.allDataPro.addAll(filteredData);
            view.totalPagesPro = (int) Math.ceil((double) view.allDataPro.size() / view.rowsPerPage);
            if (view.totalPagesPro == 0) view.totalPagesPro = 1;
            view.currentPagePro = 1;
            view.renderPagePro();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(view, "Lỗi tìm kiếm Promotion: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshCurrentPagePro() {
        String keyword = view.myTextField2.getText().trim();
        String status = view.cbxKhuyenmai1.getSelectedItem().toString();
        try {
            int savedPage = view.currentPagePro; 
            java.util.List<Object[]> filteredData = service.searchPro(keyword, status);
            view.allDataPro.clear();
            view.allDataPro.addAll(filteredData);
            view.totalPagesPro = (int) Math.ceil((double) view.allDataPro.size() / view.rowsPerPage);
            if (view.totalPagesPro == 0) view.totalPagesPro = 1;

            if (savedPage > view.totalPagesPro) view.currentPagePro = view.totalPagesPro;
            else view.currentPagePro = savedPage; 
            
            view.renderPagePro();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void updateProStatus(String proCode, String newStatus) {
        if ("CANCELLED".equals(newStatus)) {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(null, 
                "CẢNH BÁO: Hủy chương trình khuyến mãi này?\nCác sản phẩm áp dụng sẽ trở về giá gốc.", 
                "Xác nhận Hủy", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                refreshCurrentPagePro(); return; 
            }
        }
        try {
            boolean isSuccess = service.updateProStatus(proCode, newStatus);
            if (!isSuccess) javax.swing.JOptionPane.showMessageDialog(null, "Cập nhật trạng thái thất bại!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            refreshCurrentPagePro();
        }
    }

    public void handleDeletePro() {
        javax.swing.JTable table = view.tblPromotion;
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(view, "Chọn một chương trình trên bảng để xóa!", "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String proCode = table.getValueAt(selectedRow, 0).toString();
        int confirm = javax.swing.JOptionPane.showConfirmDialog(view, 
            "Chắc chắn muốn đưa chương trình [" + proCode + "] vào thùng rác?", 
            "Xác nhận Xóa", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE);
            
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                if (service.deletePro(proCode)) {
                    javax.swing.JOptionPane.showMessageDialog(view, "Xóa thành công!", "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    refreshCurrentPagePro();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(view, "Không thể xóa!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(view, "Lỗi: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void handleAddPro() {
        javax.swing.JOptionPane.showMessageDialog(view, 
            "Mở Form Tạo Chương Trình Mới (Gồm thông tin chương trình và danh sách Sản phẩm áp dụng)", 
            "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        // Ở bước tiếp theo, ta sẽ thay dòng này bằng lệnh bật Dialog chứa cái Form Master-Detail mà tôi đã tư vấn
    }
    // 1. Hàm bật Form Sửa và đẩy dữ liệu lên
    public void handleEdit() {
        int selectedRow = view.tblCoupon.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Vui lòng click chọn một mã giảm giá trên bảng để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Bóc tách dữ liệu từ giao diện bảng (JTable)
        String code = view.tblCoupon.getValueAt(selectedRow, 0).toString();
        String desc = view.tblCoupon.getValueAt(selectedRow, 1).toString();
        String type = view.tblCoupon.getValueAt(selectedRow, 2).toString();

        // Loại bỏ các ký tự thừa như " VNĐ", " %", dấu phẩy hàng nghìn
        String valueStr = view.tblCoupon.getValueAt(selectedRow, 3).toString().replaceAll("[^\\d]", "");
        String minStr = view.tblCoupon.getValueAt(selectedRow, 4).toString().replaceAll("[^\\d]", "");
        String maxStr = view.tblCoupon.getValueAt(selectedRow, 5).toString().replaceAll("[^\\d]", "");

        double value = valueStr.isEmpty() ? 0 : Double.parseDouble(valueStr);
        double min = minStr.isEmpty() ? 0 : Double.parseDouble(minStr);
        double max = maxStr.isEmpty() ? 0 : Double.parseDouble(maxStr);

        java.util.Date start = (java.util.Date) view.tblCoupon.getValueAt(selectedRow, 6);
        java.util.Date end = (java.util.Date) view.tblCoupon.getValueAt(selectedRow, 7);

        // Khởi tạo Dialog và Form Sửa chuyên biệt
        javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) null, "Cập Nhật Khuyến Mãi", true);
        View.dialog.panelFormKhuyenMaiChange form = new View.dialog.panelFormKhuyenMaiChange(this, dialog);
        
        form.setEditData(code, desc, type, value, min, max, start, end); 
        
        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(view);
        dialog.setVisible(true);
    }
    
    // 2. Hàm xử lý trung gian đẩy lệnh Sửa xuống tầng Service/DAO
    public boolean processUpdateCoupon(String oldCode, String newCode, String name, String type, double value, double minOrder, double maxDiscount, java.util.Date startDate, java.util.Date endDate) {
        try {
            boolean success = service.updateCouponData(oldCode, newCode, name, type, value, minOrder, maxDiscount, startDate, endDate);
            if (success) {
                refreshCurrentPage(); // Ép load lại bảng ngay lập tức
            } else {
                JOptionPane.showMessageDialog(null, "Không thể cập nhật! Có thể do Mã mới đã bị trùng với một Mã khác trong hệ thống.", "Lỗi Cập Nhật", JOptionPane.ERROR_MESSAGE);
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

}