/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author HUY0406
 */
public class CategoryModel {

    private int categoryId;
    private String name;
    private int isDeleted; // 0: Hoạt động, 1: Đã xóa

    // Constructor không tham số
    public CategoryModel() {
    }

    // Constructor đầy đủ tham số
    public CategoryModel(int categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    // Getter và Setter
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return name;
    }
}
