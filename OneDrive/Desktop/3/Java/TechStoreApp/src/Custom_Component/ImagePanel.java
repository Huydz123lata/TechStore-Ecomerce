package Custom_Component;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private Icon image; // Đổi tên và kiểu dữ liệu thành Icon để NetBeans hỗ trợ
    private int round = 30;

    public ImagePanel() {
        setOpaque(false);
    }

    // Getter/Setter cho Icon - NetBeans sẽ hiện trình chọn ảnh chuẩn tại đây
    public Icon getImage() {
        return image;
    }

    public void setImage(Icon image) {
        this.image = image;
        repaint();
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Tạo khung bo tròn
        g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), round, round));

        if (image != null) {
            // Chuyển Icon thành Image để vẽ và co giãn (Scale)
            Image img = ((ImageIcon) image).getImage();
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Nếu chưa có ảnh thì vẽ nền mặc định (tùy chọn)
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), round, round);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
