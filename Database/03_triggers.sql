/* =============================================================================
   20 TRIGGER NGHIỆP VỤ TRỌNG TÂM
   Hệ thống: E-Commerce (Oracle Database)
   Cập nhật: TRG10 -> Huỷ CART sau đặt hàng thành công
             TRG_ORDER_STATUS_TRANSITION -> Bonus trigger state machine
=============================================================================
   NHÓM 1 : Đơn hàng & Thanh toán  (TRG1  – TRG4)
   NHÓM 2 : Tồn kho & Sản phẩm     (TRG5  – TRG8)
   NHÓM 3 : Điểm thưởng & Đánh giá (TRG9  – TRG12)
   NHÓM 4 : Mua chung – Group Buy   (TRG13 – TRG16)
   NHÓM 5 : Bảo mật & Tài khoản    (TRG17 – TRG20)
   BONUS  : State machine guard      (TRG_ORDER_STATUS_TRANSITION)
============================================================================= */


/* =========================================================================
   NHÓM 1 — ĐƠN HÀNG & THANH TOÁN
   ========================================================================= */

-- TRG1: Tự động tính lại TOTAL khi thêm / sửa / xoá ORDER_DETAIL
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

-- TRG3: Áp mã giảm giá — trừ DISCOUNT_VALUE vào TOTAL khi gán coupon
CREATE OR REPLACE TRIGGER TRG_APPLY_COUPON
AFTER UPDATE OF COUPON_ID ON ORDERS
FOR EACH ROW
DECLARE
    v_discount NUMBER(18,2);
    v_now      DATE := SYSDATE;
BEGIN
    IF :NEW.COUPON_ID IS NOT NULL AND
       (:OLD.COUPON_ID IS NULL OR :OLD.COUPON_ID != :NEW.COUPON_ID) THEN

        SELECT DISCOUNT_VALUE INTO v_discount
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
        RAISE_APPLICATION_ERROR(-20001,
            'Mã giảm giá không hợp lệ hoặc đã hết hạn.');
END;
/

-- TRG4: Ghi nhận thời điểm thanh toán khi PAYMENT_STATUS chuyển SUCCESS
CREATE OR REPLACE TRIGGER TRG_SET_PAID_AT
BEFORE UPDATE OF PAYMENT_STATUS ON PAYMENT
FOR EACH ROW
BEGIN
    IF :NEW.PAYMENT_STATUS = 'SUCCESS' AND :OLD.PAYMENT_STATUS != 'SUCCESS' THEN
        :NEW.PAID_AT := SYSDATE;
    END IF;
END;
/


/* =========================================================================
   NHÓM 2 — TỒN KHO & SẢN PHẨM
   ========================================================================= */

-- TRG5: Trừ tồn kho khi đơn hàng chuyển sang CONFIRMED
CREATE OR REPLACE TRIGGER TRG_DEDUCT_INVENTORY
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
BEGIN
    IF :NEW.STATUS = 'CONFIRMED' AND :OLD.STATUS = 'PENDING' THEN
        FOR r IN (SELECT PRODUCT_ID, WAREHOUSE_ID, QUANTITY
                  FROM ORDER_DETAIL
                  WHERE ORDER_ID = :NEW.ORDER_ID) LOOP

            UPDATE INVENTORY
            SET QUANTITY   = QUANTITY - r.QUANTITY,
                UPDATED_AT = SYSDATE
            WHERE PRODUCT_ID  = r.PRODUCT_ID
              AND WAREHOUSE_ID = r.WAREHOUSE_ID;

            IF SQL%ROWCOUNT = 0 THEN
                RAISE_APPLICATION_ERROR(-20010,
                    'Không tìm thấy tồn kho cho sản phẩm ID: ' || r.PRODUCT_ID);
            END IF;
        END LOOP;
    END IF;
END;
/

-- TRG6: Hoàn trả tồn kho khi đơn hàng bị huỷ
CREATE OR REPLACE TRIGGER TRG_RESTORE_INVENTORY_ON_CANCEL
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
BEGIN
    IF :NEW.STATUS = 'CANCELLED' AND :OLD.STATUS NOT IN ('CANCELLED','PENDING') THEN
        FOR r IN (SELECT PRODUCT_ID, WAREHOUSE_ID, QUANTITY
                  FROM ORDER_DETAIL
                  WHERE ORDER_ID = :NEW.ORDER_ID) LOOP
            UPDATE INVENTORY
            SET QUANTITY   = QUANTITY + r.QUANTITY,
                UPDATED_AT = SYSDATE
            WHERE PRODUCT_ID  = r.PRODUCT_ID
              AND WAREHOUSE_ID = r.WAREHOUSE_ID;
        END LOOP;
    END IF;
