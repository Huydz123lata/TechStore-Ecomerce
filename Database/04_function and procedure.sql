--FUNCTION 1: fn_get_product_effective_price
CREATE OR REPLACE FUNCTION fn_get_product_effective_price (
    p_product_id IN NUMBER
) RETURN NUMBER
IS
    v_original_price   NUMBER(18,2);
    v_discount_value   NUMBER(18,2) := 0;
    v_effective_price  NUMBER(18,2);
BEGIN
    -- 1. Lấy giá gốc của sản phẩm
    SELECT PRICE
    INTO   v_original_price
    FROM   PRODUCT
    WHERE  PRODUCT_ID = p_product_id
      AND  IS_DELETED = 0;

    -- 2. Lấy giá trị giảm giá của khuyến mãi duy nhất đang áp dụng và còn hạn
    BEGIN
        SELECT pp.DISCOUNT_VALUE
        INTO   v_discount_value
        FROM   PROMOTION_PRODUCT pp
        JOIN   PROMOTION p ON pp.PROMOTION_ID = p.PROMOTION_ID
        WHERE  pp.PRODUCT_ID = p_product_id
          AND  p.IS_ACTIVE   = 1
          AND  p.IS_DELETED  = 0
          AND  SYSDATE BETWEEN p.START_AT AND p.END_AT;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Nếu sản phẩm không nằm trong chương trình khuyến mãi nào, mặc định mức giảm bằng 0
            v_discount_value := 0;
    END;

    -- 3. Tính giá thực tế sau giảm giá (đảm bảo không bị âm giá)
    v_effective_price := GREATEST(v_original_price - v_discount_value, 0);

    RETURN v_effective_price;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Trường hợp không tìm thấy sản phẩm tương ứng trong bảng PRODUCT
        RETURN NULL;
    WHEN OTHERS THEN
        RETURN NULL;
END fn_get_product_effective_price;
/

--FUNCTION 2: fn_get_user_loyalty_tier
CREATE OR REPLACE FUNCTION fn_get_user_loyalty_tier (
    p_user_id IN NUMBER
) RETURN VARCHAR2
IS
    v_points        NUMBER(10) := 0;
    v_tier          VARCHAR2(20);
    v_next_tier     VARCHAR2(20);
    v_points_needed NUMBER(10);
    v_result        VARCHAR2(200);
BEGIN
    -- 1. Lấy tổng điểm tích lũy (nếu chưa có bản ghi → 0 điểm)
    BEGIN
        SELECT TOTAL_POINTS
        INTO   v_points
        FROM   LOYALTY_POINT
        WHERE  USER_ID = p_user_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_points := 0;
    END;

    -- 2. Xác định hạng hiện tại và hạng kế tiếp
    IF v_points >= 5000 THEN
        v_tier          := 'KIM CƯƠNG';
        v_next_tier     := NULL;
        v_points_needed := 0;
    ELSIF v_points >= 2000 THEN
        v_tier          := 'VÀNG';
        v_next_tier     := 'KIM CƯƠNG';
        v_points_needed := 5000 - v_points;
    ELSIF v_points >= 500 THEN
        v_tier          := 'BẠC';
        v_next_tier     := 'VÀNG';
        v_points_needed := 2000 - v_points;
    ELSE
        v_tier          := 'ĐỒNG';
        v_next_tier     := 'BẠC';
        v_points_needed := 500 - v_points;
    END IF;

    -- 3. Tạo chuỗi kết quả
    IF v_next_tier IS NULL THEN
        v_result := v_tier || ' | ' || v_points || ' điểm | Hạng cao nhất';
    ELSE
        v_result := v_tier || ' | ' || v_points || ' điểm | ' ||
                    'Cần thêm ' || v_points_needed ||
                    ' điểm để đạt ' || v_next_tier;
    END IF;

    RETURN v_result;

EXCEPTION
    WHEN OTHERS THEN
        RETURN 'Lỗi truy vấn: ' || SQLERRM;
END fn_get_user_loyalty_tier;
/

--Function 3: fn_calculate_discount_amount
CREATE OR REPLACE FUNCTION fn_calculate_discount_amount (
    p_subtotal IN NUMBER,
    p_coupon_id IN NUMBER
) RETURN NUMBER
IS
    v_discount_type    NVARCHAR2(10);
    v_discount_value   NUMBER(18,2);
    v_min_order_value  NUMBER(18,2);
    v_max_discount     NUMBER(18,2);
    v_discount_amount  NUMBER(18,2) := 0;
