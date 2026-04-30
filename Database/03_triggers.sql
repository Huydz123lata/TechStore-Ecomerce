-- ==============================================================================
-- NHÓM 1: QUẢN LÝ ĐƠN HÀNG & THANH TOÁN
-- ==============================================================================

-- 1. Tự động cộng/trừ tiền vào TOTAL của ORDERS khi thêm/xóa ORDER_DETAIL
-- (Xử lý thuật toán cộng dồn để tránh lỗi Mutating Table của Oracle)
CREATE OR REPLACE TRIGGER TRG_ORDER_TOTAL_CALC
AFTER INSERT OR UPDATE OR DELETE ON ORDER_DETAIL
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        UPDATE ORDERS SET TOTAL = TOTAL + (:NEW.PRICE * :NEW.QUANTITY)
        WHERE ORDER_ID = :NEW.ORDER_ID;
    ELSIF UPDATING THEN
        UPDATE ORDERS SET TOTAL = TOTAL - (:OLD.PRICE * :OLD.QUANTITY) + (:NEW.PRICE * :NEW.QUANTITY)
        WHERE ORDER_ID = :NEW.ORDER_ID;
    ELSIF DELETING THEN
        UPDATE ORDERS SET TOTAL = TOTAL - (:OLD.PRICE * :OLD.QUANTITY)
        WHERE ORDER_ID = :OLD.ORDER_ID;
    END IF;
END;
/

-- 2. Đồng bộ trạng thái đơn hàng khi Thanh toán (PAYMENT) thành công
CREATE OR REPLACE TRIGGER TRG_PAYMENT_ORDER_SYNC
AFTER UPDATE OF PAYMENT_STATUS ON PAYMENT FOR EACH ROW
WHEN (NEW.PAYMENT_STATUS = 'SUCCESS')
BEGIN
    UPDATE ORDERS SET STATUS = 'PROCESSING' WHERE ORDER_ID = :NEW.ORDER_ID;
END;
/

-- 3. Đảm bảo số tiền thanh toán không được nhỏ hơn tổng đơn hàng
CREATE OR REPLACE TRIGGER TRG_VALIDATE_PAYMENT_AMOUNT
BEFORE INSERT ON PAYMENT FOR EACH ROW
DECLARE
    v_total NUMBER;
BEGIN
    SELECT TOTAL INTO v_total FROM ORDERS WHERE ORDER_ID = :NEW.ORDER_ID;
    IF :NEW.AMOUNT < v_total THEN
        RAISE_APPLICATION_ERROR(-20020, 'Lỗi: Số tiền thanh toán không đủ để trả cho đơn hàng.');
    END IF;
END;
/

-- 4. Ngăn chặn hủy đơn hàng đã hoặc đang giao đi
CREATE OR REPLACE TRIGGER TRG_PREVENT_CANCEL_SHIPPED_ORDER
BEFORE UPDATE OF STATUS ON ORDERS FOR EACH ROW
BEGIN
    IF :NEW.STATUS = 'CANCELLED' AND :OLD.STATUS IN ('SHIPPING', 'DELIVERED') THEN
        RAISE_APPLICATION_ERROR(-20032, 'Không thể hủy đơn hàng đang trên đường giao hoặc đã giao thành công.');
    END IF;
END;
/

-- ==============================================================================
-- NHÓM 2: QUẢN LÝ KHO (INVENTORY) & SẢN PHẨM
-- ==============================================================================

-- 5. Khấu trừ tồn kho khi đặt hàng (Trừ đúng KHO được gán trong ORDERS)
CREATE OR REPLACE TRIGGER TRG_DEDUCT_INVENTORY
AFTER INSERT ON ORDER_DETAIL FOR EACH ROW
DECLARE
    v_warehouse_id NUMBER;
BEGIN
    -- Lấy ID kho xuất hàng từ bảng ORDERS
    SELECT WAREHOUSE_ID INTO v_warehouse_id FROM ORDERS WHERE ORDER_ID = :NEW.ORDER_ID;

    -- Trừ kho chính xác
    IF v_warehouse_id IS NOT NULL THEN
        UPDATE INVENTORY SET QUANTITY = QUANTITY - :NEW.QUANTITY
        WHERE PRODUCT_ID = :NEW.PRODUCT_ID AND WAREHOUSE_ID = v_warehouse_id;
    END IF;