END;
/

-- TRG7: Ghi cảnh báo khi tồn kho xuống dưới ngưỡng 10
-- Yêu cầu bảng: CREATE TABLE INVENTORY_ALERT (
--   PRODUCT_ID NUMBER, WAREHOUSE_ID NUMBER, QUANTITY NUMBER, ALERT_AT DATE);
CREATE OR REPLACE TRIGGER TRG_LOW_STOCK_ALERT
AFTER UPDATE OF QUANTITY ON INVENTORY
FOR EACH ROW
BEGIN
    IF :NEW.QUANTITY < 10 AND (:OLD.QUANTITY >= 10 OR :OLD.QUANTITY IS NULL) THEN
        INSERT INTO INVENTORY_ALERT(PRODUCT_ID, WAREHOUSE_ID, QUANTITY, ALERT_AT)
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
    SELECT NVL(SUM(QUANTITY), 0) INTO v_available
    FROM INVENTORY
    WHERE PRODUCT_ID   = :NEW.PRODUCT_ID
      AND (WAREHOUSE_ID = :NEW.WAREHOUSE_ID OR :NEW.WAREHOUSE_ID IS NULL);

    IF v_available < :NEW.QUANTITY THEN
        RAISE_APPLICATION_ERROR(-20020,
            'Sản phẩm ID ' || :NEW.PRODUCT_ID ||
            ' không đủ tồn kho (còn ' || v_available || ').');
    END IF;
END;
/


/* =========================================================================
   NHÓM 3 — ĐIỂM THƯỞNG & ĐÁNH GIÁ
   ========================================================================= */

-- TRG9: Tích điểm khi đơn hàng chuyển sang DELIVERED
--       Quy tắc: 1 điểm / 10,000 VNĐ
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

-- TRG10: Tự động huỷ CART khi đơn hàng chuyển sang CONFIRMED
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

-- TRG11: Chặn đánh giá trùng (1 user – 1 sản phẩm – 1 review)
CREATE OR REPLACE TRIGGER TRG_PREVENT_DUPLICATE_REVIEW
BEFORE INSERT ON REVIEW
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM REVIEW
    WHERE USER_ID    = :NEW.USER_ID
      AND PRODUCT_ID = :NEW.PRODUCT_ID
      AND IS_DELETED = 0;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20030,
            'Người dùng đã đánh giá sản phẩm này rồi.');
    END IF;
END;
/

-- TRG12: Chỉ cho phép đánh giá nếu đã mua và nhận sản phẩm (DELIVERED)
CREATE OR REPLACE TRIGGER TRG_REVIEW_REQUIRES_PURCHASE
BEFORE INSERT ON REVIEW
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM ORDERS o
    JOIN ORDER_DETAIL od ON od.ORDER_ID = o.ORDER_ID
    WHERE o.USER_ID    = :NEW.USER_ID
      AND od.PRODUCT_ID = :NEW.PRODUCT_ID
      AND o.STATUS      = 'DELIVERED'
      AND o.IS_DELETED  = 0;

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20031,
            'Bạn phải mua và nhận sản phẩm trước khi đánh giá.');
    END IF;
END;
/


/* =========================================================================
   NHÓM 4 — MUA CHUNG (GROUP BUY)
   ========================================================================= */

-- TRG13: Cập nhật CURRENT_MEMBER khi có người tham gia nhóm
CREATE OR REPLACE TRIGGER TRG_UPDATE_GROUP_MEMBER_COUNT
AFTER INSERT ON GROUP_BUY_PARTICIPANT
FOR EACH ROW
BEGIN
    UPDATE GROUP_BUY
    SET CURRENT_MEMBER = CURRENT_MEMBER + 1
    WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;
END;
/

-- TRG14: Kích hoạt SUCCESS khi CURRENT_MEMBER đạt TARGET_MEMBER
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

-- TRG15: Tự động đánh dấu FAILED khi nhóm đã hết hạn lúc insert/update
CREATE OR REPLACE TRIGGER TRG_GROUPBUY_EXPIRE_CHECK
BEFORE INSERT OR UPDATE ON GROUP_BUY
FOR EACH ROW
BEGIN
    IF SYSDATE > :NEW.EXPIRES_AT AND :NEW.STATUS = 'PROCESSING' THEN
        :NEW.STATUS := 'FAILED';
    END IF;
END;
/