BEGIN
    -- 1. Lấy thông tin cấu hình của Coupon
    SELECT DISCOUNT_TYPE, DISCOUNT_VALUE, MIN_ORDER_VALUE, MAX_DISCOUNT
    INTO v_discount_type, v_discount_value, v_min_order_value, v_max_discount
    FROM COUPON
    WHERE COUPON_ID = p_coupon_id
      AND IS_ACTIVE = 1
      AND IS_DELETED = 0
      AND SYSDATE BETWEEN START_AT AND END_AT;

    -- 2. Kiểm tra điều kiện giá trị đơn hàng tối thiểu
    IF p_subtotal < v_min_order_value THEN
        RETURN 0;
    END IF;

    -- 3. Tính toán dựa trên loại giảm giá
    IF v_discount_type = 'PERCENTAGE' THEN
        -- Tính theo %
        v_discount_amount := p_subtotal * (v_discount_value / 100);

        -- Áp dụng "Cái trần" MAX_DISCOUNT
        IF v_max_discount IS NOT NULL AND v_discount_amount > v_max_discount THEN
            v_discount_amount := v_max_discount;
        END IF;
    ELSE
        -- Nếu là loại 'AMOUNT' (giảm tiền mặt cố định)
        v_discount_amount := v_discount_value;
    END IF;

    -- 4. Trả về kết quả (đảm bảo không giảm quá tổng tiền đơn hàng)
    RETURN LEAST(v_discount_amount, p_subtotal);

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END;

--Procedure
CREATE OR REPLACE PROCEDURE sp_get_product_detail  (
    p_product_id IN NUMBER
)
IS
    v_product PRODUCT%ROWTYPE;
BEGIN
    SELECT *
    INTO v_product
    FROM PRODUCT
    WHERE PRODUCT_ID = p_product_id
      AND IS_DELETED = 0;

    DBMS_OUTPUT.PUT_LINE('Mã sản phẩm    : ' || v_product.PRODUCT_ID);
    DBMS_OUTPUT.PUT_LINE('Tên sản phẩm   : ' || v_product.NAME);
    DBMS_OUTPUT.PUT_LINE('Mô tả          : ' || NVL(v_product.DESCRIPTION, 'Không có mô tả'));
    DBMS_OUTPUT.PUT_LINE('Giá bán        : ' || TO_CHAR(v_product.PRICE, '999,999,999.99') || ' VNĐ');
    DBMS_OUTPUT.PUT_LINE('Thương hiệu    : ' || v_product.BRAND_ID);
    DBMS_OUTPUT.PUT_LINE('Số lượng tồn   : ' || v_product.STOCK_QUANTITY);
    DBMS_OUTPUT.PUT_LINE('Bảo hành       : ' || v_product.WARRANTY_MONTH || ' tháng');
END;
/


CREATE OR REPLACE PROCEDURE sp_clear_cart_item (
    p_cart_id IN NUMBER
)
IS
BEGIN

    DELETE FROM CART_ITEM
    WHERE CART_ID = p_cart_id;

    UPDATE CART
    SET UPDATED_AT = SYSDATE
    WHERE CART_ID = p_cart_id
      AND IS_DELETED = 0;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE('Đã làm trống giỏ hàng (Cart ID: ' || p_cart_id || ') thành công.');

END;
/


CREATE OR REPLACE PROCEDURE sp_get_top_selling_product (
    p_TopCount IN NUMBER
)
IS
BEGIN
    DBMS_OUTPUT.PUT_LINE('==================================================');
    DBMS_OUTPUT.PUT_LINE('      TOP ' || p_TopCount || ' SẢN PHẨM BÁN CHẠY NHẤT CỬA HÀNG');
    DBMS_OUTPUT.PUT_LINE('==================================================');

    -- Bước 2 đến 6: Truy vấn, tính toán, giới hạn ROWNUM và lặp để hiển thị
    FOR r IN (
        SELECT * FROM (
            SELECT
                P.PRODUCT_ID,
                P.NAME,
                SUM(OD.QUANTITY) AS TOTAL_SOLD
            FROM PRODUCT P
            JOIN ORDER_DETAIL OD ON P.PRODUCT_ID = OD.PRODUCT_ID
            JOIN ORDERS O ON OD.ORDER_ID = O.ORDER_ID
            WHERE O.IS_DELETED = 0
              AND O.STATUS != 'CANCELLED'
            GROUP BY P.PRODUCT_ID, P.NAME
            ORDER BY TOTAL_SOLD DESC
        ) WHERE ROWNUM <= p_TopCount
    ) LOOP
        DBMS_OUTPUT.PUT_LINE('Mã SP: ' || r.PRODUCT_ID
                          || ' | Tên: ' || r.NAME
                          || ' | Đã bán: ' || r.TOTAL_SOLD || ' sản phẩm');
    END LOOP;