END;
/

-- 6. Hoàn trả tồn kho khi đơn hàng bị HỦY (CANCELLED)
CREATE OR REPLACE TRIGGER TRG_RESTORE_INVENTORY
AFTER UPDATE OF STATUS ON ORDERS FOR EACH ROW
WHEN (NEW.STATUS = 'CANCELLED')
BEGIN
    IF :OLD.WAREHOUSE_ID IS NOT NULL THEN
        FOR r IN (SELECT PRODUCT_ID, QUANTITY FROM ORDER_DETAIL WHERE ORDER_ID = :NEW.ORDER_ID) LOOP
            UPDATE INVENTORY SET QUANTITY = QUANTITY + r.QUANTITY
            WHERE PRODUCT_ID = r.PRODUCT_ID AND WAREHOUSE_ID = :NEW.WAREHOUSE_ID;
        END LOOP;
    END IF;
END;
/

-- ==============================================================================
-- NHÓM 3: KHÁCH HÀNG, ĐỐI TÁC & REVIEW
-- ==============================================================================

-- 7. Chỉ cho phép Review nếu khách đã thực sự mua và nhận hàng
CREATE OR REPLACE TRIGGER TRG_REVIEW_VALIDATE_PURCHASE
BEFORE INSERT ON REVIEW FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM ORDERS o
    JOIN ORDER_DETAIL od ON o.ORDER_ID = od.ORDER_ID
    WHERE o.CUSTOMER_ID = :NEW.CUSTOMER_ID
      AND od.PRODUCT_ID = :NEW.PRODUCT_ID
      AND o.STATUS = 'DELIVERED';

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Khách hàng phải nhận hàng thành công mới được đánh giá.');
    END IF;
END;
/

-- 8. Tích điểm cho khách khi đơn hàng giao thành công (1% giá trị)
CREATE OR REPLACE TRIGGER TRG_LOYALTY_POINTS_ADD
AFTER UPDATE OF STATUS ON ORDERS FOR EACH ROW
WHEN (NEW.STATUS = 'DELIVERED')
BEGIN
    -- Cập nhật nếu khách đã có ví điểm
    UPDATE LOYALTY_POINT SET TOTAL_POINTS = TOTAL_POINTS + (:NEW.TOTAL * 0.01)
    WHERE CUSTOMER_ID = :NEW.CUSTOMER_ID;
END;
/

-- 9. Chia hoa hồng Affiliate (Thông qua COUPON mà khách dùng)
CREATE OR REPLACE TRIGGER TRG_AFFILIATE_COMMISSION
AFTER UPDATE OF STATUS ON ORDERS FOR EACH ROW
WHEN (NEW.STATUS = 'DELIVERED' AND NEW.COUPON_ID IS NOT NULL)
DECLARE
    v_affiliate_id NUMBER;
    v_rate NUMBER;
BEGIN
    -- Tìm xem mã coupon này có thuộc về Affiliate nào không
    SELECT AFFILIATE_ID INTO v_affiliate_id FROM COUPON WHERE COUPON_ID = :NEW.COUPON_ID;

    IF v_affiliate_id IS NOT NULL THEN
        -- Lấy tỷ lệ hoa hồng
        SELECT COMMISSION_RATE INTO v_rate FROM AFFILIATE WHERE AFFILIATE_ID = v_affiliate_id;

        -- Cộng tiền cho Affiliate
        UPDATE AFFILIATE
        SET TOTAL_EARNED = TOTAL_EARNED + (:NEW.TOTAL * (v_rate / 100))
        WHERE AFFILIATE_ID = v_affiliate_id;
    END IF;
END;
/

-- ==============================================================================
-- NHÓM 4: KHUYẾN MÃI (PROMOTION) & MUA CHUNG (GROUP BUY)
-- ==============================================================================

