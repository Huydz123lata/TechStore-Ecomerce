
package View.panel.admin;

import Controller.DonHangController;

public class panelDonhang extends javax.swing.JPanel {
    private DonHangController controller;
    private javax.swing.table.TableRowSorter<javax.swing.table.TableModel> rowSorter;
    public java.util.List<Object[]> allData = new java.util.ArrayList<>();
    public int currentPage = 1;
    public int rowsPerPage = 10; // Muốn 1 trang có bao nhiêu dòng thì đổi ở đây
    public int totalPages = 1;
    private boolean isRendering = false;
    public panelDonhang() {
        initComponents();
        this.setBackground(java.awt.Color.WHITE);
tblDonhang.getTableHeader().setBorder(javax.swing.BorderFactory.createEmptyBorder());
// 2. Tùy chỉnh Bảng (Table) giống hệt Web
tblDonhang.setBackground(java.awt.Color.WHITE);
tblDonhang.setRowHeight(50); // Cực kỳ quan trọng: Kéo giãn khoảng cách các hàng cho thoáng
tblDonhang.setShowVerticalLines(false); // Ẩn đường kẻ dọc
tblDonhang.setShowHorizontalLines(true); // Hiện đường kẻ ngang
tblDonhang.setGridColor(new java.awt.Color(235, 235, 235)); // Đường kẻ ngang màu xám rất nhạt

// 3. Tùy chỉnh Tiêu đề của Bảng (Header)
tblDonhang.getTableHeader().setBackground(java.awt.Color.WHITE);
tblDonhang.getTableHeader().setForeground(new java.awt.Color(100, 100, 100)); // Chữ màu xám
tblDonhang.getTableHeader().setFont(new java.awt.Font("Sansserif", java.awt.Font.BOLD, 15));
tblDonhang.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(235, 235, 235))); // Chỉ để lại 1 viền mỏng ở dưới header

