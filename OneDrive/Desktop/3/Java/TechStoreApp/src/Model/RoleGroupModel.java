/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author HUY0406
 */
public class RoleGroupModel {

    private int roleGroupId;
    private String roleGroupName;

    public RoleGroupModel() {
    }

    public RoleGroupModel(int id, String name) {
        this.roleGroupId = id;
        this.roleGroupName = name;
    }

    public int getRoleGroupId() {
        return roleGroupId;
    }

    public void setRoleGroupId(int roleGroupId) {
        this.roleGroupId = roleGroupId;
    }

    public void setRoleGroupName(String roleGroupName) {
        this.roleGroupName = roleGroupName;
    }

    public String getRoleGroupName() {
        return roleGroupName;
    }

    @Override
    public String toString() {
        return roleGroupName;
    }
}
