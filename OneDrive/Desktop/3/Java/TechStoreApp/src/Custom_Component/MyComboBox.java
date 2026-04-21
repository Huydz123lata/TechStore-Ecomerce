package Custom_Component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class MyComboBox<E> extends JComboBox<E> {

    private int round = 15;
    private Color borderColor = new Color(200, 200, 200);
    private Color colorFocus = new Color(13, 110, 253);

    public MyComboBox() {
        setOpaque(false);
        setBackground(Color.WHITE);
        // GIẢM lề phải (từ 10 xuống 2) để dành không gian cho nút mũi tên
        setBorder(new EmptyBorder(5, 10, 5, 2));

        setUI(new BasicComboBoxUI() {

            protected void paintFocus(Graphics g, Rectangle bounds, Component c) {
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            }

            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int width = getWidth();
                        int height = getHeight();
                        int size = 8; // Kích thước mũi tên

                        // Căn giữa mũi tên trong nút
                        int x = (width - size) / 2;
                        int y = (height - size / 2) / 2;

                        g2.setColor(new Color(100, 100, 100)); // Màu xám đậm cho rõ
                        int[] px = {x, x + size, x + size / 2};
                        int[] py = {y, y, y + size / 2};
                        g2.fillPolygon(px, py, 3);
                        g2.dispose();
                    }
                };
                button.setPreferredSize(new Dimension(30, 30)); // Cố định kích thước nút
                button.setContentAreaFilled(false);
                button.setBorder(null);
                return button;
            }
        });

        // Làm đẹp danh sách xổ xuống
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(8, 10, 8, 10));
                if (index != -1) {
                    if (isSelected) {
                        setBackground(colorFocus);
                        setForeground(Color.WHITE);
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, round, round);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (hasFocus()) {
            g2.setColor(colorFocus);
            g2.setStroke(new BasicStroke(2f));
        } else {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
        }
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, round, round);
        g2.dispose();
    }
}