END;
/


CREATE OR REPLACE PROCEDURE sp_get_top_selling_product_by_month (
    p_Month IN NUMBER,
    p_Year IN NUMBER,
    p_TopCount IN NUMBER
)
IS
BEGIN
    DBMS_OUTPUT.PUT_LINE('==================================================');
    DBMS_OUTPUT.PUT_LINE(' TOP ' || p_TopCount || ' SẢN PHẨM BÁN CHẠY NHẤT THÁNG ' || p_Month || '/' || p_Year);
    DBMS_OUTPUT.PUT_LINE('==================================================');

    -- Bước 2 đến 6: Truy vấn có kết hợp lọc thời gian, giới hạn ROWNUM và in kết quả
    FOR r IN (
        SELECT * FROM (
            SELECT
                P.PRODUCT_ID,
                P.NAME,
                SUM(OD.QUANTITY) AS TOTAL_SOLD
            FROM PRODUCT P
            JOIN ORDER_DETAIL OD ON P.PRODUCT_ID = OD.PRODUCT_ID
            JOIN ORDERS O ON OD.ORDER_ID = O.ORDER_ID
            WHERE O.IS_DELETED = 0
              AND O.STATUS != 'CANCELLED'
              AND EXTRACT(MONTH FROM O.CREATED_AT) = p_Month
              AND EXTRACT(YEAR FROM O.CREATED_AT) = p_Year
            GROUP BY P.PRODUCT_ID, P.NAME
            ORDER BY TOTAL_SOLD DESC
        ) WHERE ROWNUM <= p_TopCount
    ) LOOP
        DBMS_OUTPUT.PUT_LINE('Mã SP: ' || r.PRODUCT_ID
                          || ' | Tên: ' || r.NAME
                          || ' | Đã bán: ' || r.TOTAL_SOLD || ' sản phẩm');
    END LOOP;
END;
/

CREATE OR REPLACE PROCEDURE sp_add_to_cart (
    p_user_id    IN NUMBER,
    p_product_id IN NUMBER,
    p_quantity   IN NUMBER
) IS
    v_cart_id    NUMBER(10);
    v_price      NUMBER(18,2);
    v_count      NUMBER(10);
BEGIN
    -- BƯỚC 1 & 2: Lấy mã giỏ hàng và giá sản phẩm
    BEGIN
        SELECT CART_ID INTO v_cart_id
        FROM CART
        WHERE USER_ID = p_user_id AND IS_DELETED = 0;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            INSERT INTO CART (USER_ID) VALUES (p_user_id)
            RETURNING CART_ID INTO v_cart_id;
    END;

    -- Lấy giá sản phẩm từ bảng PRODUCT
    SELECT PRICE INTO v_price FROM PRODUCT WHERE PRODUCT_ID = p_product_id;

    -- BƯỚC 3: Kiểm tra sản phẩm đã có trong giỏ chưa
    SELECT COUNT(*) INTO v_count
    FROM CART_ITEM
    WHERE CART_ID = v_cart_id AND PRODUCT_ID = p_product_id;

    IF v_count > 0 THEN
        -- Nếu đã có: Cập nhật cộng dồn QUANTITY và tính lại TOTAL
        UPDATE CART_ITEM
        SET QUANTITY = QUANTITY + p_quantity,
            TOTAL = (QUANTITY + p_quantity) * v_price
        WHERE CART_ID = v_cart_id AND PRODUCT_ID = p_product_id;
    ELSE
        -- Nếu chưa có: Thêm mới dòng vào chi tiết giỏ hàng
        INSERT INTO CART_ITEM (CART_ID, PRODUCT_ID, QUANTITY, TOTAL)
        VALUES (v_cart_id, p_product_id, p_quantity, p_quantity * v_price);
    END IF;

    -- BƯỚC 4: Cập nhật lại thời gian cập nhật giỏ hàng
    UPDATE CART SET UPDATED_AT = SYSDATE WHERE CART_ID = v_cart_id;

    -- BƯỚC 5: Hiển thị thông báo (Khớp 100% mẫu ảnh bạn gửi)
    DBMS_OUTPUT.PUT_LINE('Sản phẩm đã được thêm vào giỏ hàng.');
    DBMS_OUTPUT.PUT_LINE('Mã sản phẩm: ' || p_product_id);
    DBMS_OUTPUT.PUT_LINE('Số lượng: ' || p_quantity);
    DBMS_OUTPUT.PUT_LINE('Giỏ hàng ID: ' || v_cart_id);

    COMMIT;
