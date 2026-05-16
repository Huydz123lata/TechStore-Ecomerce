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

    public ProductCard(String imageUrl, String brand, String name, String price, String oldPrice) {
        // Cài đặt bố cục và viền cho thẻ
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true), // Viền bo góc nhẹ
                new EmptyBorder(10, 10, 10, 10) // Padding bên trong
        ));
        setPreferredSize(new Dimension(220, 350));

        // 1. Hình ảnh sản phẩm (Dùng JLabel chứa ImageIcon)
        JLabel lblImage = new JLabel(new ImageIcon(imageUrl));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Tên thương hiệu
        JLabel lblBrand = new JLabel(brand);
        lblBrand.setForeground(Color.GRAY);
        lblBrand.setFont(new Font("Arial", Font.BOLD, 11));

        // 3. Tên sản phẩm
        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Arial", Font.BOLD, 14));

        // 4. Giá bán
        JLabel lblPrice = new JLabel(price);
        lblPrice.setForeground(new Color(230, 80, 0)); // Màu cam
        lblPrice.setFont(new Font("Arial", Font.BOLD, 16));

        // 5. Giá cũ (gạch ngang - dùng HTML trong JLabel)
        JLabel lblOldPrice = new JLabel("<html><strike>" + oldPrice + "</strike></html>");
        lblOldPrice.setForeground(Color.LIGHT_GRAY);

        // 6. Khu vực nút bấm (Thêm giỏ & Mua ngay)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnAddCart = new JButton("Thêm giỏ");
        btnAddCart.setBackground(new Color(20, 30, 50));
        btnAddCart.setForeground(Color.WHITE);

        JButton btnBuy = new JButton("Mua ngay");
        btnBuy.setBackground(new Color(255, 120, 0));
        btnBuy.setForeground(Color.WHITE);

        buttonPanel.add(btnAddCart);
        buttonPanel.add(btnBuy);

        // Thêm tất cả vào Card
        add(lblImage);
        add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách
        add(lblBrand);
        add(lblName);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(lblPrice);
        add(lblOldPrice);
        add(Box.createVerticalGlue()); // Đẩy các nút xuống dưới cùng
        add(buttonPanel);
    }
}
