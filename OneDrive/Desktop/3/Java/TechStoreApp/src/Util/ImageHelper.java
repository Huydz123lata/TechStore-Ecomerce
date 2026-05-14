/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author HUY0406
 */
public class ImageHelper {

    // Hàm này giúp ba xử lý ảnh ở bất cứ đâu trong dự án
    public static void setProductImage(JLabel label, String imageName) {
        try {
            if (imageName == null || imageName.trim().isEmpty()) {
                label.setIcon(null);
                label.setText("No Image");
                return;
            }

            // Đường dẫn gốc tới thư mục chứa ảnh
            String path = "src/Resource/ProductImage/" + imageName;
            File file = new File(path);

            if (file.exists()) {
                ImageIcon icon = new ImageIcon(path);
                // Tự động lấy kích thước của cái Label đang gọi nó để Scale cho vừa
                Image img = icon.getImage().getScaledInstance(
                        label.getWidth() > 0 ? label.getWidth() : 120,
                        label.getHeight() > 0 ? label.getHeight() : 120,
                        Image.SCALE_SMOOTH
                );
                label.setIcon(new ImageIcon(img));
                label.setText("");
            } else {
                label.setIcon(null);
                label.setText("File not found");
            }
        } catch (Exception e) {
            System.err.println("Lỗi hiển thị ảnh: " + e.getMessage());
        }
    }
}
