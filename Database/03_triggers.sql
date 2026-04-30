-- =====================================================
-- 20 TRIGGERS NGHIỆP VỤ HOÀN CHỈNH (ĐÚNG THEO SCHEMA)
-- =====================================================

--------------------------------------------------------
-- 1. Tự động tính tổng tiền đơn hàng
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_ORDER_TOTAL_CALC
AFTER INSERT OR UPDATE OR DELETE ON ORDER_DETAIL
FOR EACH ROW
BEGIN
    IF INSERTING OR UPDATING THEN
        UPDATE ORDERS
        SET TOTAL = (
            SELECT NVL(SUM(QUANTITY * PRICE), 0)
            FROM ORDER_DETAIL
            WHERE ORDER_ID = :NEW.ORDER_ID
        )
        WHERE ORDER_ID = :NEW.ORDER_ID;
    END IF;

    IF DELETING THEN
        UPDATE ORDERS
        SET TOTAL = (
            SELECT NVL(SUM(QUANTITY * PRICE), 0)
            FROM ORDER_DETAIL
            WHERE ORDER_ID = :OLD.ORDER_ID
        )
        WHERE ORDER_ID = :OLD.ORDER_ID;
    END IF;
END;
/

--------------------------------------------------------
-- 2. Đồng bộ trạng thái thanh toán - đơn hàng
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PAYMENT_ORDER_SYNC
AFTER INSERT OR UPDATE ON PAYMENT
FOR EACH ROW
BEGIN
    IF :NEW.PAYMENT_STATUS = 'SUCCESS' THEN
        UPDATE ORDERS
        SET STATUS = 'PROCESSING'
        WHERE ORDER_ID = :NEW.ORDER_ID
          AND STATUS = 'PENDING';
    END IF;
END;
/

--------------------------------------------------------
-- 3. Kiểm tra số tiền thanh toán hợp lệ
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_VALIDATE_PAYMENT_AMOUNT
BEFORE INSERT OR UPDATE ON PAYMENT
FOR EACH ROW
DECLARE
    V_TOTAL NUMBER;
BEGIN
    SELECT TOTAL
    INTO V_TOTAL
    FROM ORDERS
    WHERE ORDER_ID = :NEW.ORDER_ID;

    IF :NEW.AMOUNT < V_TOTAL THEN
        RAISE_APPLICATION_ERROR(-20001,
            'So tien thanh toan khong du');
    END IF;
END;
/

--------------------------------------------------------
-- 4. Không cho hủy đơn đã giao vận
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PREVENT_CANCEL_SHIPPED_ORDER
BEFORE UPDATE ON ORDERS
FOR EACH ROW
BEGIN
    IF :OLD.STATUS = 'SHIPPING'
       AND :NEW.STATUS = 'CANCELLED' THEN
        RAISE_APPLICATION_ERROR(-20002,
            'Don dang giao khong the huy');
    END IF;
END;
/

--------------------------------------------------------
-- 5. Trừ tồn kho khi đặt hàng
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_DEDUCT_INVENTORY
AFTER INSERT ON ORDER_DETAIL
FOR EACH ROW
BEGIN
    UPDATE INVENTORY
    SET QUANTITY = QUANTITY - :NEW.QUANTITY
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID
      AND WAREHOUSE_ID = :NEW.WAREHOUSE_ID;
END;
/

--------------------------------------------------------
-- 6. Hoàn kho khi đơn bị hủy
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_RESTORE_INVENTORY
AFTER UPDATE ON ORDERS
FOR EACH ROW
BEGIN
    IF :NEW.STATUS = 'CANCELLED'
       AND :OLD.STATUS <> 'CANCELLED' THEN

        FOR X IN (
            SELECT PRODUCT_ID, WAREHOUSE_ID, QUANTITY
            FROM ORDER_DETAIL
            WHERE ORDER_ID = :NEW.ORDER_ID
        )
        LOOP
            UPDATE INVENTORY
            SET QUANTITY = QUANTITY + X.QUANTITY
            WHERE PRODUCT_ID = X.PRODUCT_ID
              AND WAREHOUSE_ID = X.WAREHOUSE_ID;
        END LOOP;
    END IF;
