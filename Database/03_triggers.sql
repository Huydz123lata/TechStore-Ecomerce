CREATE OR REPLACE TRIGGER TRG_PREPARE_ORDER_DETAIL
BEFORE INSERT OR UPDATE ON ORDER_DETAIL
FOR EACH ROW
DECLARE
    v_price NUMBER(18,2);
BEGIN
    -- Lấy giá hiện tại của sản phẩm để chốt giá mua
    SELECT PRICE INTO v_price
    FROM PRODUCT
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID;

    :NEW.PRICE := v_price;
    :NEW.LINE_TOTAL := :NEW.QUANTITY * v_price;
END;
/

CREATE OR REPLACE TRIGGER TRG_RECALCULATE_ORDER_TOTAL
AFTER INSERT OR UPDATE OR DELETE ON ORDER_DETAIL
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        UPDATE ORDERS
        SET SUBTOTAL = SUBTOTAL + :NEW.LINE_TOTAL,
            TOTAL = TOTAL + :NEW.LINE_TOTAL
        WHERE ORDER_ID = :NEW.ORDER_ID;
    ELSIF UPDATING THEN
        UPDATE ORDERS
        SET SUBTOTAL = SUBTOTAL - :OLD.LINE_TOTAL + :NEW.LINE_TOTAL,
            TOTAL = TOTAL - :OLD.LINE_TOTAL + :NEW.LINE_TOTAL
        WHERE ORDER_ID = :NEW.ORDER_ID;
    ELSIF DELETING THEN
        UPDATE ORDERS
        SET SUBTOTAL = SUBTOTAL - :OLD.LINE_TOTAL,
            TOTAL = TOTAL - :OLD.LINE_TOTAL
        WHERE ORDER_ID = :OLD.ORDER_ID;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_PAYMENT_SUCCESS_UPDATE_ORDER
AFTER UPDATE OF PAYMENT_STATUS ON PAYMENT
FOR EACH ROW
BEGIN
    IF :NEW.PAYMENT_STATUS = 'PAID' AND :OLD.PAYMENT_STATUS <> 'PAID' THEN
        UPDATE ORDERS
        SET STATUS = 'CONFIRMED',
            UPDATED_AT = SYSDATE
        WHERE ORDER_ID = :NEW.ORDER_ID;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_PREVENT_PAYMENT_BEFORE_ORDER_DETAIL
BEFORE INSERT ON PAYMENT
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM ORDER_DETAIL
    WHERE ORDER_ID = :NEW.ORDER_ID;

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Lỗi hệ thống: Không thể thanh toán cho đơn hàng trống (chưa có sản phẩm)!');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_CART_QUANTITY_LIMIT
BEFORE INSERT OR UPDATE ON CART_ITEM
FOR EACH ROW
DECLARE
    v_current_stock NUMBER;
BEGIN
    SELECT STOCK_QUANTITY INTO v_current_stock
    FROM PRODUCT
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID;

    IF :NEW.QUANTITY > NVL(v_current_stock, 0) THEN
        RAISE_APPLICATION_ERROR(-20007, 'Sản phẩm này hiện tại trong kho không đủ số lượng bạn yêu cầu!');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_PREVENT_DUP_PROMO_PRODUCT
BEFORE INSERT ON PROMOTION_PRODUCT
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM PROMOTION_PRODUCT
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Thao tác thất bại: Sản phẩm này đã được áp dụng ở một chương trình khuyến mãi khác!');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_CHECK_ORDER_BEFORE_BIRTH
BEFORE INSERT OR UPDATE ON ORDERS
FOR EACH ROW
DECLARE
    v_birth_date DATE;
