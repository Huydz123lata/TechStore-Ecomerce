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

    public void addPromotion(String code, String name, String type, double value, java.util.Date startDate, java.util.Date endDate, String status) throws Exception {
        dao.insertPromotion(code, name, type, value, startDate, endDate, status);
    }
}