END;
/

--------------------------------------------------------
-- 7. Chỉ người đã mua mới được review
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_REVIEW_VALIDATE_PURCHASE
BEFORE INSERT ON REVIEW
FOR EACH ROW
DECLARE
    V_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO V_COUNT
    FROM ORDERS O
    JOIN ORDER_DETAIL OD ON O.ORDER_ID = OD.ORDER_ID
    WHERE O.USER_ID = :NEW.USER_ID
      AND OD.PRODUCT_ID = :NEW.PRODUCT_ID
      AND O.STATUS = 'DELIVERED';

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20003,
            'Chi nguoi da mua moi duoc danh gia');
    END IF;
END;
/

--------------------------------------------------------
-- 8. Tự động cộng điểm tích lũy
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_LOYALTY_POINTS_ADD
AFTER UPDATE ON ORDERS
FOR EACH ROW
BEGIN
    IF :NEW.STATUS = 'DELIVERED'
       AND :OLD.STATUS <> 'DELIVERED' THEN

        UPDATE LOYALTY_POINT
        SET TOTAL_POINTS = TOTAL_POINTS + (:NEW.TOTAL * 0.01)
        WHERE USER_ID = :NEW.USER_ID;
    END IF;
END;
/

--------------------------------------------------------
-- 9. Kiểm tra giới hạn giảm giá promotion
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_CHECK_DISCOUNT_LIMIT
BEFORE INSERT OR UPDATE ON PROMOTION_PRODUCT
FOR EACH ROW
BEGIN
    IF :NEW.DISCOUNT_VALUE > 100 THEN
        RAISE_APPLICATION_ERROR(-20004,
            'Giam gia khong duoc vuot 100%');
    END IF;
END;
/

--------------------------------------------------------
-- 10. Tăng số thành viên mua chung
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_GROUP_BUY_PARTICIPANT_INC
AFTER INSERT ON GROUP_BUY_PARTICIPANT
FOR EACH ROW
BEGIN
    UPDATE GROUP_BUY
    SET CURRENT_MEMBER = CURRENT_MEMBER + 1
    WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;
END;
/

--------------------------------------------------------
-- 11. Đủ người thì mua chung thành công
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_GROUP_BUY_SUCCESS_CHECK
AFTER UPDATE ON GROUP_BUY
FOR EACH ROW
BEGIN
    IF :NEW.CURRENT_MEMBER >= :NEW.TARGET_MEMBER THEN
        UPDATE GROUP_BUY
        SET STATUS = 'SUCCESS'
        WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;
    END IF;
END;
/

--------------------------------------------------------
-- 12. Không thêm sản phẩm vào promotion hết hạn
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PROMOTION_DATE_VALIDATE
BEFORE INSERT ON PROMOTION_PRODUCT
FOR EACH ROW
DECLARE
    V_END DATE;
BEGIN
    SELECT END_AT
    INTO V_END
    FROM PROMOTION
    WHERE PROMOTION_ID = :NEW.PROMOTION_ID;

    IF V_END < SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20005,
            'Promotion da het han');
    END IF;
END;
/

--------------------------------------------------------
-- 13. Không cho đặt sản phẩm ngừng kinh doanh
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PREVENT_INACTIVE_PRODUCT_ORDER
BEFORE INSERT ON ORDER_DETAIL
FOR EACH ROW
DECLARE
    V_STATUS NUMBER;
BEGIN
    SELECT STATUS
    INTO V_STATUS
    FROM PRODUCT
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID;

    IF V_STATUS = 0 THEN
        RAISE_APPLICATION_ERROR(-20006,
            'San pham da ngung kinh doanh');
    END IF;
END;
/