BEGIN
    -- 1. Lấy ngày sinh (BIRTH) của User từ bảng APP_USER
    SELECT BIRTH INTO v_birth_date
    FROM APP_USER
    WHERE USER_ID = :NEW.USER_ID;

    -- 2. Kiểm tra điều kiện: Ngày đặt hàng phải BÉ HƠN ngày sinh
    -- Trigger BEFORE dùng để CHẶN, nên ta sẽ bắt trường hợp VI PHẠM:
    -- Nếu Ngày đặt hàng (CREATED_AT) LỚN HƠN HOẶC BẰNG Ngày sinh (BIRTH) thì chặn lại.
    IF TRUNC(NVL(:NEW.CREATED_AT, SYSDATE)) >= TRUNC(v_birth_date) THEN
        RAISE_APPLICATION_ERROR(-20015, 'Lỗi nghiệp vụ: Ngày đặt hàng không thể lớn hơn hoặc bằng ngày sinh của khách hàng!');
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Phòng trường hợp không tìm thấy User hoặc User đó để trống cột BIRTH
        NULL;
END;
/








--ĐEM VÀO BÁO CÁO CHÍNH
/* =============================================================================
   TRIGGER 1: TRG_PREVENT_PRICE_CHANGE_ON_ACTIVE_ORDER

   Nghiệp vụ: Ngăn nhân viên sửa giá sản phẩm khi sản phẩm đó đang nằm trong
   đơn hàng ở các trạng thái chưa hoàn tất (PENDING, CONFIRMED, PROCESSING,
   SHIPPING). Nếu cố tình sửa giá, trigger kiểm tra xem có đơn hàng nào đang
   "sống" chứa sản phẩm này không — nếu có thì chặn lại.

   Lý do khó:
   - Phải JOIN nhiều bảng (PRODUCT → ORDER_DETAIL → ORDERS) bên trong trigger.
   - Phải dùng :OLD.PRICE để so sánh, chỉ kích hoạt khi giá thực sự thay đổi
     (tránh chặn nhầm các UPDATE khác như sửa tên, mô tả...).
   - Giải quyết bài toán "tính nhất quán dữ liệu trong giao dịch đang chờ".
   ============================================================================= */
CREATE OR REPLACE TRIGGER TRG_PREVENT_PRICE_CHANGE_ON_ACTIVE_ORDER
BEFORE UPDATE OF PRICE ON PRODUCT
FOR EACH ROW
DECLARE
    v_active_order_count NUMBER;
BEGIN
    -- Chỉ kiểm tra khi giá thực sự thay đổi
    IF :NEW.PRICE <> :OLD.PRICE THEN

        -- Đếm số đơn hàng đang hoạt động có chứa sản phẩm này
        SELECT COUNT(*)
        INTO   v_active_order_count
        FROM   ORDER_DETAIL od
        JOIN   ORDERS o ON od.ORDER_ID = o.ORDER_ID
        WHERE  od.PRODUCT_ID = :NEW.PRODUCT_ID
          AND  o.STATUS IN (
                   'PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING'
               )
          AND  o.IS_DELETED = 0;

        IF v_active_order_count > 0 THEN
            RAISE_APPLICATION_ERROR(
                -20020,
                'Không thể thay đổi giá sản phẩm "' || :OLD.NAME || '"! ' ||
                'Hiện có ' || v_active_order_count || ' đơn hàng đang xử lý ' ||
                'chứa sản phẩm này. Vui lòng chờ đơn hàng hoàn tất hoặc hủy ' ||
                'trước khi điều chỉnh giá.'
            );
        END IF;

    END IF;
END;
/