END;



BEGIN
    -- Gọi procedure: sp_add_to_cart(p_user_id, p_product_id, p_quantity)
    sp_add_to_cart(10, 200, 2);
END;
/


CREATE OR REPLACE PROCEDURE sp_get_user_order_history (
    p_user_id IN NUMBER
) IS
    -- Cursor lấy danh sách đơn hàng
    CURSOR c_orders IS
        SELECT ORDER_ID, TOTAL, STATUS, CREATED_AT
        FROM ORDERS
        WHERE USER_ID = p_user_id AND IS_DELETED = 0
        ORDER BY CREATED_AT DESC;

    -- Cursor lấy chi tiết từng món trong đơn hàng đó
    CURSOR c_details(p_order_id NUMBER) IS
        SELECT p.NAME, od.QUANTITY, od.PRICE, od.LINE_TOTAL
        FROM ORDER_DETAIL od
        JOIN PRODUCT p ON od.PRODUCT_ID = p.PRODUCT_ID
        WHERE od.ORDER_ID = p_order_id;
BEGIN
    DBMS_OUTPUT.PUT_LINE('LỊCH SỬ ĐƠN HÀNG CỦA USER ID: ' || p_user_id);
    DBMS_OUTPUT.PUT_LINE('-------------------------------------------');

    FOR r_order IN c_orders LOOP
        DBMS_OUTPUT.PUT_LINE('Đơn hàng: ' || r_order.ORDER_ID ||
                             ' | Ngày: ' || TO_CHAR(r_order.CREATED_AT, 'DD/MM/YY') ||
                             ' | Trạng thái: ' || r_order.STATUS);

        -- Duyệt chi tiết sản phẩm của đơn hàng này
        FOR r_detail IN c_details(r_order.ORDER_ID) LOOP
            DBMS_OUTPUT.PUT_LINE('  + ' || r_detail.NAME ||
                                 ' | SL: ' || r_detail.QUANTITY ||
                                 ' | Thành tiền: ' || r_detail.LINE_TOTAL);
        END LOOP;

        DBMS_OUTPUT.PUT_LINE('=> TỔNG CỘNG ĐƠN HÀNG: ' || r_order.TOTAL);
        DBMS_OUTPUT.PUT_LINE('-------------------------------------------');
    END LOOP;
END;
/

CREATE OR REPLACE PROCEDURE sp_place_order (
    p_user_id        IN NUMBER,
    p_receiver_name  IN VARCHAR2,
    p_receiver_phone IN VARCHAR2,
    p_address        IN VARCHAR2,
    p_payment_method IN VARCHAR2, -- 'COD' hoặc 'BANK'
    p_coupon_code    IN VARCHAR2 DEFAULT NULL
) IS
    v_cart_id         NUMBER(10);
    v_order_id        NUMBER(10);
    v_subtotal        NUMBER(18,2) := 0;
    v_coupon_id       NUMBER := NULL;
    v_discount_amount NUMBER(18,2) := 0;
    v_total           NUMBER(18,2) := 0;
    v_pay_status      VARCHAR2(20);
