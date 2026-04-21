package View;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class CustomComponents {

    // Hàm vẽ chung để đảm bảo TextField và PasswordField giống hệt nhau 100%
    private static void paintRoundedStyle(JComponent c, Graphics g, Color bgColor) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 15, 15);

        // KIỂM TRA FOCUS ĐỂ ĐỔI MÀU VIỀN
        if (c.hasFocus()) {
            g2.setStroke(new BasicStroke(1.5f)); // Nét dày hơn một xíu cho nổi bật
            g2.setColor(new Color(255, 153, 51)); // Màu Cam (Bạn có thể đổi mã RGB cho tệp với UI của bạn)
        } else {
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(new Color(160, 160, 160)); // Màu Xám bình thường
        }

        g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 15, 15);
        g2.dispose();
    }

    public static class RoundedTextField extends JTextField {

        public RoundedTextField(int radius) {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRoundedStyle(this, g, getBackground());
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Để trống vì đã vẽ viền trong paintComponent để đồng bộ
        }
    }

    public static class RoundedPasswordField extends JPasswordField {

        public RoundedPasswordField(int radius) {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRoundedStyle(this, g, getBackground());
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Để trống để tránh vẽ đè viền mặc định của PasswordField
        }
    }

    public static class RoundedButton extends JButton {

        public RoundedButton(String text, int radius) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Hàm static để bo góc và chỉnh UI cho ComboBox
    public static void customizeComboBox(JComboBox comboBox) {
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                // Làm nút mũi tên tàng hình hoặc phẳng
                JButton button = new JButton();
                button.setContentAreaFilled(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vẽ nền trắng bo góc 10px (chỉnh số này cho khớp với TextField của bạn)
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);

                // Vẽ viền xám nhạt bo góc
                g2.setColor(new Color(204, 204, 204));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);

                super.paint(g, c);
            }
        });

        // Xóa bỏ các viền xanh/đen mặc định khi click vào
        comboBox.setFocusable(false);
        comboBox.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        comboBox.setBackground(Color.WHITE);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

}
