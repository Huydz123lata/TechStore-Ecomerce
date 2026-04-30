-- =============================================================================
-- 20 TRIGGER NGHIỆP VỤ TRỌNG TÂM
-- Oracle Database -- chạy trên DataGrip
-- Delimiter: chọn "Statement delimiter" = / trong DataGrip settings
-- Hoặc vào: File > Settings > Database > General > Statement delimiter = /
-- =============================================================================


-- =============================================================================
-- NHÓM 1: ĐƠN HÀNG & THANH TOÁN (TRG1 - TRG4)
-- =============================================================================

-- TRG1: Tính lại TOTAL khi thêm / sửa / xoá ORDER_DETAIL
CREATE OR REPLACE TRIGGER TRG_RECALC_ORDER_TOTAL
AFTER INSERT OR UPDATE OR DELETE ON ORDER_DETAIL
FOR EACH ROW
DECLARE
    v_order_id NUMBER;
BEGIN
    v_order_id := CASE
                      WHEN DELETING THEN :OLD.ORDER_ID
                      ELSE :NEW.ORDER_ID
                  END;

    UPDATE ORDERS
    SET TOTAL      = (SELECT NVL(SUM(QUANTITY * PRICE), 0)
                      FROM ORDER_DETAIL
                      WHERE ORDER_ID = v_order_id),
        UPDATED_AT = SYSDATE
    WHERE ORDER_ID = v_order_id;
END;
/

-- TRG2: Tự động CONFIRMED đơn hàng khi thanh toán thành công
CREATE OR REPLACE TRIGGER TRG_CONFIRM_ORDER_ON_PAYMENT
AFTER INSERT OR UPDATE OF PAYMENT_STATUS ON PAYMENT
FOR EACH ROW
BEGIN
    IF :NEW.PAYMENT_STATUS = 'SUCCESS' THEN
        UPDATE ORDERS
        SET STATUS     = 'CONFIRMED',
            UPDATED_AT = SYSDATE
        WHERE ORDER_ID = :NEW.ORDER_ID
          AND STATUS   = 'PENDING';
    END IF;
END;
/

-- TRG3: Áp mã giảm giá vào TOTAL khi gán COUPON_ID cho đơn hàng
CREATE OR REPLACE TRIGGER TRG_APPLY_COUPON
AFTER UPDATE OF COUPON_ID ON ORDERS
FOR EACH ROW
DECLARE
    v_discount NUMBER(18, 2);
    v_now      DATE := SYSDATE;
BEGIN
    IF :NEW.COUPON_ID IS NOT NULL AND
       (:OLD.COUPON_ID IS NULL OR :OLD.COUPON_ID != :NEW.COUPON_ID) THEN

        SELECT DISCOUNT_VALUE
        INTO v_discount
        FROM COUPON
        WHERE COUPON_ID = :NEW.COUPON_ID
          AND IS_ACTIVE  = 1
          AND IS_DELETED = 0
          AND v_now BETWEEN START_AT AND END_AT;

        UPDATE ORDERS
        SET TOTAL      = GREATEST(TOTAL - v_discount, 0),
            UPDATED_AT = SYSDATE
        WHERE ORDER_ID = :NEW.ORDER_ID;

    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20001, 'Ma giam gia khong hop le hoac da het han.');
END;
/

-- TRG4: Ghi PAID_AT khi PAYMENT_STATUS chuyển thành SUCCESS
CREATE OR REPLACE TRIGGER TRG_SET_PAID_AT
BEFORE UPDATE OF PAYMENT_STATUS ON PAYMENT
FOR EACH ROW
BEGIN
    IF :NEW.PAYMENT_STATUS = 'SUCCESS'
        AND :OLD.PAYMENT_STATUS != 'SUCCESS' THEN
        :NEW.PAID_AT := SYSDATE;
    END IF;
END;
/


-- =============================================================================
-- NHÓM 2: TỒN KHO & SẢN PHẨM (TRG5 - TRG8)
-- =============================================================================

-- TRG5: Trừ tồn kho khi đơn hàng chuyển PENDING -> CONFIRMED
CREATE OR REPLACE TRIGGER TRG_DEDUCT_INVENTORY
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
DECLARE
    v_qty NUMBER;