-- TRG16: Chặn tham gia nhóm mua đã đóng hoặc hết hạn
CREATE OR REPLACE TRIGGER TRG_BLOCK_JOIN_CLOSED_GROUP
BEFORE INSERT ON GROUP_BUY_PARTICIPANT
FOR EACH ROW
DECLARE
    v_status  VARCHAR2(20);
    v_expires DATE;
BEGIN
    SELECT STATUS, EXPIRES_AT INTO v_status, v_expires
    FROM GROUP_BUY
    WHERE GROUP_BUY_ID = :NEW.GROUP_BUY_ID;

    IF v_status != 'PROCESSING' THEN
        RAISE_APPLICATION_ERROR(-20040,
            'Nhóm mua chung này đã ' || v_status || ', không thể tham gia.');
    END IF;

    IF SYSDATE > v_expires THEN
        RAISE_APPLICATION_ERROR(-20041,
            'Nhóm mua chung đã hết hạn.');
    END IF;
END;
/


/* =========================================================================
   NHÓM 5 — BẢO MẬT & TÀI KHOẢN
   ========================================================================= */

-- TRG17: Thu hồi toàn bộ token khi tài khoản đổi mật khẩu
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

-- TRG18: Tự động thu hồi token hết hạn khi đăng nhập mới (tạo token mới)
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

-- TRG19: Tự động cập nhật UPDATED_AT cho USER và ACCOUNT
CREATE OR REPLACE TRIGGER TRG_USER_UPDATED_AT
BEFORE UPDATE ON "USER"
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSDATE;
END;
/

CREATE OR REPLACE TRIGGER TRG_ACCOUNT_UPDATED_AT
BEFORE UPDATE ON ACCOUNT
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSDATE;
END;
/

-- TRG20: Soft delete cascade — vô hiệu hoá ACCOUNT, token, CART khi xoá USER
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
            SELECT ACCOUNT_ID FROM ACCOUNT WHERE USER_ID = :OLD.USER_ID
        );

        UPDATE CART
        SET STATUS = 'CANCELLED'
        WHERE USER_ID = :OLD.USER_ID AND STATUS = 'ACTIVE';

    END IF;
END;
/


/* =========================================================================
   BONUS — STATE MACHINE GUARD
   Chặn các transition trạng thái đơn hàng không hợp lệ
   DELIVERED và CANCELLED là trạng thái cuối (terminal)
   ========================================================================= */

CREATE OR REPLACE TRIGGER TRG_ORDER_STATUS_TRANSITION
BEFORE UPDATE OF STATUS ON ORDERS
FOR EACH ROW
DECLARE
    v_ok NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_ok
    FROM (
        SELECT 'PENDING'    AS s, 'CONFIRMED'   AS t FROM DUAL UNION ALL
        SELECT 'PENDING',         'CANCELLED'         FROM DUAL UNION ALL
        SELECT 'CONFIRMED',       'PROCESSING'        FROM DUAL UNION ALL
        SELECT 'CONFIRMED',       'CANCELLED'         FROM DUAL UNION ALL
        SELECT 'PROCESSING',      'SHIPPING'          FROM DUAL UNION ALL
        SELECT 'PROCESSING',      'CANCELLED'         FROM DUAL UNION ALL
        SELECT 'SHIPPING',        'DELIVERED'         FROM DUAL UNION ALL
        SELECT 'SHIPPING',        'CANCELLED'         FROM DUAL
    )
    WHERE s = :OLD.STATUS AND t = :NEW.STATUS;

    IF v_ok = 0 AND :OLD.STATUS != :NEW.STATUS THEN
        RAISE_APPLICATION_ERROR(-20050,
            'Không thể chuyển trạng thái từ ' || :OLD.STATUS ||
            ' sang ' || :NEW.STATUS);
    END IF;
END;
/


/* =========================================================================
   BONUS — SCHEDULED JOB dọn token hết hạn (chạy 2:00 AM hàng ngày)
   ========================================================================= */

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_CLEANUP_EXPIRED_TOKENS',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'DELETE FROM ACCOUNT_TOKEN
                            WHERE EXPIRES_AT < SYSDATE
                               OR IS_REVOKED = ''Y'';',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY; BYHOUR=2; BYMINUTE=0',
        enabled         => TRUE
    );
END;
/


/* =========================================================================
   INDEX KHUYẾN NGHỊ (hiệu năng)
   ========================================================================= */

CREATE INDEX IDX_TOKEN_VALUE   ON ACCOUNT_TOKEN(TOKEN_VALUE);
CREATE INDEX IDX_TOKEN_ACCOUNT ON ACCOUNT_TOKEN(ACCOUNT_ID, IS_REVOKED, EXPIRES_AT);