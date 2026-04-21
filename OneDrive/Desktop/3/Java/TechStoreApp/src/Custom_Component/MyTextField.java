package Custom_Component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class MyTextField extends JTextField {

    private int round = 15;
    private Color borderColor = new Color(200, 200, 200); // Màu viền mặc định
    private Color colorFocus = new Color(13, 110, 253);   // Màu viền khi click vào (Xanh dương)

    public MyTextField() {
        // 1. Loại bỏ nền và viền mặc định của Swing
        setOpaque(false);
        setBackground(Color.WHITE);

        // 2. Padding để chữ không chạm vào viền
        setBorder(new EmptyBorder(5, 15, 5, 15));

        // 3. Thêm sự kiện Focus để bắt khoảnh khắc người dùng click vào ô
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint(); // Gọi vẽ lại để lên màu đậm
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint(); // Gọi vẽ lại để trả về màu nhạt
            }
        });
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
        repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public Color getColorFocus() {
        return colorFocus;
    }

    public void setColorFocus(Color colorFocus) {
        this.colorFocus = colorFocus;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền trắng bo tròn (đã lùi tọa độ để không bị cắt viền)
        g2.setColor(getBackground());
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, round, round);

        g2.dispose();

        // Để Java vẽ chữ (Text)
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Kiểm tra xem ô chữ có đang được người dùng click vào (Focus) không?
        if (hasFocus()) {
            g2.setColor(colorFocus); // Dùng màu Focus
            g2.setStroke(new BasicStroke(2f)); // Làm viền đậm lên (dày 2 pixel)
        } else {
            g2.setColor(borderColor); // Dùng màu mặc định
            g2.setStroke(new BasicStroke(1f)); // Viền mỏng bình thường (dày 1 pixel)
        }

        // Vẽ đường viền (lùi vào trong để nét vẽ 2px không bị khung chém mất)
        g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, round, round);

        g2.dispose();
    }
}
