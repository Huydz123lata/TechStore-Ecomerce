/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.dialog;

import Controller.KhuyenMaiController;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class panelFormKhuyenMai extends javax.swing.JPanel {

    private final KhuyenMaiController parentController;
    private final javax.swing.JDialog parentDialog;

    public panelFormKhuyenMai(KhuyenMaiController controller, javax.swing.JDialog dialog) {
        initComponents();
        this.parentController = controller;
        this.parentDialog = dialog;
        cbxType.setSelectedIndex(0);
        handleTypeChange();
    }

    private void closeForm() {
        if (parentDialog != null) {
            parentDialog.dispose();
        } else {
            SwingUtilities.getWindowAncestor(this).dispose();
        }
    }

    private void handleInsert() {
        String code = txtPromoCode.getText().trim().toUpperCase();
        String name = txtPromoName.getText().trim();
        int typeIndex = cbxType.getSelectedIndex();
        String valueStr = txtDiscountValue.getText().trim();
        String minStr = txtMin.getText().trim();
        String maxStr = txtMax.getText().trim();
        // Bẫy lỗi chữ mờ (Placeholder)
        if (code.equalsIgnoreCase("MÃ KHUYẾN MÃI") || code.equalsIgnoreCase("NHẬP MÃ...")) {
            code = "";
        }
        if (name.equalsIgnoreCase("TÊN CHƯƠNG TRÌNH") || name.equalsIgnoreCase("NHẬP TÊN...")) {
            name = "";
        }
        if (valueStr.equalsIgnoreCase("MỨC GIẢM") || valueStr.equalsIgnoreCase("NHẬP MỨC GIẢM...")) {
            valueStr = "";
        }

        Date startDate = dateStart.getDate();
        Date endDate = dateEnd.getDate();
        if (code.isEmpty() && name.isEmpty() && valueStr.isEmpty() && typeIndex <= 0) {
            closeForm();
            return;
        }

        if (code.isEmpty() || name.isEmpty() || valueStr.isEmpty() || typeIndex <= 0 || startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin (Mã, Tên, Loại, Mức giảm, Ngày tháng)!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc không được nhỏ hơn Ngày bắt đầu!", "Lỗi ngày tháng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double discountValue = 0;
        double minOrder = 0;
        double maxDiscount = 0;

        try {
            discountValue = Double.parseDouble(valueStr);
            if (discountValue <= 0) {
                throw new NumberFormatException();
            }

            // MIN (Đơn tối thiểu): Nếu bỏ trống thì gán = 0
            minOrder = minStr.isEmpty() ? 0 : Double.parseDouble(minStr);
            if (minOrder < 0) {
                throw new NumberFormatException();
            }

            String typeStr = cbxType.getSelectedItem().toString();

            if ("PERCENTAGE".equals(typeStr) || "PERCENT".equals(typeStr)) {
                if (discountValue > 100) {
                    JOptionPane.showMessageDialog(this, "Mức giảm theo phần trăm không được vượt quá 100%!", "Lỗi giá trị", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // MAX (Giảm tối đa): Nếu bỏ trống thì gán = 0 (Tức là không giới hạn)
                maxDiscount = maxStr.isEmpty() ? 0 : Double.parseDouble(maxStr);
                if (maxDiscount < 0) {
                    throw new NumberFormatException();
                }

            } else {
                // Nếu là AMOUNT (VNĐ) thì Giảm tối đa chính là giá trị giảm luôn
                maxDiscount = discountValue;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mức giảm, Đơn tối thiểu và Giảm tối đa phải là con số hợp lệ (> 0)!", "Lỗi giá trị", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String typeStr = cbxType.getSelectedItem().toString();
        String status = "UPCOMING";

        boolean isSuccess = false;
        if (parentController != null) {
            // Truyền tất cả các thông số (bao gồm min, max) qua Controller
            isSuccess = parentController.createNewPromotion(code, name, typeStr, discountValue, minOrder, maxDiscount, startDate, endDate, status);
        }

        if (isSuccess) {
            JOptionPane.showMessageDialog(this, "Tạo mã khuyến mãi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (parentController != null) {
                parentController.refreshCurrentPage();
            }
            closeForm();
        }
    }

    private void handleTypeChange() {
        if (cbxType.getSelectedItem() == null) {
            return;
        }

        String selectedType = cbxType.getSelectedItem().toString();

        if ("PERCENTAGE".equals(selectedType)) {
            // Nếu chọn % -> Hiện cả Label và Ô nhập Max
            lvlValue2.setVisible(true);
            txtMax.setVisible(true);
        } else {
            // Nếu là AMOUNT hoặc chưa chọn -> Cho tàng hình hoàn toàn
            lvlValue2.setVisible(false);
            txtMax.setVisible(false);
            txtMax.setText(""); // Xóa trắng dữ liệu lỡ nhập dở
        }

        // Refresh lại giao diện để không bị lưu bóng mờ (Glitch UI)
        this.revalidate();
        this.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblMaKM = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        lvlType = new javax.swing.JLabel();
        lvlValue = new javax.swing.JLabel();
        lvlStartDate = new javax.swing.JLabel();
        txtPromoCode = new Custom_Component.MyTextField();
        txtPromoName = new Custom_Component.MyTextField();
        txtDiscountValue = new Custom_Component.MyTextField();
        cbxType = new javax.swing.JComboBox<>();
        lvlEndDate = new javax.swing.JLabel();
        dateStart = new com.toedter.calendar.JDateChooser();
        btnAdd = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        lvlValue1 = new javax.swing.JLabel();
        lvlValue2 = new javax.swing.JLabel();
        txtMin = new Custom_Component.MyTextField();
        txtMax = new Custom_Component.MyTextField();
        dateEnd = new com.toedter.calendar.JDateChooser();

        jPanel2.setBackground(new java.awt.Color(255, 153, 0));

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TẠO MÃ GIẢM GIÁ MỚI");

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
        lblMaKM.setText("Mã Giảm giá");
        lblMaKM.setPreferredSize(new java.awt.Dimension(100, 35));

        lblName.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblName.setText("Mô tả");
        lblName.setPreferredSize(new java.awt.Dimension(100, 35));

        lvlType.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlType.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lvlType.setText("Loại Giảm giá");
        lvlType.setPreferredSize(new java.awt.Dimension(100, 35));

        lvlValue.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lvlValue.setText("Mức Giảm");
        lvlValue.setPreferredSize(new java.awt.Dimension(100, 35));

        lvlStartDate.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlStartDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lvlStartDate.setText("Ngày Bắt đầu");
        lvlStartDate.setPreferredSize(new java.awt.Dimension(100, 35));

        cbxType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Chọn loại --", "PERCENTAGE", "AMOUNT" }));
        cbxType.setPreferredSize(new java.awt.Dimension(150, 22));
        cbxType.addActionListener(this::cbxTypeActionPerformed);

        lvlEndDate.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlEndDate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lvlEndDate.setText("Ngày Kết thúc");
        lvlEndDate.setPreferredSize(new java.awt.Dimension(100, 35));

        dateStart.setDateFormatString("dd/MM/yyyy");
        dateStart.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        dateStart.setPreferredSize(new java.awt.Dimension(100, 25));

        btnAdd.setBackground(new java.awt.Color(0, 51, 204));
        btnAdd.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(204, 204, 204));
        btnAdd.setText("Thêm");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnHuy.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnHuy.setText("Huỷ");
        btnHuy.addActionListener(this::btnHuyActionPerformed);

        lvlValue1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlValue1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lvlValue1.setText("Đơn tối thiểu");
        lvlValue1.setPreferredSize(new java.awt.Dimension(100, 35));

        lvlValue2.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lvlValue2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lvlValue2.setText("GIảm tối đa");
        lvlValue2.setPreferredSize(new java.awt.Dimension(100, 35));

        dateEnd.setDateFormatString("dd/MM/yyyy");
        dateEnd.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        dateEnd.setPreferredSize(new java.awt.Dimension(100, 25));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAdd)
                .addGap(18, 18, 18)
                .addComponent(btnHuy)
                .addGap(23, 23, 23))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblMaKM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtPromoCode, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPromoName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lvlValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtDiscountValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lvlValue2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lvlValue1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtMax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(lvlType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(lvlStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(dateStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                    .addGap(14, 14, 14)
                                    .addComponent(lvlEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMaKM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPromoCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPromoName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDiscountValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlValue1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lvlValue2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(dateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lvlStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lvlEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnHuy))
                .addContainerGap(25, Short.MAX_VALUE))
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
        if (parentDialog != null) {
            parentDialog.dispose();
        } else {
            SwingUtilities.getWindowAncestor(this).dispose();
        }
    }//GEN-LAST:event_btnHuyActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        handleInsert();
    }//GEN-LAST:event_btnAddActionPerformed

    private void cbxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTypeActionPerformed
        handleTypeChange();
    }//GEN-LAST:event_cbxTypeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnHuy;
    private javax.swing.JComboBox<String> cbxType;
    private com.toedter.calendar.JDateChooser dateEnd;
    private com.toedter.calendar.JDateChooser dateStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblMaKM;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lvlEndDate;
    private javax.swing.JLabel lvlStartDate;
    private javax.swing.JLabel lvlType;
    private javax.swing.JLabel lvlValue;
    private javax.swing.JLabel lvlValue1;
    private javax.swing.JLabel lvlValue2;
    private Custom_Component.MyTextField txtDiscountValue;
    private Custom_Component.MyTextField txtMax;
    private Custom_Component.MyTextField txtMin;
    private Custom_Component.MyTextField txtPromoCode;
    private Custom_Component.MyTextField txtPromoName;
    // End of variables declaration//GEN-END:variables
}