-- 10. Chặn cấu hình mã giảm giá (PROMOTION_PRODUCT) sai quy tắc
CREATE OR REPLACE TRIGGER TRG_CHECK_DISCOUNT_LIMIT
BEFORE INSERT OR UPDATE ON PROMOTION_PRODUCT FOR EACH ROW
BEGIN
    IF :NEW.DISCOUNT_TYPE = 'PERCENT' AND :NEW.DISCOUNT_VALUE > 100 THEN
        RAISE_APPLICATION_ERROR(-20033, 'Lỗi: Mức giảm giá theo phần trăm không được vượt quá 100%.');
    END IF;
END;
/

-- 11. Tự động tăng số người tham gia Mua chung (GROUP_BUY)
CREATE OR REPLACE TRIGGER TRG_GROUP_BUY_PARTICIPANT_INC
AFTER INSERT ON GROUP_MEMBER FOR EACH ROW
BEGIN
    UPDATE GROUP_BUY
    SET CUR_PARTICIPANTS = CUR_PARTICIPANTS + 1
    WHERE GROUP_ID = :NEW.GROUP_ID;
END;
/

-- 12. Chốt trạng thái Mua chung khi đủ người (Dựa trên số vừa được tăng ở Trigger 11)
CREATE OR REPLACE TRIGGER TRG_GROUP_BUY_SUCCESS_CHECK
AFTER UPDATE OF CUR_PARTICIPANTS ON GROUP_BUY FOR EACH ROW
BEGIN
    IF :NEW.CUR_PARTICIPANTS >= :NEW.MIN_PARTICIPANTS AND :NEW.STATUS = 'WAITING' THEN
        -- Không dùng UPDATE trực tiếp ở đây để tránh Mutating, ta gán trực tiếp giá trị vào :NEW
        :NEW.STATUS := 'SUCCESS';
    END IF;
END;
/

-- 13. Ngăn chặn chèn sản phẩm vào chương trình khuyến mãi đã hết hạn
CREATE OR REPLACE TRIGGER TRG_PROMOTION_DATE_VALIDATE
BEFORE INSERT ON PROMOTION_PRODUCT FOR EACH ROW
DECLARE
    v_end DATE;
BEGIN
    SELECT END_AT INTO v_end FROM PROMOTION WHERE PROMOTION_ID = :NEW.PROMOTION_ID;
    IF v_end < SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20015, 'Chương trình khuyến mãi này đã kết thúc, không thể thêm sản phẩm.');
    END IF;
END;
/

-- ==============================================================================
-- NHÓM 5: GIỎ HÀNG, SẢN PHẨM & TÀI KHOẢN (BỔ SUNG)
-- ==============================================================================

-- 14. Chặn đặt mua sản phẩm đã ngừng kinh doanh
CREATE OR REPLACE TRIGGER TRG_PREVENT_INACTIVE_PRODUCT_ORDER
BEFORE INSERT ON ORDER_DETAIL FOR EACH ROW
DECLARE
    v_status NUMBER;
BEGIN
    SELECT STATUS INTO v_status FROM PRODUCT WHERE PRODUCT_ID = :NEW.PRODUCT_ID;
    IF v_status = 0 THEN
        RAISE_APPLICATION_ERROR(-20040, 'Lỗi: Sản phẩm này đã ngừng kinh doanh, không thể đặt hàng.');
    END IF;
END;
/

-- 15. Kiểm tra tổng tồn kho trước khi thêm vào giỏ hàng
CREATE OR REPLACE TRIGGER TRG_CHECK_CART_ITEM_STOCK
BEFORE INSERT OR UPDATE ON CART_ITEM FOR EACH ROW
DECLARE
    v_total_stock NUMBER;
BEGIN
    -- Tính tổng tồn kho của sản phẩm ở tất cả các kho
    SELECT NVL(SUM(QUANTITY), 0) INTO v_total_stock
    FROM INVENTORY
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID AND IS_DELETED = 0;

    IF :NEW.QUANTITY > v_total_stock THEN
        RAISE_APPLICATION_ERROR(-20041, 'Lỗi: Số lượng thêm vào giỏ vượt quá tổng tồn kho hiện có.');
    END IF;
