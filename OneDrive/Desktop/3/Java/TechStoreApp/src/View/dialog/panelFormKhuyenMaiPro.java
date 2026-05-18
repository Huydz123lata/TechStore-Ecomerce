/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.dialog;

import Controller.KhuyenMaiController;
import Controller.ProductController;
import DAO.ProductDAO;
import Model.ProductModel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;



public class panelFormKhuyenMaiPro extends javax.swing.JPanel {
    private final KhuyenMaiController parentController;
    private final javax.swing.JDialog parentDialog;
    
    private ProductController productController = new ProductController();
    private ProductDAO productDAO = new ProductDAO();
    
    private List<ProductModel> currentList;
    
    // ĐÃ ĐỔI: Dùng Map để lưu [Mã Sản phẩm] đi kèm [Mức Giảm Giá]
    private Map<Integer, Double> selectedProductsMap = new HashMap<>();

    public panelFormKhuyenMaiPro(KhuyenMaiController controller, javax.swing.JDialog dialog) {
        initComponents();
        this.parentController = controller;
        this.parentDialog = dialog;
        
        lblMaKM.setText("Tên Chương trình:");
        
        productController.updateDataCategoryCbx(cbxCategory);
        currentList = productDAO.getAllProducts();
        
        executeFilter();
        setupTableListener(); // Đổi tên hàm cho chuẩn vì giờ ta lắng nghe cả cột 2 và 3
    }

    private void closeForm() {
        if (parentDialog != null) {
            parentDialog.dispose();
        } else {
            SwingUtilities.getWindowAncestor(this).dispose();
        }
    }

