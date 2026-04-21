package Custom_Component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class MyButton extends JButton {

    private int round = 20;
    private Color colorHover = new Color(200, 200, 200); // Mặc định xám nhẹ, bạn có thể đổi sau
    private Color colorClick = new Color(150, 150, 150);
    private Color colorOriginal;

    public MyButton() {
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        // Lắng nghe sự kiện chuột để đổi màu
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                colorOriginal = getBackground(); // Lưu lại màu bạn đã set trong Properties
                setBackground(colorHover);
            }

            @Override
            public void mouseExited(MouseEvent me) {
                setBackground(colorOriginal); // Trả về màu bạn đã set
            }

            @Override
            public void mousePressed(MouseEvent me) {
                setBackground(colorClick);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                setBackground(colorHover);
            }
        });
    }

    // Các hàm Getter/Setter để hiển thị thuộc tính trong bảng Properties của NetBeans
    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
        repaint();
    }

    public Color getColorHover() {
        return colorHover;
    }

    public void setColorHover(Color colorHover) {
        this.colorHover = colorHover;
    }

    public Color getColorClick() {
        return colorClick;
    }

    public void setColorClick(Color colorClick) {
        this.colorClick = colorClick;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền nút bằng màu lấy từ thuộc tính Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), round, round);

        g2.dispose();
        super.paintComponent(g);
    }
}