END;
/

-- 16. Kích hoạt bảo hành điện tử khi đơn hàng giao thành công
CREATE OR REPLACE TRIGGER TRG_WARRANTY_ACTIVATION
AFTER UPDATE OF STATUS ON ORDERS FOR EACH ROW
WHEN (NEW.STATUS = 'DELIVERED')
BEGIN
    -- Chuyển trạng thái bảo hành sang 1 (Active) và ghi nhận ngày bán (SOLD_AT)
    -- Dựa trên liên kết ORDER_DETAIL_ID
    UPDATE WARRANTY
    SET STATUS = 1, SOLD_AT = SYSDATE
    WHERE ORDER_DETAIL_ID IN (SELECT ORDER_DETAIL_ID FROM ORDER_DETAIL WHERE ORDER_ID = :NEW.ORDER_ID);
END;
/

-- 17. Chặn tham gia nhóm mua chung đã chốt hoặc hết hạn
CREATE OR REPLACE TRIGGER TRG_VALIDATE_GROUP_JOIN
BEFORE INSERT ON GROUP_MEMBER FOR EACH ROW
DECLARE
    v_status VARCHAR2(20);
    v_expiry DATE;
BEGIN
    SELECT STATUS, EXPIRY_TIME INTO v_status, v_expiry
    FROM GROUP_BUY
    WHERE GROUP_ID = :NEW.GROUP_ID;

    IF v_status != 'WAITING' THEN
        RAISE_APPLICATION_ERROR(-20042, 'Lỗi: Nhóm mua chung này đã chốt hoặc đã bị hủy.');
    END IF;

    IF v_expiry < SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20043, 'Lỗi: Thời gian gom đơn của nhóm này đã kết thúc.');
    END IF;
END;
/

-- 18. Tự động khóa tài khoản khi bị xóa mềm (Soft Delete)
CREATE OR REPLACE TRIGGER TRG_LOCK_DELETED_ACCOUNT
BEFORE UPDATE OF IS_DELETED ON ACCOUNT FOR EACH ROW
WHEN (NEW.IS_DELETED = 1)
BEGIN
    -- Khi Admin đánh dấu xóa tài khoản, tự động đổi trạng thái sang INACTIVE để chặn đăng nhập
    :NEW.STATUS := 'INACTIVE';
END;
/

-- 19. Kiểm tra tính hợp lệ của mã giảm giá khi áp dụng vào đơn hàng
CREATE OR REPLACE TRIGGER TRG_VALIDATE_ORDER_COUPON
BEFORE INSERT OR UPDATE ON ORDERS FOR EACH ROW
DECLARE
    v_active NUMBER;
    v_end_at DATE;
BEGIN
    IF :NEW.COUPON_ID IS NOT NULL THEN
        SELECT IS_ACTIVE, END_AT INTO v_active, v_end_at
        FROM COUPON
        WHERE COUPON_ID = :NEW.COUPON_ID AND IS_DELETED = 0;

        IF v_active = 0 THEN
            RAISE_APPLICATION_ERROR(-20044, 'Lỗi: Mã giảm giá này đã bị vô hiệu hóa.');
        ELSIF v_end_at < SYSDATE THEN
            RAISE_APPLICATION_ERROR(-20045, 'Lỗi: Mã giảm giá này đã hết hạn sử dụng.');
        END IF;
    END IF;
END;
/

-- 20. Đưa ra thông báo thân thiện khi nhân viên kho xuất âm hàng
CREATE OR REPLACE TRIGGER TRG_PREVENT_NEGATIVE_INVENTORY
BEFORE UPDATE ON INVENTORY FOR EACH ROW
BEGIN
    -- Mặc dù Schema có CHECK (QUANTITY >= 0), dùng Trigger sẽ trả về Error Message có ý nghĩa hơn cho Frontend
    IF :NEW.QUANTITY < 0 THEN
        RAISE_APPLICATION_ERROR(-20046, 'Lỗi nghiệp vụ: Số lượng xuất kho vượt quá số lượng tồn thực tế trong kho này.');
    END IF;
END;
/