/* =============================================================================
   TRIGGER 2: TRG_SMART_LOYALTY_POINT_WITH_TIER

   Nghiệp vụ: Hệ thống tích điểm thưởng thông minh theo hạng thành viên.
   Khi đơn hàng chuyển sang DELIVERED, tự động:
     1. Tính điểm cơ bản (1 điểm / 100.000đ).
     2. Áp hệ số nhân theo hạng thành viên dựa vào TỔNG ĐIỂM HIỆN CÓ:
          ĐỒNG  (< 500 pt)   → nhân x1.0
          BẠC   (500–1999pt) → nhân x1.5
          VÀNG  (2000–4999)  → nhân x2.0
          KIM CƯƠNG (≥ 5000) → nhân x3.0
     3. Cộng điểm và ghi log hạng vào DBMS_OUTPUT (hoặc bảng audit nếu có).

   Lý do khó:
   - Logic đa tầng (tier-based): đọc điểm hiện tại TRƯỚC KHI cộng để xác định
     hệ số, sau đó mới cộng điểm — tránh bị tính nhầm hệ số khi điểm vừa vượt
     ngưỡng.
   - Xử lý trường hợp khách hàng chưa có bản ghi LOYALTY_POINT (dùng MERGE).
   - Hiệu ứng "thăng hạng": in thông báo nếu điểm sau khi cộng vượt ngưỡng
     hạng mới.
   ============================================================================= */
CREATE OR REPLACE TRIGGER TRG_SMART_LOYALTY_POINT_WITH_TIER
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
DECLARE
    v_current_points  NUMBER(10) := 0;
    v_base_points     NUMBER(10);
    v_multiplier      NUMBER(5,2);
    v_final_points    NUMBER(10);
    v_new_total       NUMBER(10);
    v_old_tier        VARCHAR2(20);
    v_new_tier        VARCHAR2(20);

    FUNCTION get_tier(p_points IN NUMBER) RETURN VARCHAR2 IS
    BEGIN
        IF    p_points >= 5000 THEN RETURN 'KIM CƯƠNG';
        ELSIF p_points >= 2000 THEN RETURN 'VÀNG';
        ELSIF p_points >= 500  THEN RETURN 'BẠC';
        ELSE                        RETURN 'ĐỒNG';
        END IF;
    END get_tier;

BEGIN
    IF :NEW.STATUS = 'DELIVERED' AND :OLD.STATUS <> 'DELIVERED' THEN

        -- 1. Lấy điểm hiện tại
        BEGIN
            SELECT TOTAL_POINTS
            INTO   v_current_points
            FROM   LOYALTY_POINT
            WHERE  USER_ID = :NEW.USER_ID;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_current_points := 0;
        END;

        -- 2. Tính điểm cơ bản
        v_base_points := FLOOR(:NEW.TOTAL / 100000);

        -- 3. Xác định hệ số nhân theo hạng TRƯỚC KHI cộng
        v_old_tier := get_tier(v_current_points);

        v_multiplier := CASE v_old_tier
                            WHEN 'KIM CƯƠNG' THEN 3.0
                            WHEN 'VÀNG'      THEN 2.0
                            WHEN 'BẠC'       THEN 1.5
                            ELSE                  1.0
                        END;

        -- 4. Tính điểm thực tế
        v_final_points := FLOOR(v_base_points * v_multiplier);
        v_new_total    := v_current_points + v_final_points;
        v_new_tier     := get_tier(v_new_total);

        -- 5. Cập nhật hoặc tạo mới bản ghi điểm
        UPDATE LOYALTY_POINT
        SET    TOTAL_POINTS = v_new_total,
               UPDATED_AT   = SYSDATE
        WHERE  USER_ID = :NEW.USER_ID;

        IF SQL%ROWCOUNT = 0 THEN
            INSERT INTO LOYALTY_POINT (USER_ID, TOTAL_POINTS, UPDATED_AT)
            VALUES (:NEW.USER_ID, v_final_points, SYSDATE);
        END IF;

        -- 6. Log kết quả
        DBMS_OUTPUT.PUT_LINE(
            '[LOYALTY] User ' || :NEW.USER_ID ||
            ' | Hạng: ' || v_old_tier ||
            ' | Điểm cơ bản: ' || v_base_points ||
            ' x' || v_multiplier ||
            ' = +' || v_final_points || ' điểm' ||
            ' | Tổng: ' || v_new_total
        );

        -- 7. Thông báo thăng hạng nếu có
        IF v_new_tier <> v_old_tier THEN
            DBMS_OUTPUT.PUT_LINE(
                '[LOYALTY] CHUC MUNG! User ' || :NEW.USER_ID ||
                ' da thang hang tu ' || v_old_tier ||
                ' len ' || v_new_tier || '!'
            );
        END IF;

    END IF;