    private void handleSave() {
        String promoName = txtPromoCode.getText().trim();
        
        if (promoName.isEmpty() || dateStart.getDate() == null || dateEnd.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên chương trình và Ngày tháng!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dateEnd.getDate().before(dateStart.getDate()) || dateEnd.getDate().equals(dateStart.getDate())) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải lớn hơn Ngày bắt đầu!", "Lỗi ngày tháng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Bắt buộc phải có SP trong Map
        if (selectedProductsMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mức giảm và tích chọn ít nhất 1 sản phẩm!", "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gọi Controller xử lý và truyền thẳng Map chứa SP+Mức giảm qua
        if (parentController != null) {
            boolean success = parentController.createNewPromotionPro(promoName, dateStart.getDate(), dateEnd.getDate(), selectedProductsMap);
            if (success) {
                JOptionPane.showMessageDialog(this, "Tạo chương trình Khuyến mãi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                closeForm();
            }
        }
    }

    private void executeFilter() {
        String search = txtSearch.getText().trim().toLowerCase();
        
        // 1. Dọn dẹp chữ chìm
        if (search.contains("tìm kiếm") || search.contains("search") || search.contains("...")) {
            search = "";
        }
        
        Object selectedCat = cbxCategory.getSelectedItem();
        String category = (selectedCat == null) ? "" : selectedCat.toString();

        if (currentList == null || currentList.isEmpty()) return;

        // 2. Sắp xếp an toàn (Bắt lỗi null tên sản phẩm)
        try {
            currentList.sort((p1, p2) -> {
                boolean c1 = selectedProductsMap.containsKey(p1.getProductId());
                boolean c2 = selectedProductsMap.containsKey(p2.getProductId());
                if (c1 && !c2) return -1; // p1 có KM -> lên đầu
                if (!c1 && c2) return 1;  // p2 có KM -> lên đầu
                
                String name1 = p1.getName() == null ? "" : p1.getName();
                String name2 = p2.getName() == null ? "" : p2.getName();
                return name1.compareToIgnoreCase(name2); 
            });
        } catch (Exception e) {
            System.out.println("Lỗi sắp xếp: " + e.getMessage());
        }

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); 

        for (ProductModel p : currentList) {
            if (p.getStatus() == 0 || p.getStockQuantity() <= 0) {
                continue; 
            }

            boolean matchesSearch = search.isEmpty() || 
                                    (p.getName() != null && p.getName().toLowerCase().contains(search)) || 
                                    String.valueOf(p.getProductId()).contains(search);
            
            // 3. FIX LỖI Ở ĐÂY: Bắt cả chữ "Chọn" và "Lọc" để không bị ẩn nhầm danh sách
            boolean matchesCategory = category.isEmpty() || 
                                      category.contains("Chọn") || 
                                      category.contains("Lọc") || 
                                      (p.getCategory() != null && p.getCategory().getName().equals(category));

            if (matchesSearch && matchesCategory) {
                boolean isChecked = selectedProductsMap.containsKey(p.getProductId());
                String discountValue = isChecked ? String.format("%.0f", selectedProductsMap.get(p.getProductId())) : "";
                
                model.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    discountValue, 
                    isChecked      
                });
            }
        }
    }

    // Logic Lắng nghe thông minh: Xử lý cả khi gõ số và khi click Checkbox
    private void setupTableListener() {
        jTable1.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            
            // Chỉ bắt sự kiện khi có thay đổi ở cột 2 (Gõ Mức giảm) hoặc cột 3 (Tích Checkbox)
            if (row >= 0 && (col == 2 || col == 3) && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                
                int productId = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
                
                // Tránh lỗi null khi ô bị rỗng
                Object chkObj = jTable1.getValueAt(row, 3);
                boolean isChecked = (chkObj != null && (boolean) chkObj);
                
                Object valObj = jTable1.getValueAt(row, 2);
                String discountStr = (valObj != null) ? valObj.toString().trim() : "";
                
                if (isChecked) {
                    try {
                        double val = Double.parseDouble(discountStr);
                        if (val <= 0) throw new NumberFormatException();
                        
                        // Đưa vào Map thành công
                        selectedProductsMap.put(productId, val);
                    } catch (NumberFormatException ex) {
                        // Nếu tích vào ô Thêm mà chưa gõ số tiền, hoặc gõ sai -> Báo lỗi & Gỡ tích
                        if (col == 3) { 
                            JOptionPane.showMessageDialog(this, "Vui lòng gõ Mức giảm hợp lệ (Số > 0) trước khi tích chọn!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                            // Tắt event listener tạm thời để tránh bị lặp vô tận (Infinite loop) khi set lại giá trị False
                            SwingUtilities.invokeLater(() -> jTable1.setValueAt(false, row, 3));
                        }
                        selectedProductsMap.remove(productId);
                    }
                } else {
                    // Nếu bỏ tích Checkbox -> Xóa ngay khỏi Map
                    selectedProductsMap.remove(productId);
                }
            }
        });
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblMaKM = new javax.swing.JLabel();
        lvlStartDate = new javax.swing.JLabel();
        txtPromoCode = new Custom_Component.MyTextField();
        lvlEndDate = new javax.swing.JLabel();
        dateEnd = new com.toedter.calendar.JDateChooser();
        dateStart = new com.toedter.calendar.JDateChooser();
        btnAdd = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        txtSearch = new Custom_Component.MyTextField();
        jLabel12 = new javax.swing.JLabel();
        cbxCategory = new javax.swing.JComboBox<>();

        jPanel2.setBackground(new java.awt.Color(255, 153, 0));

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TẠO MÃ CHƯƠNG TRÌNH MỚI");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        lblMaKM.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblMaKM.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMaKM.setText("Tên Chương trình khuyến mãi");
        lblMaKM.setPreferredSize(new java.awt.Dimension(100, 35));

        lvlStartDate.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlStartDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lvlStartDate.setText("Ngày Bắt đầu");
        lvlStartDate.setPreferredSize(new java.awt.Dimension(100, 35));

        lvlEndDate.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlEndDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lvlEndDate.setText("Ngày Kết thúc");
        lvlEndDate.setPreferredSize(new java.awt.Dimension(100, 35));

        dateEnd.setDateFormatString("dd/MM/yyyy");
        dateEnd.setFont(new java.awt.Font("Sans Serif Collection", 1, 12)); // NOI18N
        dateEnd.setPreferredSize(new java.awt.Dimension(100, 25));

        dateStart.setDateFormatString("dd/MM/yyyy");
        dateStart.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        dateStart.setPreferredSize(new java.awt.Dimension(100, 25));

        btnAdd.setBackground(new java.awt.Color(0, 51, 204));
        btnAdd.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Thêm");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnHuy.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnHuy.setText("Huỷ");
        btnHuy.addActionListener(this::btnHuyActionPerformed);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Mã Sản phẩm", "Tên sản phẩm", "Mức giảm", "Thêm"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
            jTable1.getColumnModel().getColumn(3).setResizable(false);
        }

        txtSearch.addActionListener(this::txtSearchActionPerformed);

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resource/search.png"))); // NOI18N

        cbxCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Lọc theo danh mục--" }));
        cbxCategory.addActionListener(this::cbxCategoryActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPromoCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblMaKM, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 606, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(lvlStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lvlEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(156, 156, 156))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)))
                        .addComponent(cbxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAdd)
                .addGap(18, 18, 18)
                .addComponent(btnHuy)
                .addGap(22, 22, 22))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblMaKM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPromoCode, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lvlStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lvlEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnHuy))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHuyActionPerformed
        closeForm();
    }//GEN-LAST:event_btnHuyActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        handleSave();
    }//GEN-LAST:event_btnAddActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        executeFilter();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void cbxCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCategoryActionPerformed
        executeFilter();
    }//GEN-LAST:event_cbxCategoryActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnHuy;
    private javax.swing.JComboBox<String> cbxCategory;
    private com.toedter.calendar.JDateChooser dateEnd;
    private com.toedter.calendar.JDateChooser dateStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblMaKM;
    private javax.swing.JLabel lvlEndDate;
    private javax.swing.JLabel lvlStartDate;
    private Custom_Component.MyTextField txtPromoCode;
    public Custom_Component.MyTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