--------------------------------------------------------
-- 14. Kiểm tra số lượng giỏ hàng với tồn kho
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_CHECK_CART_ITEM_STOCK
BEFORE INSERT OR UPDATE ON CART_ITEM
FOR EACH ROW
DECLARE
    V_TOTAL_STOCK NUMBER;
BEGIN
    SELECT NVL(SUM(QUANTITY),0)
    INTO V_TOTAL_STOCK
    FROM INVENTORY
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID;

    IF :NEW.QUANTITY > V_TOTAL_STOCK THEN
        RAISE_APPLICATION_ERROR(-20007,
            'Vuot qua so luong ton kho');
    END IF;
END;
/

--------------------------------------------------------
-- 15. Kiểm tra điều kiện tham gia mua chung
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_VALIDATE_GROUP_JOIN
BEFORE INSERT ON GROUP_BUY_PARTICIPANT
FOR EACH ROW
DECLARE
    V_STATUS VARCHAR2(20);
    V_EXPIRE DATE;
BEGIN
    SELECT STATUS, EXPIRES_AT
    INTO V_STATUS, V_EXPIRE
    FROM GROUP_BUY
    WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;

    IF V_STATUS = 'SUCCESS' OR V_EXPIRE < SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20008,
            'Khong the tham gia group buy');
    END IF;
END;
/

--------------------------------------------------------
-- 16. Khóa account khi user bị soft delete
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_LOCK_DELETED_ACCOUNT
AFTER UPDATE ON "USER"
FOR EACH ROW
BEGIN
    IF :NEW.IS_DELETED = 1 THEN
        UPDATE ACCOUNT
        SET STATUS = 'INACTIVE'
        WHERE USER_ID = :NEW.USER_ID;
    END IF;
END;
/

--------------------------------------------------------
-- 17. Kiểm tra coupon hợp lệ khi tạo đơn
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_VALIDATE_ORDER_COUPON
BEFORE INSERT OR UPDATE ON ORDERS
FOR EACH ROW
DECLARE
    V_ACTIVE NUMBER;
    V_END DATE;
BEGIN
    IF :NEW.COUPON_ID IS NOT NULL THEN
        SELECT IS_ACTIVE, END_AT
        INTO V_ACTIVE, V_END
        FROM COUPON
        WHERE COUPON_ID = :NEW.COUPON_ID;

        IF V_ACTIVE = 0 OR V_END < SYSDATE THEN
            RAISE_APPLICATION_ERROR(-20009,
                'Coupon khong hop le');
        END IF;
    END IF;
END;
/

--------------------------------------------------------
-- 18. Chặn xuất âm kho
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PREVENT_NEGATIVE_INVENTORY
BEFORE UPDATE ON INVENTORY
FOR EACH ROW
BEGIN
    IF :NEW.QUANTITY < 0 THEN
        RAISE_APPLICATION_ERROR(-20010,
            'So luong xuat vuot qua ton kho');
    END IF;
END;
/

--------------------------------------------------------
-- 19. Mỗi user chỉ được review 1 lần / sản phẩm
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_ONE_REVIEW_PER_PRODUCT
BEFORE INSERT ON REVIEW
FOR EACH ROW
DECLARE
    V_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO V_COUNT
    FROM REVIEW
    WHERE USER_ID = :NEW.USER_ID
      AND PRODUCT_ID = :NEW.PRODUCT_ID;

    IF V_COUNT > 0 THEN
        RAISE_APPLICATION_ERROR(-20011,
            'Ban da danh gia san pham nay');
    END IF;
END;
/

--------------------------------------------------------
-- 20. Kiểm tra ngày coupon hợp lệ
--------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_COUPON_DATE_VALIDATE
BEFORE INSERT OR UPDATE ON COUPON
FOR EACH ROW
BEGIN
    IF :NEW.END_AT <= :NEW.START_AT THEN
        RAISE_APPLICATION_ERROR(-20012,
            'Ngay ket thuc phai lon hon ngay bat dau');
    END IF;
END;
/