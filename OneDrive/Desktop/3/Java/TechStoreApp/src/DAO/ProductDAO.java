/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.BrandModel;
import Model.CategoryModel;
import Model.ProductModel;
import config.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class ProductDAO {

//    public boolean insertProduct(ProductModel p, String categoryName) {
//        Connection con = null;
//        try {
//            con = ConnectionUtils.getMyConnection();
//            con.setAutoCommit(false); // Bắt đầu Transaction
//
//            // --- BƯỚC 1: XỬ LÝ DANH MỤC (Lấy ID cũ hoặc chèn mới) ---
//            int categoryId = 0;
//            String sqlCheckCat = "SELECT CATEGORY_ID FROM CATEGORY WHERE NAME = ?";
//
//            try (PreparedStatement psCheck = con.prepareStatement(sqlCheckCat)) {
//                psCheck.setString(1, categoryName);
//                try (ResultSet rs = psCheck.executeQuery()) {
//                    if (rs.next()) {
//                        categoryId = rs.getInt("CATEGORY_ID");
//                    }
//                }
//            }
//
//            // Nếu chưa có danh mục này thì chèn mới để lấy ID
//            if (categoryId == 0) {
//                String sqlInsertCat = "INSERT INTO CATEGORY (NAME) VALUES (?)";
//                String[] colID = {"CATEGORY_ID"};
//                try (PreparedStatement psInsCat = con.prepareStatement(sqlInsertCat, colID)) {
//                    psInsCat.setString(1, categoryName);
//                    psInsCat.executeUpdate();
//                    try (ResultSet rsCat = psInsCat.getGeneratedKeys()) {
//                        if (rsCat.next()) {
//                            categoryId = rsCat.getInt(1);
//                        }
//                    }
//                }
//            }
//
//            if (categoryId == 0) {
//                throw new Exception("Lỗi: Không xử lý được Category ID");
//            }
//
//            // --- BƯỚC 2: THÊM SẢN PHẨM VỚI CATEGORY_ID VỪA LẤY ---
//            String sqlProd = "INSERT INTO PRODUCT (NAME, DESCRIPTION, PRICE, STOCK_QUANTITY, BRAND, STATUS, WARRANTY_MONTH, CATEGORY_ID) "
//                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//
//            try (PreparedStatement psProd = con.prepareStatement(sqlProd)) {
//                psProd.setString(1, p.getName());
//                psProd.setString(2, p.getDescription());
//                psProd.setDouble(3, p.getPrice());
//                psProd.setInt(4, p.getStockQuantity());
//                psProd.setString(5, p.getBrand());
//                psProd.setInt(6, p.getStatus()); // 0 hoặc 1
//                psProd.setInt(7, p.getWarrantyMonth());
//                psProd.setInt(8, categoryId); // Dùng ID vừa xử lý ở bước 1
//
//                psProd.executeUpdate();
//            }
//
//            con.commit(); // Hoàn tất mọi thứ thành công
//            return true;
//
//        } catch (Exception e) {
//            if (con != null) {
//                try {
//                    con.rollback();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                }
//            }
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (con != null) {
//                try {
//                    con.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    public List<ProductModel> getAllProduct() {
//        List<ProductModel> list = new ArrayList<>();
//        String sql = "SELECT p.*, c.NAME AS CATEGORY_NAME "
//                + "FROM PRODUCT p "
//                + "JOIN CATEGORY c ON p.CATEGORY_ID = c.CATEGORY_ID "
//                + "WHERE p.IS_DELETED = 0 "
//                + "ORDER BY p.PRODUCT_ID DESC";
//
//        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
//
//            while (rs.next()) {
//                ProductModel p = new ProductModel();
//
//                // Đọc dữ liệu từ các cột trong Database
//                p.setProductId(rs.getInt("PRODUCT_ID"));
//                p.setName(rs.getString("NAME"));
//                p.setDescription(rs.getString("DESCRIPTION"));
//                p.setPrice(rs.getDouble("PRICE"));
//                p.setStockQuantity(rs.getInt("STOCK_QUANTITY"));
//                p.setBrand(rs.getString("BRAND"));
//                p.setStatus(rs.getInt("STATUS"));
//                p.setWarrantyMonth(rs.getInt("WARRANTY_MONTH"));
//                p.setCategoryId(rs.getInt("CATEGORY_ID"));
//
//                // ĐÂY LÀ CHỖ QUAN TRỌNG: Lấy tên danh mục từ cột đã JOIN
//                p.setCategoryName(rs.getString("CATEGORY_NAME"));
//
//                // Thêm vào danh sách trả về
//                list.add(p);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//    public boolean deleteProduct(int productId) {
//        String sql = "DELETE FROM PRODUCT WHERE PRODUCT_ID = ?";
//        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, productId);
//            int rowsDeleted = ps.executeUpdate();
//
//            return rowsDeleted > 0;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
    //CATEGORY
    public List<CategoryModel> getAllCategoryName() {
        List<CategoryModel> list = new ArrayList<>();
        String sql = "SELECT CATEGORY_ID, NAME "
                + "FROM CATEGORY "
                + "WHERE IS_DELETED = 0 ";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CategoryModel c = new CategoryModel();
                c.setCategoryId(rs.getInt("CATEGORY_ID"));
                c.setName(rs.getString("NAME"));
                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertCategory(String name) {
        String sql = "INSERT INTO CATEGORY (NAME) VALUES (?)";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        String sql = "UPDATE CATEGORY "
                + "SET IS_DELETED = 1 "
                + "WHERE CATEGORY_ID = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //BRAND
    public List<BrandModel> getAllBrandName() {
        List<BrandModel> list = new ArrayList<>();
        String sql = "SELECT BRAND_ID, NAME "
                + "FROM BRAND "
                + "WHERE IS_DELETED = 0 ";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BrandModel c = new BrandModel();
                c.setBrandId(rs.getInt("BRAND_ID"));
                c.setName(rs.getString("NAME"));
                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertBRAND(String name) {
        String sql = "INSERT INTO BRAND (NAME) VALUES (?)";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBRAND(int id) {
        String sql = "UPDATE BRAND "
                + "SET IS_DELETED = 1 "
                + "WHERE BRAND_ID = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //product
    public boolean insertProduct(ProductModel pm) {

        String sql = "INSERT INTO PRODUCT (NAME, DESCRIPTION, PRICE, STOCK_QUANTITY, BRAND_ID, WARRANTY_MONTH, CATEGORY_ID, IMAGE_NAME) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pm.getName());
            ps.setString(2, pm.getDescription());
            ps.setDouble(3, pm.getPrice());
            ps.setInt(4, pm.getStockQuantity());
            ps.setInt(5, pm.getBrand().getBrandId());
            ps.setInt(6, pm.getWarrantyMonth());
            ps.setInt(7, pm.getCategory().getCategoryId());
            ps.setString(8, pm.getImage());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