BEGIN
    IF :NEW.STATUS = 'CONFIRMED' AND :OLD.STATUS = 'PENDING' THEN
        FOR r IN (SELECT PRODUCT_ID, WAREHOUSE_ID, QUANTITY
                  FROM ORDER_DETAIL
                  WHERE ORDER_ID = :NEW.ORDER_ID)
        LOOP
            UPDATE INVENTORY
            SET QUANTITY   = QUANTITY - r.QUANTITY,
                UPDATED_AT = SYSDATE
            WHERE PRODUCT_ID   = r.PRODUCT_ID
              AND WAREHOUSE_ID = r.WAREHOUSE_ID;

            IF SQL%ROWCOUNT = 0 THEN
                RAISE_APPLICATION_ERROR(-20010,
                    'Khong tim thay ton kho cho san pham ID: ' || r.PRODUCT_ID);
            END IF;
        END LOOP;
    END IF;
END;
/

-- TRG6: Hoàn tồn kho khi đơn hàng bị huỷ
CREATE OR REPLACE TRIGGER TRG_RESTORE_INVENTORY_ON_CANCEL
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
BEGIN
    IF :NEW.STATUS = 'CANCELLED'
        AND :OLD.STATUS NOT IN ('CANCELLED', 'PENDING') THEN
        FOR r IN (SELECT PRODUCT_ID, WAREHOUSE_ID, QUANTITY
                  FROM ORDER_DETAIL
                  WHERE ORDER_ID = :NEW.ORDER_ID)
        LOOP
            UPDATE INVENTORY
            SET QUANTITY   = QUANTITY + r.QUANTITY,
                UPDATED_AT = SYSDATE
            WHERE PRODUCT_ID   = r.PRODUCT_ID
              AND WAREHOUSE_ID = r.WAREHOUSE_ID;
        END LOOP;
    END IF;
END;
/

-- TRG7: Ghi cảnh báo khi tồn kho xuống dưới 10
-- Cần tạo trước: CREATE TABLE INVENTORY_ALERT (
--   PRODUCT_ID NUMBER, WAREHOUSE_ID NUMBER, QUANTITY NUMBER, ALERT_AT DATE);
CREATE OR REPLACE TRIGGER TRG_LOW_STOCK_ALERT
AFTER UPDATE OF QUANTITY ON INVENTORY
FOR EACH ROW
BEGIN
    IF :NEW.QUANTITY < 10
        AND (:OLD.QUANTITY >= 10 OR :OLD.QUANTITY IS NULL) THEN
        INSERT INTO INVENTORY_ALERT (PRODUCT_ID, WAREHOUSE_ID, QUANTITY, ALERT_AT)
        VALUES (:NEW.PRODUCT_ID, :NEW.WAREHOUSE_ID, :NEW.QUANTITY, SYSDATE);
    END IF;
END;
/

-- TRG8: Chặn thêm ORDER_DETAIL khi sản phẩm không đủ tồn kho
CREATE OR REPLACE TRIGGER TRG_BLOCK_OUT_OF_STOCK
BEFORE INSERT ON ORDER_DETAIL
FOR EACH ROW
DECLARE
    v_available NUMBER;
BEGIN
    SELECT NVL(SUM(QUANTITY), 0)
    INTO v_available
    FROM INVENTORY
    WHERE PRODUCT_ID   = :NEW.PRODUCT_ID
      AND (WAREHOUSE_ID = :NEW.WAREHOUSE_ID OR :NEW.WAREHOUSE_ID IS NULL);

    IF v_available < :NEW.QUANTITY THEN
        RAISE_APPLICATION_ERROR(-20020,
            'San pham ID ' || :NEW.PRODUCT_ID ||
            ' khong du ton kho (con ' || v_available || ').');
    END IF;
END;
/


-- =============================================================================
-- NHÓM 3: ĐIỂM THƯỞNG & ĐÁNH GIÁ (TRG9 - TRG12)
-- =============================================================================

-- TRG9: Tích điểm khi đơn hàng chuyển sang DELIVERED (1đ / 10,000 VND)
CREATE OR REPLACE TRIGGER TRG_EARN_LOYALTY_POINTS
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
DECLARE
    v_points NUMBER;
