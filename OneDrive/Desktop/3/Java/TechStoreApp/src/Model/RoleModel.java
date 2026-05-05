/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author HUY0406
 */
public class RoleModel {

    private int roleID;
    private int functionID;
    private String roleName;
    private String functionName;
    private int canAdd;
    private int canEdit;
    private int canDelete;
    private int canView;
    private int canDownload;

    public RoleModel() {
        this.functionName = "";
        this.canAdd = 0;
        this.canEdit = 0;
        this.canDelete = 0;
        this.canView = 0;
        this.canDownload = 0;
    }

    // Constructor đầy đủ tham số
    public RoleModel(int roleID, int functionID, String roleName, String functionName,
            int canAdd, int canEdit, int canDelete, int canView, int canDownload) {
        this.roleID = roleID;
        this.functionID = functionID;
        this.roleName = roleName;
        this.functionName = functionName;
        this.canAdd = canAdd;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canView = canView;
        this.canDownload = canDownload;
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getCanAdd() {
        return canAdd;
    }

    public int getCanEdit() {
        return canEdit;
    }

    public int getCanDelete() {
        return canDelete;
    }

    public int getCanView() {
        return canView;
    }

    public int getCanDownload() {
        return canDownload;
    }

    public int getRoleID() {
        return roleID;
    }

    public int getFunctionID() {
        return functionID;
    }

    public String getRoleName() {
        return roleName;
    }

}
