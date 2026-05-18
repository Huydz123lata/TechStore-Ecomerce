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

    public java.util.List<Object[]> allDataPro = new java.util.ArrayList<>();
    public int currentPagePro = 1;
    public int totalPagesPro = 1;
    private boolean isRenderingPro = false;
    
    public panelKhuyenmai() {
        initComponents();

        tblCoupon.getTableHeader().setBorder(javax.swing.BorderFactory.createEmptyBorder());
        tblCoupon.setBackground(java.awt.Color.WHITE);
        tblCoupon.setRowHeight(50);
        tblCoupon.setShowVerticalLines(false);
        tblCoupon.setShowHorizontalLines(true);
        tblCoupon.setGridColor(new java.awt.Color(235, 235, 235));
        tblCoupon.getTableHeader().setBackground(java.awt.Color.WHITE);
        tblCoupon.getTableHeader().setForeground(new java.awt.Color(100, 100, 100));
        tblCoupon.getTableHeader().setFont(new java.awt.Font("Sansserif", java.awt.Font.BOLD, 15));
        tblCoupon.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(235, 235, 235)));
        tblCoupon.setFillsViewportHeight(true);

        if (tblCoupon.getParent() instanceof javax.swing.JViewport) {
            tblCoupon.getParent().setBackground(java.awt.Color.WHITE);
        }

        rowSorter = new javax.swing.table.TableRowSorter<>(tblCoupon.getModel());
        tblCoupon.setRowSorter(rowSorter);
        tblPromotion.getTableHeader().setBorder(javax.swing.BorderFactory.createEmptyBorder());
        tblPromotion.setBackground(java.awt.Color.WHITE);
        tblPromotion.setRowHeight(50);
        tblPromotion.setShowVerticalLines(false);
        tblPromotion.setShowHorizontalLines(true);
        tblPromotion.setGridColor(new java.awt.Color(235, 235, 235));
        tblPromotion.getTableHeader().setBackground(java.awt.Color.WHITE);
        tblPromotion.getTableHeader().setForeground(new java.awt.Color(100, 100, 100));
        tblPromotion.getTableHeader().setFont(new java.awt.Font("Sansserif", java.awt.Font.BOLD, 15));
        tblPromotion.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(235, 235, 235)));
        tblPromotion.setFillsViewportHeight(true);

        if (tblPromotion.getParent() instanceof javax.swing.JViewport) {
            tblPromotion.getParent().setBackground(java.awt.Color.WHITE);
        }
        javax.swing.table.TableRowSorter<javax.swing.table.TableModel> rowSorterPro = new javax.swing.table.TableRowSorter<>(tblPromotion.getModel());
        tblPromotion.setRowSorter(rowSorterPro);

        controller = new KhuyenMaiController(this);
        setupStatusCellEditor();
        setupStatusCellEditorPro();
        setupTableEventsPro();

        controller.loadData();
        controller.loadDataPro();
    }

    public void renderPage() {
        isRendering = true;
        if (tblCoupon.isEditing()) {
            tblCoupon.getCellEditor().cancelCellEditing();
        }
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblCoupon.getModel();
        model.setRowCount(0);

        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, allData.size());

        for (int i = start; i < end; i++) {
            model.addRow(allData.get(i));
        }
        lblPage.setText("Trang " + currentPage + " / " + totalPages);
        isRendering = false;
    }

    public void renderPagePro() {
        isRenderingPro = true;
        if (tblPromotion.isEditing()) {
            tblPromotion.getCellEditor().cancelCellEditing();
        }
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblPromotion.getModel();
        model.setRowCount(0);

        int start = (currentPagePro - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, allDataPro.size());

        for (int i = start; i < end; i++) {
            model.addRow(allDataPro.get(i));
        }
        lblPage1.setText("Trang " + currentPagePro + " / " + totalPagesPro);
        isRenderingPro = false;
    }

    private void setupStatusCellEditor() {
        javax.swing.JComboBox<String> cbxStatusEditor = new javax.swing.JComboBox<>(new String[]{"ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING"});
        cbxStatusEditor.setFont(tblCoupon.getFont());
        javax.swing.table.TableColumn statusColumn = tblCoupon.getColumnModel().getColumn(8);
        statusColumn.setCellEditor(new javax.swing.DefaultCellEditor(cbxStatusEditor));

        tblCoupon.getModel().addTableModelListener(e -> {
            if (!isRendering && e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 8) {
                int row = e.getFirstRow();
                javax.swing.table.TableModel model = (javax.swing.table.TableModel) e.getSource();
                String promoCode = model.getValueAt(row, 0).toString();
                String newStatus = model.getValueAt(row, 8).toString();
                if (controller != null) {
                    controller.updatePromotionStatus(promoCode, newStatus);
                }
            }
        });
    }

    private void setupStatusCellEditorPro() {
        javax.swing.JComboBox<String> cbxStatusEditor = new javax.swing.JComboBox<>(new String[]{"ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING"});
        cbxStatusEditor.setFont(tblPromotion.getFont());

        // Chú ý: Cột trạng thái bây giờ là số 4 (nếu đếm từ 0, sau khi bạn đã xóa cột Mô tả)
        javax.swing.table.TableColumn statusColumn = tblPromotion.getColumnModel().getColumn(4);
        statusColumn.setCellEditor(new javax.swing.DefaultCellEditor(cbxStatusEditor));

        tblPromotion.getModel().addTableModelListener(e -> {
            if (!isRenderingPro && e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 4) {
                int row = e.getFirstRow();
                javax.swing.table.TableModel model = (javax.swing.table.TableModel) e.getSource();
                String proID = model.getValueAt(row, 0).toString();
                String newStatus = model.getValueAt(row, 4).toString();
                if (controller != null) {
                    controller.updateProStatus(proID, newStatus);
                }
            }
        });
    }

    private void setupTableEventsPro() {
        tblPromotion.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblPromotion.getSelectedRow();
                int col = tblPromotion.getSelectedColumn();

                // Nếu click vào cột số 5 (Chi tiết)
                if (row != -1 && col == 5) {
                    if (controller != null) {
                        controller.handleViewProDetails(); // <--- GỌI HÀM BẬT FORM Ở ĐÂY
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
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
        tblCoupon = new javax.swing.JTable();
        cbxKhuyenmai = new javax.swing.JComboBox<>();
        btnAdd = new javax.swing.JButton();
        btnChange = new javax.swing.JButton();
        panelPromotion = new javax.swing.JPanel();
        ChuyenPageSp1 = new javax.swing.JPanel();
        btnPrev1 = new javax.swing.JButton();
        lblPage1 = new javax.swing.JLabel();
        btnNext1 = new javax.swing.JButton();
        btnDeletePro = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        myTextField2 = new Custom_Component.MyTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblPromotion = new javax.swing.JTable();
        cbxKhuyenmai1 = new javax.swing.JComboBox<>();
        btnAddPro = new javax.swing.JButton();
        btnChangePro = new javax.swing.JButton();

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 102, 0));
        jLabel1.setText("QUẢN LÝ KHUYẾN MÃI");

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setText("Theo dõi và xử lý trạng thái Coupon và Promotion");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(jLabel2)
                .addGap(18, 18, 18))
        );

        jTabbedPane4.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N

        panelCoupon.setBackground(new java.awt.Color(255, 255, 255));

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
                .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext))
        );
        ChuyenPageSpLayout.setVerticalGroup(
            ChuyenPageSpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChuyenPageSpLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ChuyenPageSpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnPrev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ChuyenPageSpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPage)
                        .addComponent(btnNext)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnDelete.setBackground(new java.awt.Color(0, 153, 153));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Xoá Mã");
        btnDelete.setPreferredSize(new java.awt.Dimension(100, 35));
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Trạng thái mã");

        myTextField1.addActionListener(this::myTextField1ActionPerformed);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resource/search.png"))); // NOI18N

        jScrollPane1.setBorder(null);

        tblCoupon.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tblCoupon.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã Giảm giá", "Mô tả", "Loại Giảm giá", "Mức giảm", "Đơn tối thiểu", "Giảm tối đa", "Ngày Bắt đầu", "Ngày Kết thúc", "Trạng thái"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblCoupon);
        if (tblCoupon.getColumnModel().getColumnCount() > 0) {
            tblCoupon.getColumnModel().getColumn(0).setResizable(false);
            tblCoupon.getColumnModel().getColumn(1).setResizable(false);
            tblCoupon.getColumnModel().getColumn(2).setResizable(false);
            tblCoupon.getColumnModel().getColumn(3).setResizable(false);
            tblCoupon.getColumnModel().getColumn(4).setResizable(false);
            tblCoupon.getColumnModel().getColumn(5).setResizable(false);
            tblCoupon.getColumnModel().getColumn(6).setResizable(false);
            tblCoupon.getColumnModel().getColumn(7).setResizable(false);
            tblCoupon.getColumnModel().getColumn(8).setResizable(false);
        }

        cbxKhuyenmai.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cbxKhuyenmai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING" }));
        cbxKhuyenmai.addActionListener(this::cbxKhuyenmaiActionPerformed);

        btnAdd.setBackground(new java.awt.Color(0, 51, 204));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Thêm Mã");
        btnAdd.setPreferredSize(new java.awt.Dimension(100, 35));
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnChange.setBackground(new java.awt.Color(255, 102, 0));
        btnChange.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnChange.setForeground(new java.awt.Color(255, 255, 255));
        btnChange.setText("Sửa Mã");
        btnChange.setPreferredSize(new java.awt.Dimension(100, 35));
        btnChange.addActionListener(this::btnChangeActionPerformed);

        javax.swing.GroupLayout panelCouponLayout = new javax.swing.GroupLayout(panelCoupon);
        panelCoupon.setLayout(panelCouponLayout);
        panelCouponLayout.setHorizontalGroup(
            panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCouponLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCouponLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, 0)
                        .addGroup(panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelCouponLayout.createSequentialGroup()
                                .addComponent(myTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(cbxKhuyenmai, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCouponLayout.createSequentialGroup()
                        .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        panelCouponLayout.setVerticalGroup(
            panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCouponLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panelCouponLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(myTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelCouponLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(7, 7, 7)
                        .addComponent(cbxKhuyenmai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane4.addTab("Quản lý Coupon", panelCoupon);

        panelPromotion.setBackground(new java.awt.Color(255, 255, 255));
        panelPromotion.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        btnDeletePro.setBackground(new java.awt.Color(0, 153, 153));
        btnDeletePro.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnDeletePro.setForeground(new java.awt.Color(255, 255, 255));
        btnDeletePro.setText("Xoá Promotion");
        btnDeletePro.setPreferredSize(new java.awt.Dimension(120, 35));
        btnDeletePro.addActionListener(this::btnDeleteProActionPerformed);

        jLabel11.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Trạng thái Khuyến mãi");

        myTextField2.addActionListener(this::myTextField2ActionPerformed);

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resource/search.png"))); // NOI18N

        jScrollPane5.setBorder(null);

        tblPromotion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tblPromotion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã Khuyến mãi", "Tên Khuyến mãi", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái", "Chi tiết"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tblPromotion);
        if (tblPromotion.getColumnModel().getColumnCount() > 0) {
            tblPromotion.getColumnModel().getColumn(0).setResizable(false);
            tblPromotion.getColumnModel().getColumn(1).setResizable(false);
            tblPromotion.getColumnModel().getColumn(2).setResizable(false);
            tblPromotion.getColumnModel().getColumn(3).setResizable(false);
            tblPromotion.getColumnModel().getColumn(4).setResizable(false);
            tblPromotion.getColumnModel().getColumn(5).setResizable(false);
        }

        cbxKhuyenmai1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cbxKhuyenmai1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "ACTIVE", "CANCELLED", "EXPIRED", "UPCOMING" }));
        cbxKhuyenmai1.addActionListener(this::cbxKhuyenmai1ActionPerformed);

        btnAddPro.setBackground(new java.awt.Color(0, 51, 204));
        btnAddPro.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnAddPro.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPro.setText("Thêm Promotion");
        btnAddPro.setPreferredSize(new java.awt.Dimension(120, 35));
        btnAddPro.addActionListener(this::btnAddProActionPerformed);

        btnChangePro.setBackground(new java.awt.Color(255, 102, 0));
        btnChangePro.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnChangePro.setForeground(new java.awt.Color(255, 255, 255));
        btnChangePro.setText("Sửa Promotion");
        btnChangePro.setPreferredSize(new java.awt.Dimension(120, 35));
        btnChangePro.addActionListener(this::btnChangeProActionPerformed);

        javax.swing.GroupLayout panelPromotionLayout = new javax.swing.GroupLayout(panelPromotion);
        panelPromotion.setLayout(panelPromotionLayout);
        panelPromotionLayout.setHorizontalGroup(
            panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPromotionLayout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addGroup(panelPromotionLayout.createSequentialGroup()
                        .addComponent(myTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(cbxKhuyenmai1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAddPro, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeletePro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnChangePro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPromotionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(ChuyenPageSp1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelPromotionLayout.setVerticalGroup(
            panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPromotionLayout.createSequentialGroup()
                .addGroup(panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPromotionLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel12)
                        .addGap(29, 29, 29))
                    .addGroup(panelPromotionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel11)
                        .addGap(1, 1, 1)
                        .addGroup(panelPromotionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxKhuyenmai1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(myTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDeletePro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAddPro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnChangePro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(ChuyenPageSp1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane4.addTab("Quản lý Promotion", panelPromotion);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane4)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane4))
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
        if (controller != null) {
            controller.handleSearch();
        }
    }//GEN-LAST:event_myTextField1ActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (controller != null) {
            controller.handleAdd();
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (controller != null) {
            controller.handleDelete();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void cbxKhuyenmaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxKhuyenmaiActionPerformed
        if (controller != null) {
            controller.handleSearch();
        }
    }//GEN-LAST:event_cbxKhuyenmaiActionPerformed

    private void btnPrev1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrev1ActionPerformed
        if (currentPagePro > 1) {
            currentPagePro--;
            renderPagePro();
        }
    }//GEN-LAST:event_btnPrev1ActionPerformed

    private void btnNext1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNext1ActionPerformed
        if (currentPagePro < totalPagesPro) {
            currentPagePro++;
            renderPagePro();
        }
    }//GEN-LAST:event_btnNext1ActionPerformed

    private void btnDeleteProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProActionPerformed
        if (controller != null) {
            controller.handleDeletePro();
        }
    }//GEN-LAST:event_btnDeleteProActionPerformed

    private void myTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myTextField2ActionPerformed
        if (controller != null) {
            controller.handleSearchPro();
        }
    }//GEN-LAST:event_myTextField2ActionPerformed

    private void cbxKhuyenmai1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxKhuyenmai1ActionPerformed
        if (controller != null) {
            controller.handleSearchPro();
        }
    }//GEN-LAST:event_cbxKhuyenmai1ActionPerformed

    private void btnAddProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProActionPerformed
        if (controller != null) {
            controller.handleAddPro();
        }
    }//GEN-LAST:event_btnAddProActionPerformed

    private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeActionPerformed
        if (controller != null) {
            controller.handleEdit();
        }
    }//GEN-LAST:event_btnChangeActionPerformed

    private void btnChangeProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeProActionPerformed
        if (controller != null) {
            controller.handleEditPro(); // <--- GỌI HÀM SỬA BÊN CONTROLLER
        }
    }//GEN-LAST:event_btnChangeProActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ChuyenPageSp;
    private javax.swing.JPanel ChuyenPageSp1;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddPro;
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnChangePro;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeletePro;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnNext1;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnPrev1;
    public javax.swing.JComboBox<String> cbxKhuyenmai;
    public javax.swing.JComboBox<String> cbxKhuyenmai1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JLabel lblPage;
    private javax.swing.JLabel lblPage1;
    public Custom_Component.MyTextField myTextField1;
    public Custom_Component.MyTextField myTextField2;
    private javax.swing.JPanel panelCoupon;
    private javax.swing.JPanel panelPromotion;
    public javax.swing.JTable tblCoupon;
    public javax.swing.JTable tblPromotion;
    // End of variables declaration//GEN-END:variables
}