END;
/


/* =============================================================================
   TRIGGER 3: TRG_BLOCK_SUSPICIOUS_ORDER_SPIKE

   Nghiệp vụ: Phát hiện và chặn hành vi đặt hàng bất thường (chống gian lận /
   fraud detection). Trigger kiểm tra khi có đơn hàng MỚI được tạo:
     - Nếu trong vòng 10 phút qua, user đã đặt >= 5 đơn hàng → chặn (có thể
       là bot hoặc lạm dụng coupon).
     - Nếu tổng tiền đơn hàng mới > 5 lần giá trị trung bình 30 ngày của user
       đó → chặn (giao dịch bất thường, cần xác minh thủ công).

   Lý do khó:
   - Dùng WINDOW thời gian (SYSDATE - INTERVAL '10' MINUTE) để giới hạn phạm vi.
   - Tính AVG động từ lịch sử đơn hàng của chính user trong 30 ngày.
   - Hai điều kiện độc lập, mỗi điều kiện có thông báo lỗi riêng — giúp admin
     biết chính xác lý do bị chặn.
   - Xử lý edge case: nếu user chưa có lịch sử đơn → bỏ qua kiểm tra giá trị
     trung bình (tránh chặn nhầm khách mới).
   ============================================================================= */
CREATE OR REPLACE TRIGGER TRG_BLOCK_SUSPICIOUS_ORDER_SPIKE
BEFORE INSERT ON ORDERS
FOR EACH ROW
DECLARE
    v_order_count_10min  NUMBER;
    v_avg_order_30days   NUMBER(18,2);
    v_history_count      NUMBER;
    v_threshold_amount   NUMBER(18,2);
BEGIN
    -- ── KIỂM TRA 1: Tần suất đặt hàng trong 10 phút ──────────────────────────
    SELECT COUNT(*)
    INTO   v_order_count_10min
    FROM   ORDERS
    WHERE  USER_ID    = :NEW.USER_ID
      AND  IS_DELETED = 0
      AND  CREATED_AT >= SYSDATE - INTERVAL '10' MINUTE;

    IF v_order_count_10min >= 5 THEN
        RAISE_APPLICATION_ERROR(
            -20030,
            'Phát hiện hành vi đặt hàng bất thường: Tài khoản đã tạo ' ||
            v_order_count_10min || ' đơn trong 10 phút qua. ' ||
            'Tài khoản tạm thời bị hạn chế. Vui lòng liên hệ hỗ trợ.'
        );
    END IF;

    -- ── KIỂM TRA 2: Giá trị đơn hàng bất thường so với lịch sử ──────────────
    SELECT COUNT(*), NVL(AVG(TOTAL), 0)
    INTO   v_history_count, v_avg_order_30days
    FROM   ORDERS
    WHERE  USER_ID    = :NEW.USER_ID
      AND  STATUS    <> 'CANCELLED'
      AND  IS_DELETED = 0
      AND  CREATED_AT >= SYSDATE - 30;   -- 30 ngày gần nhất

    -- Chỉ kiểm tra khi đã có ít nhất 3 đơn hàng lịch sử (tránh chặn khách mới)
    IF v_history_count >= 3 THEN
        v_threshold_amount := v_avg_order_30days * 5;

        IF :NEW.TOTAL > v_threshold_amount THEN
            RAISE_APPLICATION_ERROR(
                -20031,
                'Phát hiện giao dịch bất thường: Giá trị đơn hàng (' ||
                TO_CHAR(:NEW.TOTAL, '999,999,999,999') || ' đ) vượt quá ' ||
                '5 lần giá trị trung bình 30 ngày của tài khoản (' ||
                TO_CHAR(v_avg_order_30days, '999,999,999,999') || ' đ). ' ||
                'Đơn hàng bị tạm giữ để xác minh.'
            );
        END IF;
    END IF;
END;
/