BEGIN
    -- 1. Tìm giỏ hàng & Tính tiền (Gọi Function giảm giá của bạn)
    SELECT CART_ID INTO v_cart_id FROM CART WHERE USER_ID = p_user_id AND IS_DELETED = 0;
    SELECT SUM(TOTAL) INTO v_subtotal FROM CART_ITEM WHERE CART_ID = v_cart_id;

    IF p_coupon_code IS NOT NULL THEN
        SELECT COUPON_ID INTO v_coupon_id FROM COUPON WHERE CODE = p_coupon_code;
        v_discount_amount := fn_calculate_discount_amount(v_subtotal, v_coupon_id);
    END IF;

    v_total := v_subtotal - v_discount_amount;

    -- 2. Logic xử lý trạng thái thanh toán
    IF p_payment_method = 'COD' THEN
        v_pay_status := 'PENDING'; -- Khách chưa trả tiền
    ELSE
        v_pay_status := 'COMPLETED'; -- Giả sử thanh toán online đã xong
    END IF;

    -- 3. Tạo đơn hàng
    INSERT INTO ORDERS (
        USER_ID, COUPON_ID, SUBTOTAL, DISCOUNT_AMOUNT, TOTAL,
        STATUS, RECEIVER_NAME, RECEIVER_PHONE, SHIPPING_ADDRESS
    ) VALUES (
        p_user_id, v_coupon_id, v_subtotal, v_discount_amount, v_total,
        'CONFIRMED', p_receiver_name, p_receiver_phone, p_address
    ) RETURNING ORDER_ID INTO v_order_id;

    -- 4. Tạo bản ghi Thanh toán (PAYMENT)
    INSERT INTO PAYMENT (
        ORDER_ID, PAYMENT_METHOD, AMOUNT, PAYMENT_STATUS, CREATED_AT
    ) VALUES (
        v_order_id, p_payment_method, v_total, v_pay_status, SYSDATE
    );

    -- 5. Chép chi tiết & Xóa giỏ hàng (Gọi Proc của bạn)
    INSERT INTO ORDER_DETAIL (ORDER_ID, PRODUCT_ID, QUANTITY, PRICE, LINE_TOTAL)
    SELECT v_order_id, ci.PRODUCT_ID, ci.QUANTITY, p.PRICE, ci.TOTAL
    FROM CART_ITEM ci JOIN PRODUCT p ON ci.PRODUCT_ID = p.PRODUCT_ID
    WHERE ci.CART_ID = v_cart_id;

    sp_clear_cart_item(v_cart_id);

    -- In thông báo đầy đủ các con số
    DBMS_OUTPUT.PUT_LINE('------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Đặt hàng thành công! (Hình thức: ' || p_payment_method || ')');
    DBMS_OUTPUT.PUT_LINE('Trạng thái thanh toán: ' || v_pay_status);
    DBMS_OUTPUT.PUT_LINE('Tạm tính: ' || TO_CHAR(v_subtotal, '999,999,999,999'));
    DBMS_OUTPUT.PUT_LINE('Giảm giá: ' || TO_CHAR(v_discount_amount, '999,999,999,999'));
    DBMS_OUTPUT.PUT_LINE('Tổng cộng: ' || TO_CHAR(v_total, '999,999,999,999'));
    DBMS_OUTPUT.PUT_LINE('------------------------------------');

    COMMIT;
END;
/

CREATE OR REPLACE PROCEDURE sp_print_order_info (
    p_order_id IN NUMBER
)
IS
    v_total        ORDERS.TOTAL%TYPE;
    v_status       ORDERS.STATUS%TYPE;
    v_coupon_id    ORDERS.COUPON_ID%TYPE;
    v_coupon_code  COUPON.CODE%TYPE;
    v_discount_val COUPON.DISCOUNT_VALUE%TYPE;

    v_prod_name    PRODUCT.NAME%TYPE;
    v_qty          ORDER_DETAIL.QUANTITY%TYPE;
    v_price        ORDER_DETAIL.PRICE%TYPE;


    CURSOR cur_order_details IS
        SELECT p.NAME, od.QUANTITY, od.PRICE
        FROM ORDER_DETAIL od
        JOIN PRODUCT p ON od.PRODUCT_ID = p.PRODUCT_ID
        WHERE od.ORDER_ID = p_order_id;

BEGIN

    SELECT TOTAL, STATUS, COUPON_ID
    INTO v_total, v_status, v_coupon_id
    FROM ORDERS
    WHERE ORDER_ID = p_order_id;

    DBMS_OUTPUT.PUT_LINE('=== THÔNG TIN ĐƠN HÀNG [' || p_order_id || '] ===');
    DBMS_OUTPUT.PUT_LINE('Trạng thái : ' || v_status);


    IF v_coupon_id IS NOT NULL THEN
        SELECT CODE, DISCOUNT_VALUE INTO v_coupon_code, v_discount_val
        FROM COUPON WHERE COUPON_ID = v_coupon_id;
        DBMS_OUTPUT.PUT_LINE('Mã giảm giá: ' || v_coupon_code || ' (Mức giảm: ' || v_discount_val || ')');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Mã giảm giá: Không áp dụng.');
    END IF;

    DBMS_OUTPUT.PUT_LINE('--- DANH SÁCH SẢN PHẨM ---');


    OPEN cur_order_details;
    LOOP
        FETCH cur_order_details INTO v_prod_name, v_qty, v_price;
        EXIT WHEN cur_order_details%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('- ' || v_prod_name || ' | SL: ' || v_qty || ' | Đơn giá: ' || v_price);
    END LOOP;
    CLOSE cur_order_details;

    DBMS_OUTPUT.PUT_LINE('--------------------------');
    DBMS_OUTPUT.PUT_LINE('TỔNG THANH TOÁN: ' || v_total);

END;
/






