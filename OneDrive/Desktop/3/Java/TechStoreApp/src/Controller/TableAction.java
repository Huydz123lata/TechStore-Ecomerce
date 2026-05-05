package Controller;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class TableAction {

    // 1. Lớp vẽ giao diện 2 nút Sửa/Xóa
    public static class ActionRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

            JButton btnDelete = new JButton("Xóa");

            // Style nút cho giống mẫu
            btnDelete.setForeground(Color.RED);
            btnDelete.setBackground(Color.WHITE);

            panel.add(btnDelete);

            // Giữ màu nền đồng bộ khi chọn hàng
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            return panel;
        }
    }

    // 2. Lớp xử lý khi người dùng thực sự click vào nút
    public static class ActionEditor extends DefaultCellEditor {

        public ActionEditor() {
            super(new JCheckBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            JButton btnDelete = new JButton("Xóa");

            // Bắt sự kiện nút Xóa
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                int confirm = JOptionPane.showConfirmDialog(null, "Xác nhận xóa hàng này?", "Thông báo", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Gọi hàm xóa từ Model/DAO của bạn ở đây
                    System.out.println("Đã xóa hàng: " + row);
                }
            });

            panel.add(btnDelete);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
