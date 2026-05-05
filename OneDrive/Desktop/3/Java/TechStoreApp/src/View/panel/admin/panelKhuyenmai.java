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
        panelCoupon = new javax.swing.JPanel();
        ChuyenPageSp = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        lblPage = new javax.swing.JLabel();
        btnNext = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        myTextField1 = new Custom_Component.MyTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKhuyenmai = new javax.swing.JTable();
        cbxKhuyenmai = new javax.swing.JComboBox<>();
        btnAdd = new javax.swing.JButton();
        panelPromotion = new javax.swing.JPanel();
        ChuyenPageSp1 = new javax.swing.JPanel();
        btnPrev1 = new javax.swing.JButton();
        lblPage1 = new javax.swing.JLabel();
        btnNext1 = new javax.swing.JButton();
        btnDelete1 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        myTextField2 = new Custom_Component.MyTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblKhuyenmai1 = new javax.swing.JTable();
        cbxKhuyenmai1 = new javax.swing.JComboBox<>();
        btnAdd1 = new javax.swing.JButton();

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

        panelCoupon.setBackground(new java.awt.Color(255, 255, 255));
        panelCoupon.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 20, 20));

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

        btnDelete.setBackground(new java.awt.Color(0, 153, 153));
        btnDelete.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Xoá Mã");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

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
                "Mã Giảm giá", "Mô tả", "Loại Giảm giá", "Mức giảm", "Ngày Bắt đầu", "Ngày Kết thúc", "Trạng thái"
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
            tblKhuyenmai.getColumnModel().getColumn(5).setResizable(false);
            tblKhuyenmai.getColumnModel().getColumn(6).setResizable(false);
        }

        cbxKhuyenmai.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cbxKhuyenmai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING" }));
        cbxKhuyenmai.addActionListener(this::cbxKhuyenmaiActionPerformed);

        btnAdd.setBackground(new java.awt.Color(0, 51, 204));
        btnAdd.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Thêm Mã");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        javax.swing.GroupLayout panelCouponLayout = new javax.swing.GroupLayout(panelCoupon);
        panelCoupon.setLayout(panelCouponLayout);
        panelCouponLayout.setHorizontalGroup(
            panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCouponLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCouponLayout.createSequentialGroup()
                .addGroup(panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelCouponLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCouponLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(myTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(cbxKhuyenmai, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(btnAdd)
                .addGap(18, 18, 18)
                .addComponent(btnDelete))
        );
        panelCouponLayout.setVerticalGroup(
            panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCouponLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(1, 1, 1)
                .addGroup(panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(myTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxKhuyenmai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDelete)
                        .addComponent(btnAdd)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        panelPromotion.setBackground(new java.awt.Color(255, 255, 255));
        panelPromotion.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 20, 20));

        ChuyenPageSp1.setBackground(new java.awt.Color(255, 255, 255));

        btnPrev1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnPrev1.setText("<");
        btnPrev1.addActionListener(this::btnPrev1ActionPerformed);

        lblPage1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblPage1.setText("Trang 1 / 1");

        btnNext1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnNext1.setText(">");
        btnNext1.addActionListener(this::btnNext1ActionPerformed);

        javax.swing.GroupLayout ChuyenPageSp1Layout = new javax.swing.GroupLayout(ChuyenPageSp1);
        ChuyenPageSp1.setLayout(ChuyenPageSp1Layout);
        ChuyenPageSp1Layout.setHorizontalGroup(
            ChuyenPageSp1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChuyenPageSp1Layout.createSequentialGroup()
                .addComponent(btnPrev1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPage1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext1))
        );
        ChuyenPageSp1Layout.setVerticalGroup(
            ChuyenPageSp1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChuyenPageSp1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ChuyenPageSp1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPrev1)
                    .addComponent(lblPage1)
                    .addComponent(btnNext1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnDelete1.setBackground(new java.awt.Color(0, 153, 153));
        btnDelete1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnDelete1.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete1.setText("Xoá Mã");
        btnDelete1.addActionListener(this::btnDelete1ActionPerformed);

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Trạng thái mã");

        myTextField2.addActionListener(this::myTextField2ActionPerformed);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resource/search.png"))); // NOI18N

        jScrollPane2.setBorder(null);

        tblKhuyenmai1.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tblKhuyenmai1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã Giảm giá", "Mô tả", "Loại Giảm giá", "Mức giảm", "Ngày Bắt đầu", "Ngày Kết thúc", "Trạng thái"
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
        jScrollPane2.setViewportView(tblKhuyenmai1);
        if (tblKhuyenmai1.getColumnModel().getColumnCount() > 0) {
            tblKhuyenmai1.getColumnModel().getColumn(0).setResizable(false);
            tblKhuyenmai1.getColumnModel().getColumn(1).setResizable(false);
            tblKhuyenmai1.getColumnModel().getColumn(2).setResizable(false);
            tblKhuyenmai1.getColumnModel().getColumn(3).setResizable(false);
            tblKhuyenmai1.getColumnModel().getColumn(4).setResizable(false);
            tblKhuyenmai1.getColumnModel().getColumn(5).setResizable(false);
            tblKhuyenmai1.getColumnModel().getColumn(6).setResizable(false);
        }

        cbxKhuyenmai1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cbxKhuyenmai1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING" }));
        cbxKhuyenmai1.addActionListener(this::cbxKhuyenmai1ActionPerformed);

        btnAdd1.setBackground(new java.awt.Color(0, 51, 204));
        btnAdd1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnAdd1.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd1.setText("Thêm Mã");
        btnAdd1.addActionListener(this::btnAdd1ActionPerformed);

        javax.swing.GroupLayout panelPromotionLayout = new javax.swing.GroupLayout(panelPromotion);
        panelPromotion.setLayout(panelPromotionLayout);
        panelPromotionLayout.setHorizontalGroup(
            panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPromotionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(ChuyenPageSp1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPromotionLayout.createSequentialGroup()
                .addGroup(panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelPromotionLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPromotionLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(myTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(cbxKhuyenmai1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(btnAdd1)
                .addGap(18, 18, 18)
                .addComponent(btnDelete1))
        );
        panelPromotionLayout.setVerticalGroup(
            panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPromotionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(1, 1, 1)
                .addGroup(panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(myTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxKhuyenmai1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDelete1)
                        .addComponent(btnAdd1)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ChuyenPageSp1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelCoupon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelPromotion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCoupon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPromotion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
        if (controller != null) controller.handleSearch();
    }//GEN-LAST:event_myTextField1ActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (controller != null) controller.handleAdd();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (controller != null) controller.handleDelete();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void cbxKhuyenmaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxKhuyenmaiActionPerformed
        if (controller != null) controller.handleSearch();
    }//GEN-LAST:event_cbxKhuyenmaiActionPerformed

    private void btnPrev1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrev1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPrev1ActionPerformed

    private void btnNext1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNext1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNext1ActionPerformed

    private void btnDelete1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelete1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnDelete1ActionPerformed

    private void myTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_myTextField2ActionPerformed

    private void cbxKhuyenmai1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxKhuyenmai1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxKhuyenmai1ActionPerformed

    private void btnAdd1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdd1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAdd1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ChuyenPageSp;
    private javax.swing.JPanel ChuyenPageSp1;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAdd1;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDelete1;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnNext1;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnPrev1;
    public javax.swing.JComboBox<String> cbxKhuyenmai;
    public javax.swing.JComboBox<String> cbxKhuyenmai1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblPage;
    private javax.swing.JLabel lblPage1;
    public Custom_Component.MyTextField myTextField1;
    public Custom_Component.MyTextField myTextField2;
    private javax.swing.JPanel panelCoupon;
    private javax.swing.JPanel panelPromotion;
    public javax.swing.JTable tblKhuyenmai;
    public javax.swing.JTable tblKhuyenmai1;
    // End of variables declaration//GEN-END:variables
}
