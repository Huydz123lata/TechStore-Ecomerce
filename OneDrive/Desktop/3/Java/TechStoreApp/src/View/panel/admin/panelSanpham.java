/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.panel.admin;

import View.dialog.dialogThemSP;

public class panelSanpham extends javax.swing.JPanel {

    private javax.swing.table.TableRowSorter<javax.swing.table.TableModel> rowSorter;
    // Danh sách chứa TẤT CẢ dữ liệu (Thay thế cho Database thật sau này)
    private java.util.List<Object[]> allData = new java.util.ArrayList<>();

// Các biến điều khiển phân trang
    private int currentPage = 1;
    private int rowsPerPage = 10; // Muốn 1 trang có bao nhiêu dòng thì đổi ở đây
    private int totalPages = 1;

    public panelSanpham() {
        initComponents();
        // 1. Ép toàn bộ nền thành màu Trắng
        this.setBackground(java.awt.Color.WHITE);
        tblSanPham.getTableHeader().setBorder(javax.swing.BorderFactory.createEmptyBorder());
// 2. Tùy chỉnh Bảng (Table) giống hệt Web
        tblSanPham.setBackground(java.awt.Color.WHITE);
        tblSanPham.setRowHeight(50); // Cực kỳ quan trọng: Kéo giãn khoảng cách các hàng cho thoáng
        tblSanPham.setShowVerticalLines(false); // Ẩn đường kẻ dọc
        tblSanPham.setShowHorizontalLines(true); // Hiện đường kẻ ngang
        tblSanPham.setGridColor(new java.awt.Color(235, 235, 235)); // Đường kẻ ngang màu xám rất nhạt

// 3. Tùy chỉnh Tiêu đề của Bảng (Header)
        tblSanPham.getTableHeader().setBackground(java.awt.Color.WHITE);
        tblSanPham.getTableHeader().setForeground(new java.awt.Color(100, 100, 100)); // Chữ màu xám
        tblSanPham.getTableHeader().setFont(new java.awt.Font("Sansserif", java.awt.Font.BOLD, 15));
        tblSanPham.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(235, 235, 235))); // Chỉ để lại 1 viền mỏng ở dưới header

// 4. Phủ trắng vùng trống bên dưới bảng
        tblSanPham.setFillsViewportHeight(true);
        if (tblSanPham.getParent() instanceof javax.swing.JViewport) {
            tblSanPham.getParent().setBackground(java.awt.Color.WHITE);
        }
        // Khởi tạo bộ lọc và gắn nó vào bảng
        rowSorter = new javax.swing.table.TableRowSorter<>(tblSanPham.getModel());
        tblSanPham.setRowSorter(rowSorter);
        loadDummyData();
    }
    // Bỏ đoạn này vào dưới cùng của class panelSanpham (trước dấu } cuối cùng)
    // Hàm 1: Nạp dữ liệu vào "Kho" (allData)

    public void loadDummyData() {
        allData.clear();

        // Tự động tạo ra 35 sản phẩm mẫu để test phân trang
        for (int i = 1; i <= 35; i++) {
            allData.add(new Object[]{null, "SP20260" + i, "Sản phẩm test số " + i, 1500000.0, 50, 10, "Sẵn sàng"});
        }

        // Tính toán tổng số trang (Ví dụ: 35 dòng / 10 = 3.5 -> Làm tròn lên là 4 trang)
        totalPages = (int) Math.ceil((double) allData.size() / rowsPerPage);
        if (totalPages == 0) {
            totalPages = 1;
        }

        currentPage = 1; // Reset về trang 1
        renderPage();    // Gọi hàm vẽ bảng
    }

