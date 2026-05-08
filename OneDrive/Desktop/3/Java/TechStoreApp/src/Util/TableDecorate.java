package Util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class TableDecorate {

    //active
    public class StatusRenderer extends DefaultTableCellRenderer {

        private ImageIcon activeIcon;
        private ImageIcon inactiveIcon;

        public StatusRenderer() {
            try {
                java.net.URL activeURL = getClass().getResource("/Resource/green_dot.png");
                java.net.URL inactiveURL = getClass().getResource("/Resource/red_dot.png");

                if (activeURL != null) {
                    activeIcon = new ImageIcon(activeURL);
                }
                if (inactiveURL != null) {
                    inactiveIcon = new ImageIcon(inactiveURL);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);

            if (value == null) {
                label.setIcon(null);
                label.setText("");
                return label;
            }

            String status = value.toString().trim();

            if ("ACTIVE".equalsIgnoreCase(status)) {
                label.setIcon(activeIcon);
                label.setText("Hoạt động");
                label.setForeground(new Color(40, 167, 69));
            } else {
                label.setIcon(inactiveIcon);
                label.setText("Không hoạt động");
                label.setForeground(new Color(220, 53, 69));
            }

            label.setIconTextGap(10);
            return label;
        }
    }
}
