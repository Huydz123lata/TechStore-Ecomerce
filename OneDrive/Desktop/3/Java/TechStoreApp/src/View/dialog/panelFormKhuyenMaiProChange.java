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



public class panelFormKhuyenMaiProChange extends javax.swing.JPanel {
    private final KhuyenMaiController parentController;
    private final javax.swing.JDialog parentDialog;
    
    private ProductController productController = new ProductController();
    private ProductDAO productDAO = new ProductDAO();
    private List<ProductModel> currentList;
    private Map<Integer, Double> selectedProductsMap = new HashMap<>();
    
    private int editPromoId = -1; // Biến lưu trữ ID của chương trình đang sửa

    public panelFormKhuyenMaiProChange(KhuyenMaiController controller, javax.swing.JDialog dialog) {
        initComponents();
        this.parentController = controller;
        this.parentDialog = dialog;
        
        lblMaKM.setText("Tên Chương trình:");
        productController.updateDataCategoryCbx(cbxCategory);
        currentList = productDAO.getAllProducts();
        
        setupTableListener(); 
    }

    // Hàm nhận dữ liệu từ Controller bơm vào Form
    public void setEditData(int promoId, String name, java.util.Date start, java.util.Date end, Map<Integer, Double> currentProducts) {
        this.editPromoId = promoId;
        txtPromoCode.setText(name);
        dateStart.setDate(start);
        dateEnd.setDate(end);
        
        if (currentProducts != null) {
            this.selectedProductsMap.putAll(currentProducts);
        }
        executeFilter(); // Vẽ lại bảng với các sản phẩm đã được tích sẵn
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

        if (selectedProductsMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mức giảm và tích chọn ít nhất 1 sản phẩm!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (parentController != null && editPromoId != -1) {
            boolean success = parentController.updatePromotionPro(editPromoId, promoName, dateStart.getDate(), dateEnd.getDate(), selectedProductsMap);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật chương trình thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                closeForm();
            }
        }
    }

    private void executeFilter() {
        String search = txtSearch.getText().trim().toLowerCase();
        
        // 1. Dọn dẹp chữ chìm placeholder
        if (search.contains("tìm kiếm") || search.contains("search") || search.contains("...")) {
            search = "";
        }
        
        Object selectedCat = cbxCategory.getSelectedItem();
        String category = (selectedCat == null) ? "" : selectedCat.toString();

        if (currentList == null || currentList.isEmpty()) return;

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); 

        for (ProductModel p : currentList) {
            // LĂN XỬ LÝ QUAN TRỌNG: Chỉ xét các sản phẩm CÓ trong chương trình khuyến mãi này
            // Nếu sản phẩm không nằm trong selectedProductsMap -> Bỏ qua luôn, không hiện lên bảng
            if (!selectedProductsMap.containsKey(p.getProductId())) {
                continue; 
            }

            // Bộ lọc tìm kiếm theo Tên hoặc Mã sản phẩm
            boolean matchesSearch = search.isEmpty() || 
                                    (p.getName() != null && p.getName().toLowerCase().contains(search)) || 
                                    String.valueOf(p.getProductId()).contains(search);
            
            // Bộ lọc theo Danh mục hàng hóa
            boolean matchesCategory = category.isEmpty() || 
                                      category.contains("Chọn") || 
                                      category.contains("Lọc") || 
                                      (p.getCategory() != null && p.getCategory().getName().equals(category));

            // Nếu thỏa mãn các điều kiện tìm kiếm/lọc danh mục thì mới đưa lên bảng
            if (matchesSearch && matchesCategory) {
                // Chắc chắn là có trong Map vì đã check ở trên
                double discountPrice = selectedProductsMap.get(p.getProductId());
                String discountValue = String.format("%.0f", discountPrice);
                
                model.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    discountValue, 
                    true // Mặc định luôn là true (đã tích chọn) vì nó thuộc chương trình này
                });
            }
        }
    }

    private void setupTableListener() {
        jTable1.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            
            // Lắng nghe cột Mức giảm (2) và cột Thêm (3)
            if (row >= 0 && (col == 2 || col == 3) && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int productId = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
                
                Object chkObj = jTable1.getValueAt(row, 3);
                boolean isChecked = (chkObj != null && (boolean) chkObj);
                
                Object valObj = jTable1.getValueAt(row, 2);
                String discountStr = (valObj != null) ? valObj.toString().trim() : "";
                
                if (isChecked) {
                    try {
                        double val = Double.parseDouble(discountStr);
                        if (val <= 0) throw new NumberFormatException();
                        selectedProductsMap.put(productId, val);
                    } catch (NumberFormatException ex) {
                        if (col == 3) { 
                            JOptionPane.showMessageDialog(this, "Vui lòng gõ Mức giảm hợp lệ trước khi tích!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                            SwingUtilities.invokeLater(() -> jTable1.setValueAt(false, row, 3));
                        }
                        selectedProductsMap.remove(productId);
                    }
                } else {
                    selectedProductsMap.remove(productId); // Bỏ tích là tự động loại bỏ
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
        jLabel1.setText("SỬA MÃ CHƯƠNG TRÌNH");

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
        btnAdd.setText("Sửa");
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
