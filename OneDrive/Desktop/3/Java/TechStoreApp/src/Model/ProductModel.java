package Model;

public class ProductModel {

    private int productId;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private int status;
    private int warrantyMonth;
    private String image;
    private CategoryModel category;
    private BrandModel brand;

    public ProductModel() {
        this.category = new CategoryModel();
        this.brand = new BrandModel();
    }

    // --- GETTERS VÀ SETTERS CHO ĐỐI TƯỢNG ---
    public CategoryModel getCategory() {
        return category;
    }

    public void setCategory(CategoryModel category) {
        this.category = category;
    }

    public BrandModel getBrand() {
        return brand;
    }

    public void setBrand(BrandModel brand) {
        this.brand = brand;
    }

    // --- CÁC HÀM TIỆN ÍCH (Để lấy ID nhanh) ---
    public int getCategoryId() {
        return category != null ? category.getCategoryId() : 0;
    }

    public int getBrandId() {
        return brand != null ? brand.getBrandId() : 0;
    }

    // --- CÁC GETTER/SETTER CÒN LẠI (Price, Name, Stock...) ---
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getWarrantyMonth() {
        return warrantyMonth;
    }

    public void setWarrantyMonth(int warrantyMonth) {
        this.warrantyMonth = warrantyMonth;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