// 4. Phủ trắng vùng trống bên dưới bảng
tblDonhang.setFillsViewportHeight(true);
if (tblDonhang.getParent() instanceof javax.swing.JViewport) {
    tblDonhang.getParent().setBackground(java.awt.Color.WHITE);
}
    // Khởi tạo bộ lọc và gắn nó vào bảng
    rowSorter = new javax.swing.table.TableRowSorter<>(tblDonhang.getModel());
    tblDonhang.setRowSorter(rowSorter);
    controller = new DonHangController(this);
    setupEvents();
    setupStatusCellEditor();
    controller.loadData();
    }
    private javax.swing.Timer searchTimer;
    private void setupEvents() {
        btnExcel.addActionListener(e -> controller.handleExportExcel());
        cbxDonhang.addActionListener(e -> controller.handleSearch());
        cbxThanhtoan.addActionListener(e -> controller.handleSearch());
        myTextField1.addActionListener(e -> controller.handleSearch());
        tblDonhang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblDonhang.getSelectedRow();
                    int col = tblDonhang.getSelectedColumn();
                    
                    // Bỏ qua nếu click đúp vào cột 4 (Trạng thái) hoặc cột 5 (Thanh toán)
                    if (row != -1 && col != 4 && col != 5) {
                        String orderIdStr = tblDonhang.getValueAt(row, 0).toString().replace("ORD", "");
                        
                        // Tạm thời hiển thị Dialog thông báo. Sau này sẽ thay bằng dòng mở Form.
                        javax.swing.JOptionPane.showMessageDialog(panelDonhang.this, 
                            "Chuẩn bị mở Form chi tiết cho Đơn hàng số: " + orderIdStr);
                    }
                }
            }
        });
    }
    public void renderPage() {
        isRendering = true;
        if (tblDonhang.isEditing()) {
            tblDonhang.getCellEditor().cancelCellEditing();
        }
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblDonhang.getModel();
    model.setRowCount(0); // Xóa trắng bảng
    
    // Tính toán vị trí bắt đầu và kết thúc để cắt dữ liệu
    int start = (currentPage - 1) * rowsPerPage;
    int end = Math.min(start + rowsPerPage, allData.size());
    
    // Đổ dữ liệu đã cắt lên bảng
    for (int i = start; i < end; i++) {
        model.addRow(allData.get(i));
    }
    
    // Cập nhật con số trên Label
    lblPage.setText("Trang " + currentPage + " / " + totalPages);
    isRendering = false;
}
    private void setupStatusCellEditor() {
        // 1. Tạo ComboBox chứa các trạng thái chuẩn
        javax.swing.JComboBox<String> cbxStatusEditor = new javax.swing.JComboBox<>(
            new String[]{"PENDING", "CONFIRMED", "PROCESSING", "SHIPPING", "DELIVERED", "CANCELLED"}
        );
        
        // 2. Gắn ComboBox này vào cột số 4 (Cột Trạng thái)
        javax.swing.table.TableColumn statusColumn = tblDonhang.getColumnModel().getColumn(4);
        statusColumn.setCellEditor(new javax.swing.DefaultCellEditor(cbxStatusEditor));
        
        // 3. Lắng nghe sự thay đổi dữ liệu trên bảng
        tblDonhang.getModel().addTableModelListener(e -> {
            // Chỉ chạy lệnh Update nếu không phải do hàm renderPage đang đổ dữ liệu
            if (!isRendering && e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 4) {
                int row = e.getFirstRow();
                javax.swing.table.TableModel model = (javax.swing.table.TableModel) e.getSource();
                
                // Lấy ID và Trạng thái mới
                String orderIdStr = model.getValueAt(row, 0).toString().replace("ORD", "");
                String newStatus = model.getValueAt(row, 4).toString();
                
                // Gửi qua Controller để gọi DB Update
                // Bạn cần tự viết hàm updateOrderStatus trong Controller nhé
                if(controller != null) {
                    controller.updateOrderStatus(Integer.parseInt(orderIdStr), newStatus);
                }
            }
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ChuyenPageSp = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        lblPage = new javax.swing.JLabel();
        btnNext = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        myTextField1 = new Custom_Component.MyTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDonhang = new javax.swing.JTable();
        cbxDonhang = new javax.swing.JComboBox<>();
        cbxThanhtoan = new javax.swing.JComboBox<>();
        btnExcel = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setText("QUẢN LÝ ĐƠN HÀNG");

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setText("Theo dõi và xử lý trạng thái đơn đặt hàng");

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resource/search.png"))); // NOI18N

        jScrollPane1.setBorder(null);

        tblDonhang.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tblDonhang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã đơn hàng", "Tên khách hàng", "Ngày đặt", "Tổng tiền", "Trạng thái", "Thanh toán"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDonhang);
        if (tblDonhang.getColumnModel().getColumnCount() > 0) {
            tblDonhang.getColumnModel().getColumn(0).setResizable(false);
            tblDonhang.getColumnModel().getColumn(1).setResizable(false);
            tblDonhang.getColumnModel().getColumn(2).setResizable(false);
            tblDonhang.getColumnModel().getColumn(3).setResizable(false);
            tblDonhang.getColumnModel().getColumn(4).setResizable(false);
            tblDonhang.getColumnModel().getColumn(5).setResizable(false);
        }

        cbxDonhang.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cbxDonhang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "PENDING", "CONFIRMED", "PROCESSING", "SHIPPING", "DELIVERED", "CANCELLED" }));

        cbxThanhtoan.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        cbxThanhtoan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "SUCCESS", "FAILED", "PENDING", "REFUNDED" }));

        btnExcel.setBackground(new java.awt.Color(0, 204, 51));
        btnExcel.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnExcel.setForeground(new java.awt.Color(255, 255, 255));
        btnExcel.setText("Xuất Excel");

        jLabel6.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel6.setText("Trạng thái đơn hàng");

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel7.setText("Trạng thái thanh toán");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 707, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(myTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxDonhang, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbxThanhtoan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnExcel))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cbxDonhang, cbxThanhtoan});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel6, jLabel7});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(myTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxDonhang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbxThanhtoan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExcel)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ChuyenPageSp;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    public javax.swing.JComboBox<String> cbxDonhang;
    public javax.swing.JComboBox<String> cbxThanhtoan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPage;
    public Custom_Component.MyTextField myTextField1;
    private javax.swing.JTable tblDonhang;
    // End of variables declaration//GEN-END:variables
}