// Hàm 2: Cắt dữ liệu từ "Kho" đem lên Bảng tùy theo Trang hiện tại
    public void renderPage() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblSanPham.getModel();
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
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnThemSP = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSanPham = new javax.swing.JTable();
        ChuyenPageSp = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        lblPage = new javax.swing.JLabel();
        btnNext = new javax.swing.JButton();
        btnXoaSP = new javax.swing.JButton();
        myTextField1 = new Custom_Component.MyTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setText("QUẢN LÝ SẢN PHẨM");

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setText("Thông tin chi tiết các sản phẩm");

        btnThemSP.setBackground(new java.awt.Color(77, 111, 255));
        btnThemSP.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnThemSP.setForeground(new java.awt.Color(255, 255, 255));
        btnThemSP.setText("+ Thêm sản phẩm");
        btnThemSP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnThemSPMouseClicked(evt);
            }
        });
        btnThemSP.addActionListener(this::btnThemSPActionPerformed);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resource/search.png"))); // NOI18N

        jScrollPane1.setBorder(null);

        tblSanPham.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        tblSanPham.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Hình ảnh", "Mã SP", "Tên SP", "Giá Bán", "Tồn kho", "Đã bán", "Trạng thái"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblSanPham);
        if (tblSanPham.getColumnModel().getColumnCount() > 0) {
            tblSanPham.getColumnModel().getColumn(0).setResizable(false);
            tblSanPham.getColumnModel().getColumn(1).setResizable(false);
            tblSanPham.getColumnModel().getColumn(2).setResizable(false);
            tblSanPham.getColumnModel().getColumn(3).setResizable(false);
            tblSanPham.getColumnModel().getColumn(4).setResizable(false);
            tblSanPham.getColumnModel().getColumn(5).setResizable(false);
            tblSanPham.getColumnModel().getColumn(6).setResizable(false);
        }

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

        btnXoaSP.setBackground(new java.awt.Color(77, 111, 255));
        btnXoaSP.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnXoaSP.setForeground(new java.awt.Color(255, 255, 255));
        btnXoaSP.setText("- Xoá sản phẩm");
        btnXoaSP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnXoaSPMouseClicked(evt);
            }
        });
        btnXoaSP.addActionListener(this::btnXoaSPActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(myTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                        .addGap(344, 344, 344)
                        .addComponent(btnXoaSP, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnThemSP))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnThemSP, btnXoaSP});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(btnThemSP))
                    .addComponent(jLabel1))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnXoaSP)
                    .addComponent(jLabel2))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(myTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ChuyenPageSp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnThemSP, btnXoaSP});

    }// </editor-fold>//GEN-END:initComponents

    private void btnThemSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemSPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnThemSPActionPerformed

    private void btnThemSPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnThemSPMouseClicked
        // 1. Khởi tạo Dialog
// (Dùng JFrame làm chủ, true để chặn tương tác màn hình dưới cho đến khi đóng dialog)
        dialogThemSP diag = new dialogThemSP(new javax.swing.JFrame(), true);
        diag.setLocationRelativeTo(null); // Hiện ra giữa màn hình
        diag.setVisible(true);

// 2. Sau khi Dialog đóng, kiểm tra xem người dùng có bấm Lưu không
        if (diag.isConfirmed()) {
            // Lấy mảng dữ liệu từ dialog gửi về
            Object[] newRow = diag.getRowData();

            // Thêm vào "Kho dữ liệu tổng" (allData) ở vị trí đầu tiên
            allData.add(0, newRow);

            // Tính toán lại tổng số trang (vì thêm dòng mới có thể làm tăng số trang)
            totalPages = (int) Math.ceil((double) allData.size() / rowsPerPage);

            // Quay về trang 1 để người dùng thấy ngay sản phẩm vừa thêm
            currentPage = 1;
            renderPage();

            javax.swing.JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
        }
    }//GEN-LAST:event_btnThemSPMouseClicked

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

    private void btnXoaSPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnXoaSPMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnXoaSPMouseClicked

    private void btnXoaSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaSPActionPerformed
        int selectedRow = tblSanPham.getSelectedRow();

        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm trong bảng để xoá!");
            return;
        }

        // 2. Hỏi xác nhận trước khi xoá (Tránh bấm nhầm)
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xoá sản phẩm này không?", "Xác nhận xoá",
                javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            // 3. XÁC ĐỊNH VỊ TRÍ THỰC SỰ TRONG DANH SÁCH TỔNG (allData)
            // Vì bảng đang phân trang, dòng số 0 trên bảng có thể là dòng số 10, 20 trong kho dữ liệu
            int realIndex = (currentPage - 1) * rowsPerPage + selectedRow;

            // 4. Xoá trong kho dữ liệu tổng
            allData.remove(realIndex);

            // 5. Tính toán lại tổng số trang (Lỡ xoá xong làm giảm số trang)
            totalPages = (int) Math.ceil((double) allData.size() / rowsPerPage);
            if (totalPages == 0) {
                totalPages = 1;
            }

            // 6. Xử lý trường hợp xoá hết sạch dòng ở trang cuối cùng
            if (currentPage > totalPages) {
                currentPage = totalPages;
            }

            // 7. Cập nhật lại giao diện
            renderPage();
            javax.swing.JOptionPane.showMessageDialog(this, "Đã xoá sản phẩm thành công!");
        }
    }//GEN-LAST:event_btnXoaSPActionPerformed

    private void myTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_myTextField1KeyReleased
        // Lấy chữ bạn vừa gõ trong ô (Chú ý: Thay jTextField1 bằng tên đúng của ô tìm kiếm nếu bạn đã đổi tên)
        String text = myTextField1.getText();

        if (text.trim().length() == 0) {
            // Nếu xóa hết chữ thì hiển thị lại toàn bộ bảng
            rowSorter.setRowFilter(null);
        } else {
            // Nếu có chữ thì lọc. "(?i)" giúp tìm kiếm không phân biệt chữ hoa, chữ thường
            rowSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text));
        }
    }//GEN-LAST:event_myTextField1KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ChuyenPageSp;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnThemSP;
    private javax.swing.JButton btnXoaSP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPage;
    private Custom_Component.MyTextField myTextField1;
    private javax.swing.JTable tblSanPham;
    // End of variables declaration//GEN-END:variables
}
