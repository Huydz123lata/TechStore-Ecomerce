package Util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class TableDecorate {

    public static class StatusRenderer extends DefaultTableCellRenderer {

        private ImageIcon activeIcon;
        private ImageIcon inactiveIcon;

        public StatusRenderer() {
            try {
                // Ba kiểm tra kỹ đường dẫn ảnh trong Resource nhé
                java.net.URL activeURL = getClass().getResource("/Resource/green_dot.png");
                java.net.URL inactiveURL = getClass().getResource("/Resource/red_dot.png");

                if (activeURL != null) {
                    activeIcon = new ImageIcon(activeURL);
                }
                if (inactiveURL != null) {
                    inactiveIcon = new ImageIcon(inactiveURL);
                }

            } catch (Exception e) {
                System.err.println("Lỗi load icon TableDecorate: " + e.getMessage());
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setIconTextGap(10); // Khoảng cách giữa chấm và chữ

            if (value == null) {
                label.setIcon(null);
                label.setText("");
                return label;
            }

            String status = value.toString().trim();

            // LOGIC TỐI ƯU: Kiểm tra các từ khóa tích cực
            // Ba có thể thêm bất kỳ chữ nào muốn hiện màu xanh vào đây
            if (status.equalsIgnoreCase("ACTIVE")
                    || status.equalsIgnoreCase("Đang bán")
                    || status.equalsIgnoreCase("Hoạt động")) {

                label.setIcon(activeIcon);
                label.setForeground(new Color(40, 167, 69)); // Màu xanh lá
            } else {
                // Các trường hợp còn lại: INACTIVE, Ngừng bán, Hết hàng, Ngừng kinh doanh...
                label.setIcon(inactiveIcon);
                label.setForeground(new Color(220, 53, 69)); // Màu đỏ
            }

            // Nếu hàng đang được chọn thì giữ màu chữ trắng cho dễ nhìn trên nền xanh của bảng
            if (isSelected) {
                label.setForeground(table.getSelectionForeground());
            }

            return label;
        }
    }
}
