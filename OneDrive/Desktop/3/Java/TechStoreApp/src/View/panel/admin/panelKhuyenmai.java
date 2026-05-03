/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.panel.admin;
import Controller.KhuyenMaiController;
public class panelKhuyenmai extends javax.swing.JPanel {
    private KhuyenMaiController controller;
    private javax.swing.table.TableRowSorter<javax.swing.table.TableModel> rowSorter;
    public java.util.List<Object[]> allData = new java.util.ArrayList<>();
    public int currentPage = 1;
    public int rowsPerPage = 10; 
    public int totalPages = 1;
    private boolean isRendering = false;

    public panelKhuyenmai() {
        initComponents();

        // Custom UI Table
        tblKhuyenmai.getTableHeader().setBorder(javax.swing.BorderFactory.createEmptyBorder());
        tblKhuyenmai.setBackground(java.awt.Color.WHITE);
        tblKhuyenmai.setRowHeight(50); 
        tblKhuyenmai.setShowVerticalLines(false); 
        tblKhuyenmai.setShowHorizontalLines(true); 
        tblKhuyenmai.setGridColor(new java.awt.Color(235, 235, 235)); 
        tblKhuyenmai.getTableHeader().setBackground(java.awt.Color.WHITE);
        tblKhuyenmai.getTableHeader().setForeground(new java.awt.Color(100, 100, 100)); 
        tblKhuyenmai.getTableHeader().setFont(new java.awt.Font("Sansserif", java.awt.Font.BOLD, 15));
        tblKhuyenmai.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(235, 235, 235))); 
        tblKhuyenmai.setFillsViewportHeight(true);

        if (tblKhuyenmai.getParent() instanceof javax.swing.JViewport) {
            tblKhuyenmai.getParent().setBackground(java.awt.Color.WHITE);
        }

        rowSorter = new javax.swing.table.TableRowSorter<>(tblKhuyenmai.getModel());
        tblKhuyenmai.setRowSorter(rowSorter);
        
        controller = new KhuyenMaiController(this);
        setupEvents();
        setupStatusCellEditor();
        controller.loadData();
    }

    public void renderPage() {
        isRendering = true;
        if (tblKhuyenmai.isEditing()) {
            tblKhuyenmai.getCellEditor().cancelCellEditing();
        }
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblKhuyenmai.getModel();
        model.setRowCount(0); 
        
        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, allData.size());
        
        for (int i = start; i < end; i++) {
            model.addRow(allData.get(i));
        }
        lblPage.setText("Trang " + currentPage + " / " + totalPages);
        isRendering = false;
    }

    private void setupEvents() {
        btnAdd.addActionListener(e -> controller.handleAdd());
        btnExcel.addActionListener(e -> controller.handleExportExcel());
        cbxKhuyenmai.addActionListener(e -> controller.handleSearch());
        myTextField1.addActionListener(e -> controller.handleSearch());
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; renderPage(); }
        });
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) { currentPage++; renderPage(); }
        });
    }

    private void setupStatusCellEditor() {
        javax.swing.JComboBox<String> cbxStatusEditor = new javax.swing.JComboBox<>(new String[]{"ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING"});
        cbxStatusEditor.setFont(tblKhuyenmai.getFont()); 
        javax.swing.table.TableColumn statusColumn = tblKhuyenmai.getColumnModel().getColumn(6);
        statusColumn.setCellEditor(new javax.swing.DefaultCellEditor(cbxStatusEditor));
        
        tblKhuyenmai.getModel().addTableModelListener(e -> {
            if (!isRendering && e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 6) {
                int row = e.getFirstRow();
                javax.swing.table.TableModel model = (javax.swing.table.TableModel) e.getSource();
                String promoCode = model.getValueAt(row, 0).toString();
                String newStatus = model.getValueAt(row, 6).toString();
                if(controller != null) controller.updatePromotionStatus(promoCode, newStatus);
            }
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        ChuyenPageSp = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        lblPage = new javax.swing.JLabel();
        btnNext = new javax.swing.JButton();
        btnExcel = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        myTextField1 = new Custom_Component.MyTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKhuyenmai = new javax.swing.JTable();
        cbxKhuyenmai = new javax.swing.JComboBox<>();
        btnAdd = new javax.swing.JButton();

        jPanel2.setBackground(new java.awt.Color(204, 255, 204));

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setText("QUẢN LÝ KHUYẾN MÃI");

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setText("Theo dõi và xử lý trạng thái đơn đặt hàng");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(jLabel2)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 20, 20));

        ChuyenPageSp.setBackground(new java.awt.Color(255, 255, 255));

        btnPrev.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnPrev.setText("<");
        btnPrev.addActionListener(this::btnPrevActionPerformed);

        lblPage.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblPage.setText("Trang 1 / 1");

        btnNext.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnNext.setText(">");
        btnNext.addActionListener(this::btnNextActionPerformed);

        javax.swing.GroupLayout ChuyenPageSpLayout = new javax.swing.GroupLayout(ChuyenPageSp);
        ChuyenPageSp.setLayout(ChuyenPageSpLayout);
        ChuyenPageSpLayout.setHorizontalGroup(
            ChuyenPageSpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChuyenPageSpLayout.createSequentialGroup()
                .addComponent(btnPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext))
        );
        ChuyenPageSpLayout.setVerticalGroup(
            ChuyenPageSpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChuyenPageSpLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ChuyenPageSpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPrev)
                    .addComponent(lblPage)
                    .addComponent(btnNext))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnExcel.setBackground(new java.awt.Color(0, 204, 51));
        btnExcel.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnExcel.setForeground(new java.awt.Color(255, 255, 255));
        btnExcel.setText("Xuất Excel");

        jLabel6.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Trạng thái mã");

        myTextField1.addActionListener(this::myTextField1ActionPerformed);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resource/search.png"))); // NOI18N

        jScrollPane1.setBorder(null);

        tblKhuyenmai.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tblKhuyenmai.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã Khuyến mãi", "Tên Chương trình", "Loại Giảm giá", "Mức giảm", "Bắt đầu", "Kết thúc", "Trạng thái"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblKhuyenmai);
        if (tblKhuyenmai.getColumnModel().getColumnCount() > 0) {
            tblKhuyenmai.getColumnModel().getColumn(0).setResizable(false);
            tblKhuyenmai.getColumnModel().getColumn(1).setResizable(false);
            tblKhuyenmai.getColumnModel().getColumn(2).setResizable(false);
            tblKhuyenmai.getColumnModel().getColumn(3).setResizable(false);
            tblKhuyenmai.getColumnModel().getColumn(4).setResizable(false);
            tblKhuyenmai.getColumnModel().getColumn(6).setResizable(false);
        }

        cbxKhuyenmai.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cbxKhuyenmai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING" }));

        btnAdd.setBackground(new java.awt.Color(0, 51, 204));
        btnAdd.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Thêm Mã");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(myTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(cbxKhuyenmai, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(btnAdd)
                .addGap(18, 18, 18)
                .addComponent(btnExcel))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(1, 1, 1)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(myTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxKhuyenmai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExcel)
                        .addComponent(btnAdd)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        if (currentPage > 1) { // Nếu chưa phải trang đầu tiên
            currentPage--;     // Giảm số trang
            renderPage();      // Vẽ lại bảng
        }
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        if (currentPage < totalPages) { // Nếu chưa tới trang cuối cùng
            currentPage++;              // Tăng số trang
            renderPage();               // Vẽ lại bảng
        }
    }//GEN-LAST:event_btnNextActionPerformed

    private void myTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_myTextField1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ChuyenPageSp;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    public javax.swing.JComboBox<String> cbxKhuyenmai;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPage;
    public Custom_Component.MyTextField myTextField1;
    private javax.swing.JTable tblKhuyenmai;
    // End of variables declaration//GEN-END:variables
}
