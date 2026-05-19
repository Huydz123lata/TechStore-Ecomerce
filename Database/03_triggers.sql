--Trigger 1
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

--Trigger 2
CREATE OR REPLACE TRIGGER TRG_RECALCULATE_CART_TOTAL
AFTER INSERT OR UPDATE OR DELETE ON CART_ITEM
FOR EACH ROW
DECLARE
    v_cart_id        NUMBER;
    v_sum_cart_total NUMBER(18,2);
BEGIN
    -- 1. Xác định ID của giỏ hàng đang bị tác động
    -- Dùng NVL phòng trường hợp xóa sản phẩm khỏi giỏ (khi DELETE thì :NEW sẽ bị rỗng)
    v_cart_id := NVL(:NEW.CART_ID, :OLD.CART_ID);

    -- 2. Dùng hàm SUM gom toàn bộ tiền của các sản phẩm hiện có trong giỏ hàng đó
    SELECT SUM(TOTAL) INTO v_sum_cart_total
    FROM CART_ITEM
    WHERE CART_ID = v_cart_id;

    -- Nếu giỏ hàng trống (khách xóa sạch đồ), gán tổng tiền bằng 0
    v_sum_cart_total := NVL(v_sum_cart_total, 0);

    -- 3. Cập nhật con số tổng mới này vào bảng lớn CART kèm theo thời gian chỉnh sửa
    UPDATE CART
    SET TOTAL = v_sum_cart_total,
        UPDATED_AT = SYSDATE
    WHERE CART_ID = v_cart_id;
END;
/

--Trigger 3
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

--Trigger 4
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

--Trigger 5
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

--Trigger 6
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

--Trigger 7
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


--Trigger 8 (chọn)
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


--Trigger 9 (chọn)
CREATE OR REPLACE TRIGGER TRG_SMART_LOYALTY_POINT_WITH_TIER
AFTER UPDATE OF STATUS ON ORDERS
FOR EACH ROW
DECLARE
    v_current_points  NUMBER(10) := 0;
    v_base_points     NUMBER(10);
    v_multiplier      NUMBER(5,2);
    v_final_points    NUMBER(10);
    v_new_total       NUMBER(10);
    v_tier_info       VARCHAR2(200);
BEGIN
    -- Chỉ kích hoạt khi đơn hàng chính thức chuyển sang trạng thái DELIVERED
    IF :NEW.STATUS = 'DELIVERED' AND :OLD.STATUS <> 'DELIVERED' THEN

        -- 1. Lấy tổng điểm tích lũy hiện tại của khách hàng
        BEGIN
            SELECT TOTAL_POINTS
            INTO   v_current_points
            FROM   LOYALTY_POINT
            WHERE  USER_ID = :NEW.USER_ID;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_current_points := 0;
        END;

        -- 2. Tính điểm cơ bản (100,000 VND = 1 điểm cơ bản)
        v_base_points := FLOOR(:NEW.TOTAL / 100000);

        -- 3. Gọi Function của bạn để lấy chuỗi thông tin phân hạng hiện tại (TRƯỚC KHI CỘNG ĐIỂM)
        v_tier_info := fn_get_user_loyalty_tier(:NEW.USER_ID);

        -- 4. Xác định hệ số nhân dựa trên từ khóa hạng chứa trong chuỗi kết quả trả về
        v_multiplier := CASE
                            WHEN v_tier_info LIKE 'KIM CƯƠNG%' THEN 3.0
                            WHEN v_tier_info LIKE 'VÀNG%'      THEN 2.0
                            WHEN v_tier_info LIKE 'BẠC%'       THEN 1.5
                            ELSE                                    1.0
                        END;

        -- 5. Tính toán số điểm thực tế nhận được sau khi nhân hệ số và tổng điểm mới
        v_final_points := FLOOR(v_base_points * v_multiplier);
        v_new_total    := v_current_points + v_final_points;

        -- 6. Thực hiện cập nhật cộng dồn hoặc khởi tạo mới bản ghi điểm thưởng
        UPDATE LOYALTY_POINT
        SET    TOTAL_POINTS = v_new_total,
               UPDATED_AT   = SYSDATE
        WHERE  USER_ID = :NEW.USER_ID;

        IF SQL%ROWCOUNT = 0 THEN
            INSERT INTO LOYALTY_POINT (USER_ID, TOTAL_POINTS, UPDATED_AT)
            VALUES (:NEW.USER_ID, v_final_points, SYSDATE);
        END IF;

    END IF;
END;
/


--Trigger 10
CREATE OR REPLACE TRIGGER TRG_BLOCK_SUSPICIOUS_ORDER_SPIKE
BEFORE INSERT ON ORDERS
FOR EACH ROW
DECLARE
    v_order_count_10min NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO   v_order_count_10min
    FROM   ORDERS
    WHERE  USER_ID    = :NEW.USER_ID
      AND  IS_DELETED = 0
      AND  CREATED_AT >= SYSDATE - INTERVAL '10' MINUTE;

    IF v_order_count_10min >= 4 THEN
        RAISE_APPLICATION_ERROR(
            -20030,
            'Đặt hàng quá nhanh! Vui lòng thử lại sau 10 phút.'
        );
    END IF;
END;
/

--trigger 11
CREATE OR REPLACE TRIGGER TRG_DEDUCT_STOCK_ON_ORDER
AFTER INSERT ON ORDER_DETAIL
FOR EACH ROW
BEGIN
    -- Cứ mỗi dòng sản phẩm được chèn vào hóa đơn, lập tức trừ số lượng tồn kho của sản phẩm đó
    UPDATE PRODUCT
    SET STOCK_QUANTITY = STOCK_QUANTITY - :NEW.QUANTITY
    WHERE PRODUCT_ID = :NEW.PRODUCT_ID;
END;
/

