/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.FunctionDAO;
import DAO.RoleDAO;
import DAO.RoleGroupDAO;
import Model.FunctionModel;
import Model.RoleModel;
import Model.RoleGroupModel;
import java.awt.Font;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HUY0406
 */
public class adminPageController {

    private final RoleDAO roleDAO = new RoleDAO();
    private final RoleGroupDAO rolegroupDAO = new RoleGroupDAO();
    private final FunctionDAO functionDAO = new FunctionDAO();

    public void setUpTableRoleScope(JTable table, int col) {

        table.setRowHeight(30);
        table.putClientProperty("JTable.showAlternateRowColor", true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 2; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(col).setCellRenderer(new TableAction.ActionRenderer());
        table.getColumnModel().getColumn(col).setCellEditor(new TableAction.ActionEditor());
    }

    public void setUpTable(JTable table, int col) {

        table.setRowHeight(30);
        table.putClientProperty("JTable.showAlternateRowColor", true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(col).setCellRenderer(new TableAction.ActionRenderer());
        table.getColumnModel().getColumn(col).setCellEditor(new TableAction.ActionEditor());
    }

    //Role_permission management
    public void loadDataToTableRoleScope(JTable table) {

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        List<FunctionModel> listFunctions = functionDAO.getAllFunctionName();

        for (FunctionModel f : listFunctions) {
            model.addRow(new Object[]{
                f.getFunctionId(),
                f.getNameFunction(),
                false,
                false,
                false,
                false,
                false,
                ""
            });
        }
    }

    //RoleGroup
    public void loadDataToTableRoleGroup(JTable table) {

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        int stt = 1;
        List<RoleGroupModel> listRoleGroup = rolegroupDAO.getAllRoleGroupName();

        if (listRoleGroup != null) {
            for (RoleGroupModel group : listRoleGroup) {
                model.addRow(new Object[]{
                    stt++,
                    group.getRoleGroupId(),
                    group.getGroupName(),
                    "",});
            }
        }
    }

    public void updateRoleGroupComboBox(JComboBox comboBox) {
        List<RoleGroupModel> list = rolegroupDAO.getAllRoleGroupName();
        comboBox.removeAllItems();
        comboBox.addItem("-- Chọn nhóm quyền --");
        if (list != null) {
            for (RoleGroupModel model : list) {
                comboBox.addItem(model);
            }
        }
    }

    public void displayPermissionsToTable(JTable table, int groupId) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // (đưa về false hết)
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int col = 2; col <= 6; col++) {
                model.setValueAt(false, i, col);
            }
        }

        // Lấy dữ liệu từ DB
        List<RoleModel> listRole = rolegroupDAO.getRolesByGroupId(groupId);

        // BƯỚC 3: Duyệt và tích Checkbox
        for (RoleModel r : listRole) {
            for (int i = 0; i < model.getRowCount(); i++) {
                // Lấy tên chức năng trên dòng hiện tại của bảng
                String funcNameInTable = model.getValueAt(i, 1).toString();

                // So sánh với tên chức năng lấy từ DB
                if (funcNameInTable.equalsIgnoreCase(r.getFunctionName())) {
                    model.setValueAt(r.getCanAdd() == 1, i, 2);
                    model.setValueAt(r.getCanEdit() == 1, i, 3);
                    model.setValueAt(r.getCanDelete() == 1, i, 4);
                    model.setValueAt(r.getCanDownload() == 1, i, 5);
                    model.setValueAt(r.getCanView() == 1, i, 6);
                    break;
                }
            }
        }
    }

    //Role
    public void loadDataToTableRole(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); //xóa sạch bảng cũ

        int stt = 1;
        List<RoleModel> roleName = roleDAO.getAllRoleName();

        if (roleName != null) {
            for (RoleModel r : roleName) {
                model.addRow(new Object[]{
                    stt++,
                    r.getRoleID(),
                    r.getRoleName(),
                    ""});
            }
        }
    }

    public void updateFunctionNameToComboBox(JComboBox comboBox) {
        List<FunctionModel> list = functionDAO.getAllFunctionName();
        comboBox.removeAllItems();
        comboBox.addItem("-- Chọn chức năng --");
        if (list != null) {
            for (FunctionModel l : list) {
                comboBox.addItem(l);
            }
        }
    }

}
