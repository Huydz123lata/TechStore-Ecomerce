package Service;

import DAO.KhuyenMaiDAO;
import java.util.List;

public class KhuyenMaiService {
    private final KhuyenMaiDAO dao = new KhuyenMaiDAO();

    public List<Object[]> getAllPromotions() {
        return dao.selectAll();
    }

    public List<Object[]> searchPromotions(String keyword, String status) {
        return dao.selectByCondition(keyword, status);
    }

    public boolean updatePromotionStatus(String promoCode, String newStatus) {
        return dao.updateStatus(promoCode, newStatus);
    }

    public void addPromotion(String code, String name, String type, double value, double minOrder, double maxDiscount, java.util.Date startDate, java.util.Date endDate, String status) throws Exception {
        dao.insertPromotion(code, name, type, value, minOrder, maxDiscount, startDate, endDate, status);
    }
    public boolean deletePromotion(String promoCode) {
        return dao.deletePromotion(promoCode);
    }
    public java.util.List<Object[]> getAllPro() {
        return dao.selectAllPro();
    }

    public java.util.List<Object[]> searchPro(String keyword, String status) {
        return dao.selectByConditionPro(keyword, status);
    }

    public boolean updateProStatus(String proCode, String newStatus) {
        return dao.updateStatusPro(proCode, newStatus);
    }

    public boolean deletePro(String proCode) {
        return dao.deletePromotionPro(proCode);
    }

    public boolean updateCouponData(String oldCode, String newCode, String name, String type, double value, double minOrder, double maxDiscount, java.util.Date startDate, java.util.Date endDate) {
        return dao.updateCoupon(oldCode, newCode, name, type, value, minOrder, maxDiscount, startDate, endDate);
    }
}