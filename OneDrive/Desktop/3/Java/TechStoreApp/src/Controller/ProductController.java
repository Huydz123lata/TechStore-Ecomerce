/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.ProductDAO;
import Model.BrandModel;
import Model.CategoryModel;
import Model.ProductModel;
import View.panel.admin.panelSanpham;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HUY0406
 */
public class ProductController {

    private final ProductDAO productDAO = new ProductDAO();

    public void updateDataCategoryCbx(JComboBox comboBox) {
        List<CategoryModel> list = productDAO.getAllCategoryName();
        comboBox.removeAllItems();
        comboBox.addItem("--Chọn danh mục SP--");
        if (list != null) {
            for (CategoryModel c : list) {
                comboBox.addItem(c);
            }
        }
    }

    public void updateDataSPOldCbx(JComboBox cbx) {

        List<ProductModel> list = productDAO.getAllProducts();
        cbx.removeAllItems();
        cbx.addItem("-- Nhập sản phẩm mới --");

        if (list != null) {
            for (ProductModel p : list) {
                cbx.addItem(p);
            }
        }
    }

    public void updateDataBrandCbx(JComboBox comboBox) {
        List<BrandModel> list = productDAO.getAllBrandName();
        comboBox.removeAllItems();
        comboBox.addItem("--Chọn thương hiệu SP--");
        if (list != null) {
            for (BrandModel c : list) {
                comboBox.addItem(c);
            }
        }
    }

    public void addRowToTableNhapHang(JTable table, ProductModel pm) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        DecimalFormat df = new DecimalFormat("#,###");
        model.addRow(new Object[]{
            model.getRowCount() + 1,
            0,
            pm.getName(),
            pm.getStockQuantity(),
            df.format(pm.getPrice()),
            pm.getCategory().getName(),
            pm.getBrand().getName(),
            pm.getWarrantyMonth(),
            pm.getDescription()
        });
    }

    public void deleteRowFromTableNhapHang(JTable table, List<ProductModel> listTam) {
        // 1. Hỏi cái bảng xem người ta đang nhấn chuột vào dòng nào
        int selectedRow = table.getSelectedRow();

        // 2. Nếu nhấn rồi (khác -1) thì mới làm việc
        if (selectedRow >= 0) {
            // 3. Hiện cái bảng hỏi: "Thiệt không ba?"
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Bạn có chắc muốn xóa món này khỏi danh sách chờ không?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // 4. Xóa món đó trong cái túi listTam
                listTam.remove(selectedRow);

                // 5. Bảo cái bảng: "Dữ liệu đổi rồi, vẽ lại đi!"
                refreshTableNhapHang(table, listTam);
            }
        } else {
            // Nếu chưa chọn dòng nào mà đòi xóa thì nhắc nhở
            JOptionPane.showMessageDialog(null, "Bạn phải chọn một dòng trên bảng mới xóa được chứ!");
        }
    }

    public void refreshTableNhapHang(JTable table, List<ProductModel> list) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");
        int stt = 1;
        for (ProductModel pm : list) {
            model.addRow(new Object[]{
                stt++,
                0,
                pm.getName(),
                pm.getStockQuantity(),
                df.format(pm.getPrice()),
                pm.getCategory().getName(),
                pm.getBrand().getName(),
                pm.getWarrantyMonth(),
                pm.getImage(),
                pm.getDescription()
            });
        }
    }

    public void loadAllProductsToTable(JTable table, panelSanpham panel) {
        List<ProductModel> list = productDAO.getAllProducts();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        DecimalFormat df = new DecimalFormat("#,###");

        if (list != null && !list.isEmpty()) {
            for (ProductModel p : list) {
                String trangThaiText = (p.getStatus() == 0) ? "Ngừng kinh doanh"
                        : (p.getStockQuantity() > 0 ? "Đang bán" : "Hết hàng");

                model.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getCategory().getName(),
                    p.getBrand().getName(),
                    df.format(p.getPrice()),
                    p.getStockQuantity(),
                    p.getSoldQuantity(),
                    trangThaiText,
                    p.getImage(),
                    p.getDescription()
                });
            }
        }
        if (panel != null) {
            panel.updateSummaryLabels(list); // Cập nhật Dashboard
            panel.setCurrentList(list);
        }
    }

    public void filterProducts(JTable table, List<ProductModel> currentList, String searchText, String categoryName, String statusText) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");

        if (currentList == null) {
            return;
        }

        for (ProductModel p : currentList) {
            // 1. Kiểm tra Tìm kiếm (Tên sản phẩm)
            boolean matchesSearch = p.getName().toLowerCase().contains(searchText.toLowerCase());

            // 2. Kiểm tra Danh mục (Hiện hết nếu là dòng mặc định)
            boolean matchesCategory = categoryName.equals("--Chọn danh mục SP--")
                    || (p.getCategory() != null && p.getCategory().getName().equals(categoryName));

            // 3. Kiểm tra Trạng thái (Hiện hết nếu là dòng mặc định)
            String pStatus = (p.getStatus() == 0) ? "Ngừng kinh doanh" : (p.getStockQuantity() > 0 ? "Đang bán" : "Hết hàng");
            boolean matchesStatus = statusText.equals("--Lọc theo trạng thái--")
                    || pStatus.equals(statusText);

            // Chỉ thêm vào bảng nếu thỏa mãn cả 3 điều kiện
            if (matchesSearch && matchesCategory && matchesStatus) {
                model.addRow(new Object[]{
                    p.getProductId(), // Cột 0
                    p.getName(), // Cột 1
                    p.getCategory().getName(), // Cột 2
                    p.getBrand().getName(), // Cột 3
                    df.format(p.getPrice()),// Cột 4
                    p.getStockQuantity(), // Cột 5
                    p.getSoldQuantity(), // Cột 6
                    pStatus, // Cột 7
                    p.getImage(), // CỘT 8: THÊM DÒNG NÀY (Để hiện ảnh)
                    p.getDescription() // CỘT 9: THÊM DÒNG NÀY (Để hiện mô tả)
                });
            }
        }
    }

}