BEGIN
    IF :NEW.STATUS = 'DELIVERED' AND :OLD.STATUS != 'DELIVERED' THEN
        v_points := TRUNC(:NEW.TOTAL / 10000);

        MERGE INTO LOYALTY_POINT lp
        USING (SELECT :NEW.USER_ID AS uid FROM DUAL) src
        ON (lp.USER_ID = src.uid)
        WHEN MATCHED THEN
            UPDATE SET TOTAL_POINTS = TOTAL_POINTS + v_points,
                       UPDATED_AT   = SYSDATE
        WHEN NOT MATCHED THEN
            INSERT (USER_ID, TOTAL_POINTS, UPDATED_AT)
            VALUES (src.uid, v_points, SYSDATE);
    END IF;
END;
/

-- TRG10: Huỷ CART đang ACTIVE khi đơn hàng chuyển PENDING -> CONFIRMED
CREATE OR REPLACE TRIGGER TRG_CANCEL_CART_ON_ORDER_CONFIRMED
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
BEGIN
    IF :NEW.STATUS = 'CONFIRMED' AND :OLD.STATUS = 'PENDING' THEN
        UPDATE CART
        SET STATUS = 'CANCELLED'
        WHERE USER_ID = :NEW.USER_ID
          AND STATUS  = 'ACTIVE';
    END IF;
END;
/

-- TRG11: Chặn đánh giá trùng (1 user - 1 sản phẩm - 1 review)
CREATE OR REPLACE TRIGGER TRG_PREVENT_DUPLICATE_REVIEW
BEFORE INSERT ON REVIEW
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM REVIEW
    WHERE USER_ID    = :NEW.USER_ID
      AND PRODUCT_ID = :NEW.PRODUCT_ID
      AND IS_DELETED = 0;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20030,
            'Nguoi dung da danh gia san pham nay roi.');
    END IF;
END;
/

-- TRG12: Chỉ cho phép đánh giá nếu đã mua và nhận hàng (DELIVERED)
CREATE OR REPLACE TRIGGER TRG_REVIEW_REQUIRES_PURCHASE
BEFORE INSERT ON REVIEW
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM ORDERS o
    JOIN ORDER_DETAIL od ON od.ORDER_ID = o.ORDER_ID
    WHERE o.USER_ID    = :NEW.USER_ID
      AND od.PRODUCT_ID = :NEW.PRODUCT_ID
      AND o.STATUS      = 'DELIVERED'
      AND o.IS_DELETED  = 0;

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20031,
            'Ban phai mua va nhan san pham truoc khi danh gia.');
    END IF;
END;
/


-- =============================================================================
-- NHÓM 4: MUA CHUNG - GROUP BUY (TRG13 - TRG16)
-- =============================================================================

-- TRG13: Tăng CURRENT_MEMBER khi có người tham gia nhóm
CREATE OR REPLACE TRIGGER TRG_UPDATE_GROUP_MEMBER_COUNT
AFTER INSERT ON GROUP_BUY_PARTICIPANT
FOR EACH ROW
BEGIN
    UPDATE GROUP_BUY
    SET CURRENT_MEMBER = CURRENT_MEMBER + 1
    WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;
END;
/

-- TRG14: Kích hoạt SUCCESS khi CURRENT_MEMBER >= TARGET_MEMBER
CREATE OR REPLACE TRIGGER TRG_GROUPBUY_SUCCESS_ON_FULL
AFTER UPDATE OF CURRENT_MEMBER ON GROUP_BUY
FOR EACH ROW
BEGIN
    IF :NEW.CURRENT_MEMBER >= :NEW.TARGET_MEMBER
        AND :OLD.STATUS = 'PROCESSING' THEN
        UPDATE GROUP_BUY
        SET STATUS = 'SUCCESS'
        WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;
    END IF;
END;
/

-- TRG15: Tự động đánh dấu FAILED nếu nhóm hết hạn khi insert/update
CREATE OR REPLACE TRIGGER TRG_GROUPBUY_EXPIRE_CHECK
BEFORE INSERT OR UPDATE ON GROUP_BUY
FOR EACH ROW
BEGIN
    IF SYSDATE > :NEW.EXPIRES_AT AND :NEW.STATUS = 'PROCESSING' THEN
        :NEW.STATUS := 'FAILED';
    END IF;
