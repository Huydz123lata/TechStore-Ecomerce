/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.panel.admin;

/**
 *
 * @author phamd
 */
public class panelTrangchu extends javax.swing.JPanel {
    private java.util.List<Object[]> listCanhBao = new java.util.ArrayList<>();
    private int currentPageCanhBao = 1;
    private int rowsPerPageCanhBao = 5;
    private int totalPagesCanhBao = 1;
    public panelTrangchu() {
        initComponents();
        loadDashboardData();
    }
    private void renderPageCanhBao() {
    // 1. Tính toán vị trí cắt dữ liệu
    int start = (currentPageCanhBao - 1) * rowsPerPageCanhBao;
    int end = Math.min(start + rowsPerPageCanhBao, listCanhBao.size());

    // 2. Lấy mô hình của bảng và xóa sạch dữ liệu cũ
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel(); // Thay jTable1 bằng tên thật của bảng
    model.setRowCount(0);

    // 3. Đổ dữ liệu của trang hiện tại vào bảng
    for (int i = start; i < end; i++) {
        model.addRow(listCanhBao.get(i));
    }

    // 4. Cập nhật chữ trên Label phân trang
    lblPageCanhBao.setText("Trang " + currentPageCanhBao + " / " + totalPagesCanhBao);
}
private void loadDashboardData() {
    // 1. Giả lập dữ liệu (Sau này chỗ này sẽ là code gọi Database)
    double doanhThuHomNay = 15500000.0;
    int donHangMoi = 42;
    int spDaBan = 105;
    
    // BIẾN QUAN TRỌNG: Đơn khách vừa đặt, chờ kho đóng gói
    int donDangCho = 8; 

    // 2. Format số tiền cho có dấu phẩy (15,500,000 đ)
    java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
    
    // 3. Đẩy dữ liệu vào các Label
    lblDoanhThu.setText(formatter.format(doanhThuHomNay) + " đ");
    lblDonMoi.setText(String.valueOf(donHangMoi));
    lblSpBan.setText(String.valueOf(spDaBan));
    
    // 4. Logic UI riêng cho Thẻ "Đơn chờ xử lý"
    lblDonCho.setText(String.valueOf(donDangCho));
    
    // Đổi màu để cảnh báo nhân viên theo mức độ
    if (donDangCho > 10) {
        // Quá tải: Báo động Đỏ
        lblDonCho.setForeground(new java.awt.Color(255, 50, 50));
    } else if (donDangCho > 0) {
        // Có đơn chờ: Cảnh báo Cam
        lblDonCho.setForeground(new java.awt.Color(255, 150, 0));
    } else {
        // Hết việc: Xanh lá an tâm
        lblDonCho.setForeground(new java.awt.Color(50, 200, 50));
        lblDonCho.setText("0"); 
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        card1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblDoanhThu = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        card3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        lblDonMoi = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        card4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        lblSpBan = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        card5 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        lblDonCho = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        pnThongTin = new javax.swing.JPanel();
        pnBieuDo = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        pnCanhBao = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        ChuyenPage1 = new javax.swing.JPanel();
        btnPrev1 = new javax.swing.JButton();
        lblPageCanhBao = new javax.swing.JLabel();
        btnNext1 = new javax.swing.JButton();

        card1.setBackground(new java.awt.Color(204, 255, 255));
        card1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        card1.setPreferredSize(new java.awt.Dimension(200, 100));

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("DOANH THU HÔM NAY");

        lblDoanhThu.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblDoanhThu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDoanhThu.setText("0");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Cập nhật lúc...");

        javax.swing.GroupLayout card1Layout = new javax.swing.GroupLayout(card1);
        card1.setLayout(card1Layout);
        card1Layout.setHorizontalGroup(
            card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(card1Layout.createSequentialGroup()
                        .addGroup(card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDoanhThu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        card1Layout.setVerticalGroup(
            card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDoanhThu)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addContainerGap())
        );

        card3.setBackground(new java.awt.Color(255, 204, 204));
        card3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        card3.setPreferredSize(new java.awt.Dimension(200, 100));

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("ĐƠN HÀNG MỚI");

        lblDonMoi.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblDonMoi.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDonMoi.setText("0");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Cập nhật lúc...");

        javax.swing.GroupLayout card3Layout = new javax.swing.GroupLayout(card3);
        card3.setLayout(card3Layout);
        card3Layout.setHorizontalGroup(
            card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(card3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblDonMoi, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        card3Layout.setVerticalGroup(
            card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDonMoi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addContainerGap())
        );

        card4.setBackground(new java.awt.Color(255, 255, 204));
        card4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        card4.setPreferredSize(new java.awt.Dimension(200, 100));

        jLabel10.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("SẢN PHẨM ĐÃ BÁN");

        lblSpBan.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblSpBan.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSpBan.setText("0");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("Cập nhật lúc...");

        javax.swing.GroupLayout card4Layout = new javax.swing.GroupLayout(card4);
        card4.setLayout(card4Layout);
        card4Layout.setHorizontalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(card4Layout.createSequentialGroup()
                        .addGroup(card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(card4Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(lblSpBan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        card4Layout.setVerticalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSpBan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addContainerGap())
        );

        card5.setBackground(new java.awt.Color(204, 255, 204));
        card5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        card5.setPreferredSize(new java.awt.Dimension(200, 100));

        jLabel13.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("ĐƠN ĐANG CHỜ XỬ LÝ");

        lblDonCho.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblDonCho.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDonCho.setText("0");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Cập nhật lúc...");

        javax.swing.GroupLayout card5Layout = new javax.swing.GroupLayout(card5);
        card5.setLayout(card5Layout);
        card5Layout.setHorizontalGroup(
            card5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(card5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDonCho, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(card5Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        card5Layout.setVerticalGroup(
            card5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDonCho)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jLabel15)
                .addContainerGap())
        );

        pnBieuDo.setBackground(new java.awt.Color(255, 255, 255));
        pnBieuDo.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("BIỂU ĐỒ DOANH THU 7 NGÀY QUA");

        javax.swing.GroupLayout pnBieuDoLayout = new javax.swing.GroupLayout(pnBieuDo);
        pnBieuDo.setLayout(pnBieuDoLayout);
        pnBieuDoLayout.setHorizontalGroup(
            pnBieuDoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnBieuDoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnBieuDoLayout.setVerticalGroup(
            pnBieuDoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnBieuDoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnCanhBao.setBackground(new java.awt.Color(255, 255, 255));
        pnCanhBao.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        jLabel8.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 51, 51));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("SẢN PHẨM SẮP HẾT HÀNG");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Mã SP", "Tên Sản phẩm", "Tồn kho"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
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
        }

        ChuyenPage1.setBackground(new java.awt.Color(255, 255, 255));

        btnPrev1.setFont(new java.awt.Font("SansSerif", 1, 10)); // NOI18N
        btnPrev1.setText("<");
        btnPrev1.addActionListener(this::btnPrev1ActionPerformed);

        lblPageCanhBao.setFont(new java.awt.Font("SansSerif", 1, 10)); // NOI18N
        lblPageCanhBao.setText("Trang 1 / 1");

        btnNext1.setFont(new java.awt.Font("SansSerif", 1, 10)); // NOI18N
        btnNext1.setText(">");
        btnNext1.addActionListener(this::btnNext1ActionPerformed);

        javax.swing.GroupLayout ChuyenPage1Layout = new javax.swing.GroupLayout(ChuyenPage1);
        ChuyenPage1.setLayout(ChuyenPage1Layout);
        ChuyenPage1Layout.setHorizontalGroup(
            ChuyenPage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChuyenPage1Layout.createSequentialGroup()
                .addComponent(btnPrev1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPageCanhBao)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext1))
        );
        ChuyenPage1Layout.setVerticalGroup(
            ChuyenPage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChuyenPage1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(ChuyenPage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPrev1)
                    .addComponent(lblPageCanhBao)
                    .addComponent(btnNext1)))
        );

        javax.swing.GroupLayout pnCanhBaoLayout = new javax.swing.GroupLayout(pnCanhBao);
        pnCanhBao.setLayout(pnCanhBaoLayout);
        pnCanhBaoLayout.setHorizontalGroup(
            pnCanhBaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnCanhBaoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnCanhBaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnCanhBaoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(ChuyenPage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        pnCanhBaoLayout.setVerticalGroup(
            pnCanhBaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnCanhBaoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ChuyenPage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnThongTinLayout = new javax.swing.GroupLayout(pnThongTin);
        pnThongTin.setLayout(pnThongTinLayout);
        pnThongTinLayout.setHorizontalGroup(
            pnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnThongTinLayout.createSequentialGroup()
                .addComponent(pnBieuDo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnCanhBao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnThongTinLayout.setVerticalGroup(
            pnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnThongTinLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnBieuDo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnCanhBao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnThongTin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(card3, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(card4, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(card5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(card1, 98, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card3, 98, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card4, 98, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(card5, 98, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(pnThongTin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {card1, card3, card4, card5});

    }// </editor-fold>//GEN-END:initComponents

    private void btnPrev1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrev1ActionPerformed
        if (currentPageCanhBao > 1) { // Nếu chưa phải trang đầu tiên
            currentPageCanhBao--;     // Giảm số trang
            renderPageCanhBao();      // Vẽ lại bảng
        }
    }//GEN-LAST:event_btnPrev1ActionPerformed

    private void btnNext1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNext1ActionPerformed
        if (currentPageCanhBao < totalPagesCanhBao) { // Nếu chưa tới trang cuối cùng
            currentPageCanhBao++;              // Tăng số trang
            renderPageCanhBao();               // Vẽ lại bảng
        }
    }//GEN-LAST:event_btnNext1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ChuyenPage1;
    private javax.swing.JButton btnNext1;
    private javax.swing.JButton btnPrev1;
    private javax.swing.JPanel card1;
    private javax.swing.JPanel card3;
    private javax.swing.JPanel card4;
    private javax.swing.JPanel card5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblDoanhThu;
    private javax.swing.JLabel lblDonCho;
    private javax.swing.JLabel lblDonMoi;
    private javax.swing.JLabel lblPageCanhBao;
    private javax.swing.JLabel lblSpBan;
    private javax.swing.JPanel pnBieuDo;
    private javax.swing.JPanel pnCanhBao;
    private javax.swing.JPanel pnThongTin;
    // End of variables declaration//GEN-END:variables
}
