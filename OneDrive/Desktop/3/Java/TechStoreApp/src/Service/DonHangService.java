package Service;

import DAO.DonHangDAO;
import java.util.List;

public class DonHangService {
    private final DonHangDAO dao = new DonHangDAO();

    public List<Object[]> getAllOrders() {
        return dao.selectAll();
    }

    public List<Object[]> searchOrders(String keyword, String status, String payment) {
        return dao.selectByCondition(keyword, status, payment);
    }
    public boolean updateOrderStatus(int orderId, String newStatus) {
    // Gọi xuống tầng DAO để thực thi SQL
    return dao.updateStatus(orderId, newStatus);
    }
}