END;
/

-- TRG16: Chặn tham gia nhóm đã đóng hoặc hết hạn
CREATE OR REPLACE TRIGGER TRG_BLOCK_JOIN_CLOSED_GROUP
BEFORE INSERT ON GROUP_BUY_PARTICIPANT
FOR EACH ROW
DECLARE
    v_status  VARCHAR2(20);
    v_expires DATE;
BEGIN
    SELECT STATUS, EXPIRES_AT
    INTO v_status, v_expires
    FROM GROUP_BUY
    WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;

    IF v_status != 'PROCESSING' THEN
        RAISE_APPLICATION_ERROR(-20040,
            'Nhom mua chung nay da ' || v_status || ', khong the tham gia.');
    END IF;

    IF SYSDATE > v_expires THEN
        RAISE_APPLICATION_ERROR(-20041, 'Nhom mua chung da het han.');
    END IF;
END;
/


-- =============================================================================
-- NHÓM 5: BẢO MẬT & TÀI KHOẢN (TRG17 - TRG20)
-- =============================================================================

-- TRG17: Thu hồi toàn bộ token khi đổi mật khẩu
CREATE OR REPLACE TRIGGER TRG_REVOKE_TOKENS_ON_PWD_CHANGE
AFTER UPDATE OF PASSWORD_HASH ON ACCOUNT
FOR EACH ROW
BEGIN
    IF :NEW.PASSWORD_HASH != :OLD.PASSWORD_HASH THEN
        UPDATE ACCOUNT_TOKEN
        SET IS_REVOKED = 'Y'
        WHERE ACCOUNT_ID = :NEW.ACCOUNT_ID
          AND IS_REVOKED  = 'N';
    END IF;
END;
/

-- TRG18: Thu hồi token hết hạn khi tạo token đăng nhập mới
CREATE OR REPLACE TRIGGER TRG_REVOKE_EXPIRED_TOKENS
BEFORE INSERT ON ACCOUNT_TOKEN
FOR EACH ROW
BEGIN
    UPDATE ACCOUNT_TOKEN
    SET IS_REVOKED = 'Y'
    WHERE ACCOUNT_ID = :NEW.ACCOUNT_ID
      AND IS_REVOKED  = 'N'
      AND EXPIRES_AT  < SYSDATE;
END;
/

-- TRG19a: Tự động cập nhật UPDATED_AT cho bảng USER
CREATE OR REPLACE TRIGGER TRG_USER_UPDATED_AT
BEFORE UPDATE ON "USER"
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSDATE;
END;
/

-- TRG19b: Tự động cập nhật UPDATED_AT cho bảng ACCOUNT
CREATE OR REPLACE TRIGGER TRG_ACCOUNT_UPDATED_AT
BEFORE UPDATE ON ACCOUNT
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSDATE;
END;
/

-- TRG20: Soft delete cascade khi USER bị xoá mềm
--        Lan xuống: ACCOUNT -> ACCOUNT_TOKEN -> CART
CREATE OR REPLACE TRIGGER TRG_CASCADE_SOFT_DELETE_USER
AFTER UPDATE OF IS_DELETED ON "USER"
FOR EACH ROW
BEGIN
    IF :NEW.IS_DELETED = 1 AND :OLD.IS_DELETED = 0 THEN

        UPDATE ACCOUNT
        SET IS_DELETED = 1,
            STATUS     = 'INACTIVE',
            UPDATED_AT = SYSDATE
        WHERE USER_ID = :OLD.USER_ID;

        UPDATE ACCOUNT_TOKEN
        SET IS_REVOKED = 'Y'
        WHERE ACCOUNT_ID IN (
            SELECT ACCOUNT_ID
            FROM ACCOUNT
            WHERE USER_ID = :OLD.USER_ID
        );

        UPDATE CART
        SET STATUS = 'CANCELLED'
        WHERE USER_ID = :OLD.USER_ID
          AND STATUS  = 'ACTIVE';

    END IF;
END;
/





