/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Custom_Component;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ProductCard extends JPanel {

    private JLabel lblName, lblPrice, lblOldPrice;

    // Hàm khởi tạo nhận dữ liệu
    public ProductCard(String name, String price, String oldPrice) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Cố định kích thước thẻ
        setPreferredSize(new Dimension(220, 280));
        setMaximumSize(new Dimension(220, 280));

        // 1. Hình ảnh (Giả lập bằng một ô màu xám nếu chưa có ảnh)
        JPanel imagePlaceholder = new JPanel();
        imagePlaceholder.setBackground(new Color(240, 240, 240));
        imagePlaceholder.setPreferredSize(new Dimension(180, 120));
        imagePlaceholder.setMaximumSize(new Dimension(180, 120));
        imagePlaceholder.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Tên sản phẩm
        lblName = new JLabel(name);
        lblName.setFont(new Font("Arial", Font.BOLD, 14));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Giá bán
        lblPrice = new JLabel(price);
        lblPrice.setForeground(new Color(230, 80, 0)); // Màu cam TechStore
        lblPrice.setFont(new Font("Arial", Font.BOLD, 16));
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Các nút bấm
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnAddCart = new JButton("Thêm giỏ");
        JButton btnBuy = new JButton("Mua ngay");

        // Tuỳ chỉnh màu nút (nếu có FlatLaf sẽ tự đẹp, đây là fallback)
        btnBuy.setBackground(new Color(255, 120, 0));
        btnBuy.setForeground(Color.WHITE);

        buttonPanel.add(btnAddCart);
        buttonPanel.add(btnBuy);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ghép các thành phần lại
        add(imagePlaceholder);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(lblName);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(lblPrice);
        add(Box.createVerticalGlue()); // Đẩy nút xuống đáy
        add(buttonPanel);
    }
}
