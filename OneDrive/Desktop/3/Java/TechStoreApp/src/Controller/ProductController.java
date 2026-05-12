/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.ProductDAO;
import Model.BrandModel;
import Model.CategoryModel;
import Model.ProductModel;
import java.util.List;
import javax.swing.JComboBox;

/**
 *
 * @author HUY0406
 */
public class ProductController {

    private final ProductDAO productDAO = new ProductDAO();

    public void updateDataCategoryCbx(JComboBox comboBox) {
        List<CategoryModel> list = productDAO.getAllCategoryName();
        comboBox.removeAllItems();
        comboBox.addItem("--Chọn danh mục SP--");
        if (list != null) {
            for (CategoryModel c : list) {
                comboBox.addItem(c);
            }
        }
    }

    public void updateDataBrandCbx(JComboBox comboBox) {
        List<BrandModel> list = productDAO.getAllBrandName();
        comboBox.removeAllItems();
        comboBox.addItem("--Chọn danh mục SP--");
        if (list != null) {
            for (BrandModel c : list) {
                comboBox.addItem(c);
            }
        }
    }

}
