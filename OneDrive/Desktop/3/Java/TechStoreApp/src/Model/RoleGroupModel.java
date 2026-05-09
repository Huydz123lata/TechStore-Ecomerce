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
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public RoleGroupModel(int id, String name) {
        this.roleGroupId = id;
        this.groupName = name;
    }

    public int getRoleGroupId() {
        return roleGroupId;
    }

    @Override
    public String toString() {
        return groupName;
    }
}
