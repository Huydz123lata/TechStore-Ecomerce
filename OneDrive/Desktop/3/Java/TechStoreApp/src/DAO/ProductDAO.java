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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class ProductDAO {

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

    public boolean upsertProduct(ProductModel pm) {
        // 1. Kiểm tra xem sản phẩm đã tồn tại theo TÊN chưa
        String sqlCheck = "SELECT STOCK_QUANTITY FROM PRODUCT WHERE UPPER(NAME) = UPPER(?)";

        try (Connection con = ConnectionUtils.getMyConnection(); // Sử dụng hàm kết nối của ba
                 PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {

            psCheck.setString(1, pm.getName());
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                // TRƯỜNG HỢP 1: ĐÃ CÓ TRONG DATABASE -> CỘNG DỒN STOCK
                String sqlUpdate = "UPDATE PRODUCT SET STOCK_QUANTITY = STOCK_QUANTITY + ? WHERE NAME = ?";
                try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                    psUpdate.setInt(1, pm.getStockQuantity());
                    psUpdate.setString(2, pm.getName());
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                // TRƯỜNG HỢP 2: CHƯA CÓ TRONG DATABASE -> INSERT MỚI
                // Gọi lại cái hàm insertProduct cũ mà ba đã viết
                return insertProduct(pm);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ProductModel> getAllProducts() {
        List<ProductModel> list = new ArrayList<>();
        String sql = "SELECT p.PRODUCT_ID, p.NAME AS PRO_NAME, p.PRICE, p.STOCK_QUANTITY, "
                + "p.IMAGE_NAME, p.WARRANTY_MONTH, p.DESCRIPTION, p.STATUS, "
                + "p.CATEGORY_ID, p.BRAND_ID, "
                + "c.NAME AS CAT_NAME, b.NAME AS BRAND_NAME, "
                + "/* Subquery lấy số tiền giảm giá lớn nhất của chiến dịch đang kích hoạt */ "
                + "COALESCE(("
                + "    SELECT MAX(pm.DISCOUNT_VALUE) "
                + "    FROM PROMOTION_PRODUCT pm "
                + "    INNER JOIN PROMOTION pr ON pm.PROMOTION_ID = pr.PROMOTION_ID "
                + "    WHERE pm.PRODUCT_ID = p.PRODUCT_ID "
                + "      AND pr.IS_ACTIVE = 1 "
                + "      AND pr.IS_DELETED = 0 "
                + "      AND SYSDATE BETWEEN pr.START_AT AND pr.END_AT"
                + "), 0) AS DISCOUNT_AMOUNT, "
                + "(SELECT COALESCE(SUM(od.QUANTITY), 0) FROM ORDER_DETAIL od WHERE od.PRODUCT_ID = p.PRODUCT_ID) AS SOLD_QUANTITY "
                + "FROM PRODUCT p "
                + "INNER JOIN CATEGORY c ON p.CATEGORY_ID = c.CATEGORY_ID "
                + "INNER JOIN BRAND b ON p.BRAND_ID = b.BRAND_ID "
                + "ORDER BY p.PRODUCT_ID DESC";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProductModel p = new ProductModel();

                //Đổ dữ liệu sản phẩm cơ bản
                p.setProductId(rs.getInt("PRODUCT_ID"));
                p.setName(rs.getString("PRO_NAME"));
                p.setPrice(rs.getDouble("PRICE")); // Đây là giá gốc
                p.setStockQuantity(rs.getInt("STOCK_QUANTITY"));
                p.setImage(rs.getString("IMAGE_NAME"));
                p.setWarrantyMonth(rs.getInt("WARRANTY_MONTH"));
                p.setDescription(rs.getString("DESCRIPTION"));
                p.setStatus(rs.getInt("STATUS"));
                p.setSoldQuantity(rs.getInt("SOLD_QUANTITY"));
                p.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));

                // Đóng gói Category (Danh mục)
                CategoryModel cat = new CategoryModel();
                cat.setCategoryId(rs.getInt("CATEGORY_ID"));
                cat.setName(rs.getString("CAT_NAME"));
                p.setCategory(cat);

                // Đóng gói Brand (Thương hiệu)
                BrandModel brand = new BrandModel();
                brand.setBrandId(rs.getInt("BRAND_ID"));
                brand.setName(rs.getString("BRAND_NAME"));
                p.setBrand(brand);

                // Thêm đối tượng vào danh sách kết quả
                list.add(p);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tại getAllProducts: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductModel> getProductsByCategory(int categoryId) {
        List<ProductModel> list = new ArrayList<>();
        String sql = "SELECT p.PRODUCT_ID, p.NAME AS PRO_NAME, p.PRICE, p.STOCK_QUANTITY, "
                + "p.IMAGE_NAME, p.WARRANTY_MONTH, p.DESCRIPTION, p.STATUS, "
                + "p.CATEGORY_ID, p.BRAND_ID, "
                + "c.NAME AS CAT_NAME, b.NAME AS BRAND_NAME, "
                + "COALESCE(("
                + "    SELECT MAX(pm.DISCOUNT_VALUE) "
                + "    FROM PROMOTION_PRODUCT pm "
                + "    INNER JOIN PROMOTION pr ON pm.PROMOTION_ID = pr.PROMOTION_ID "
                + "    WHERE pm.PRODUCT_ID = p.PRODUCT_ID "
                + "      AND pr.IS_ACTIVE = 1 "
                + "      AND pr.IS_DELETED = 0 "
                + "      AND SYSDATE BETWEEN pr.START_AT AND pr.END_AT"
                + "), 0) AS DISCOUNT_AMOUNT, "
                + "(SELECT COALESCE(SUM(od.QUANTITY), 0) FROM ORDER_DETAIL od WHERE od.PRODUCT_ID = p.PRODUCT_ID) AS SOLD_QUANTITY "
                + "FROM PRODUCT p "
                + "INNER JOIN CATEGORY c ON p.CATEGORY_ID = c.CATEGORY_ID "
                + "INNER JOIN BRAND b ON p.BRAND_ID = b.BRAND_ID "
                + "WHERE p.CATEGORY_ID = ? /* Lọc đúng ID danh mục được chọn */ "
                + "ORDER BY p.PRODUCT_ID DESC";

        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            // Truyền ID danh mục vào dấu chấm hỏi
            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductModel p = new ProductModel();
                    p.setProductId(rs.getInt("PRODUCT_ID"));
                    p.setName(rs.getString("PRO_NAME"));
                    p.setPrice(rs.getDouble("PRICE"));
                    p.setStockQuantity(rs.getInt("STOCK_QUANTITY"));
                    p.setImage(rs.getString("IMAGE_NAME"));
                    p.setWarrantyMonth(rs.getInt("WARRANTY_MONTH"));
                    p.setDescription(rs.getString("DESCRIPTION"));
                    p.setStatus(rs.getInt("STATUS"));
                    p.setSoldQuantity(rs.getInt("SOLD_QUANTITY"));
                    p.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));

                    CategoryModel cat = new CategoryModel();
                    cat.setCategoryId(rs.getInt("CATEGORY_ID"));
                    cat.setName(rs.getString("CAT_NAME"));
                    p.setCategory(cat);

                    BrandModel brand = new BrandModel();
                    brand.setBrandId(rs.getInt("BRAND_ID"));
                    brand.setName(rs.getString("BRAND_NAME"));
                    p.setBrand(brand);

                    list.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tại getProductsByCategory: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    //ChiTietSanPhamdialog
    public boolean updateStatus(int productId, int statusValue) {
        String sql = "UPDATE PRODUCT SET STATUS = ? WHERE PRODUCT_ID = ?";
        try (Connection conn = ConnectionUtils.getMyConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, statusValue); // Số 1 hoặc 0
            pstmt.setInt(2, productId);   // ID sản phẩm

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
