/* =============================================================================
   FUNCTION 1: fn_get_product_effective_price

   Mô tả: Tính giá THỰC TẾ của một sản phẩm tại thời điểm hiện tại, có tính
   đến khuyến mãi đang chạy (bảng PROMOTION_PRODUCT). Trả về:
     - Giá sau khuyến mãi nếu sản phẩm đang trong chiến dịch khuyến mãi còn hạn.
     - Giá gốc nếu không có khuyến mãi nào áp dụng.

   Cách dùng:
     SELECT fn_get_product_effective_price(1) FROM DUAL;
     -- Hoặc dùng trong query để hiển thị giá khuyến mãi cho toàn danh mục:
     SELECT NAME, PRICE, fn_get_product_effective_price(PRODUCT_ID) AS EFFECTIVE_PRICE
     FROM PRODUCT WHERE IS_DELETED = 0;
   ============================================================================= */
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

    -- 2. Tìm khuyến mãi đang chạy có giảm giá cao nhất cho sản phẩm này
    --    (một sản phẩm có thể thuộc nhiều chiến dịch → lấy mức giảm tốt nhất)
    BEGIN
        SELECT MAX(pp.DISCOUNT_VALUE)
        INTO   v_discount_value
        FROM   PROMOTION_PRODUCT pp
        JOIN   PROMOTION p ON pp.PROMOTION_ID = p.PROMOTION_ID
        WHERE  pp.PRODUCT_ID = p_product_id
          AND  p.IS_ACTIVE   = 1
          AND  p.IS_DELETED  = 0
          AND  SYSDATE BETWEEN p.START_AT AND p.END_AT;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_discount_value := 0;
    END;

    -- 3. Tính giá hiệu lực (không để giá âm)
    v_effective_price := GREATEST(
                             v_original_price - NVL(v_discount_value, 0),
                             0
                         );

    RETURN v_effective_price;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Sản phẩm không tồn tại → trả về NULL để caller tự xử lý
        RETURN NULL;
    WHEN OTHERS THEN
        RETURN NULL;
END fn_get_product_effective_price;
/


/* =============================================================================
   FUNCTION 2: fn_get_user_loyalty_tier

   Mô tả: Trả về thông tin đầy đủ về hạng thành viên của một user dưới dạng
   VARCHAR2, bao gồm: tên hạng, tổng điểm hiện tại, và số điểm còn thiếu để
   lên hạng tiếp theo. Hữu ích cho màn hình "Tài khoản của tôi" hoặc báo cáo.

   Output format:
     "VÀNG | 2350 điểm | Cần thêm 1650 điểm để đạt KIM CƯƠNG"
     "KIM CƯƠNG | 6200 điểm | Hạng cao nhất"

   Cách dùng:
     SELECT fn_get_user_loyalty_tier(1) FROM DUAL;
     -- Hoặc trong báo cáo:
     SELECT u.FULL_NAME, fn_get_user_loyalty_tier(u.USER_ID) AS MEMBERSHIP_INFO
     FROM APP_USER u WHERE u.IS_DELETED = 0;
   ============================================================================= */
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


--HoaiBaoViet
CREATE OR REPLACE PROCEDURE sp_print_order_info (p_order_id IN NUMBER)
IS
    v_total ORDERS.TOTAL%TYPE;
    v_status ORDERS.STATUS%TYPE;

    v_coupon_id ORDERS.COUPON_ID%TYPE;
    v_coupon_code COUPON.CODE%TYPE;
    v_discount_val COUPON.DISCOUNT_VALUE%TYPE;

    v_prod_name PRODUCT.NAME%TYPE;
    v_qty ORDER_DETAIL.QUANTITY%TYPE;
    v_price ORDER_DETAIL.PRICE%TYPE;

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


CREATE OR REPLACE PROCEDURE insert_product(
    p_name        IN VARCHAR2,
    p_image       IN VARCHAR2,
    p_description IN VARCHAR2,
    p_price       IN NUMBER,
    p_brand_id    IN NUMBER,
    p_category_id IN NUMBER,
    p_warranty    IN NUMBER,
    p_stock       IN NUMBER
) IS
BEGIN
    INSERT INTO PRODUCT (
        NAME, IMAGE_NAME, DESCRIPTION, PRICE,
        BRAND_ID, CATEGORY_ID, WARRANTY_MONTH,
        STATUS, STOCK_QUANTITY
    )
    VALUES (
        p_name, p_image, p_description, p_price,
        p_brand_id, p_category_id, p_warranty,
        1, p_stock
    );
    COMMIT;
END;
/

CREATE OR REPLACE PROCEDURE update_product (
    prod_id       IN NUMBER,
    p_name        IN VARCHAR2,
    p_description IN VARCHAR2,
    p_price       IN NUMBER,
    p_brand_id    IN NUMBER,
    p_cat_id      IN NUMBER,
    p_warranty    IN NUMBER,
    p_status      IN NUMBER,
    p_stock       IN NUMBER
) IS
BEGIN
    UPDATE PRODUCT
    SET NAME            = p_name,
        DESCRIPTION     = p_description,
        PRICE           = p_price,
        BRAND_ID        = p_brand_id,
        CATEGORY_ID     = p_cat_id,
        WARRANTY_MONTH  = p_warranty,
        STATUS          = p_status,
        STOCK_QUANTITY  = p_stock,
        UPDATED_AT      = SYSDATE -- Cập nhật thời gian sửa đổi
    WHERE PRODUCT_ID = prod_id;

    COMMIT;
END;
/

CREATE OR REPLACE PROCEDURE sp_delete_product (
    prod_id IN NUMBER
) IS
BEGIN
    UPDATE PRODUCT
    SET IS_DELETED = 1, UPDATED_AT = SYSDATE
    WHERE PRODUCT_ID = prod_id;

    COMMIT;
END;




--UPDATE KHI SỬA BẢNG [4]
CREATE OR REPLACE PROCEDURE insert_coupon (
    p_code_str    IN VARCHAR2,
    p_disc_type   IN VARCHAR2,
    p_disc_value  IN NUMBER,
    p_min_order   IN NUMBER,
    p_max_disc    IN NUMBER,
    p_start_date  IN DATE,
    p_end_date    IN DATE,
    p_desc_info   IN VARCHAR2,
    p_aff_id      IN NUMBER DEFAULT NULL
) IS
BEGIN
    INSERT INTO COUPON (
        CODE, DISCOUNT_TYPE, DISCOUNT_VALUE,
        MIN_ORDER_VALUE, MAX_DISCOUNT, START_AT,
        END_AT, IS_ACTIVE, DESCRIPTION, AFFILIATE_ID
    )
    VALUES (
        p_code_str, p_disc_type, p_disc_value,
        p_min_order, p_max_disc, p_start_date,
        p_end_date, 1, p_desc_info, p_aff_id
    );
    COMMIT;
END;

--UPDATE KHI SỬA BẢNG [5]
CREATE OR REPLACE PROCEDURE update_coupon (
    p_coupon_id   IN NUMBER,
    p_code_str    IN VARCHAR2,
    p_disc_type   IN VARCHAR2,
    p_disc_value  IN NUMBER,
    p_min_order   IN NUMBER,
    p_max_disc    IN NUMBER,
    p_start_date  IN DATE,
    p_end_date    IN DATE,
    p_is_active   IN NUMBER,
    p_desc_info   IN VARCHAR2,
    p_affiliate_id IN NUMBER
) IS
BEGIN
    UPDATE COUPON
    SET CODE            = p_code_str,
        DISCOUNT_TYPE   = p_disc_type,
        DISCOUNT_VALUE  = p_disc_value,
        MIN_ORDER_VALUE = p_min_order,
        MAX_DISCOUNT    = p_max_disc,
        START_AT        = p_start_date,
        END_AT          = p_end_date,
        IS_ACTIVE       = p_is_active,
        DESCRIPTION     = p_desc_info,
        AFFILIATE_ID    = p_affiliate_id,
        UPDATED_AT      = SYSDATE
    WHERE COUPON_ID = p_coupon_id;

    COMMIT;
END;

--UPDATE KHI SỬA BẢNG [6]
CREATE OR REPLACE PROCEDURE sp_delete_coupon (
    p_coupon_id IN NUMBER
) IS
BEGIN
    UPDATE COUPON
    SET IS_DELETED = 1,
        IS_ACTIVE  = 0,
        UPDATED_AT = SYSDATE
    WHERE COUPON_ID = p_coupon_id;

    -- Kiểm tra xem có dòng nào được update không (Tránh xóa nhầm ID không tồn tại)
    IF SQL%ROWCOUNT = 0 THEN
        DBMS_OUTPUT.PUT_LINE('Cảnh báo: Không tìm thấy Coupon ID ' || p_coupon_id);
    END IF;

    COMMIT;
END;

--UPDATE KHI SỬA BẢNG [1]
CREATE OR REPLACE PROCEDURE update_order_status (
    p_order_id IN NUMBER,
    p_new_status IN VARCHAR2
) IS
    CURSOR cur_order_details IS
        SELECT PRODUCT_ID, QUANTITY
        FROM ORDER_DETAIL
        WHERE ORDER_ID = p_order_id;

    v_prod_id ORDER_DETAIL.PRODUCT_ID%TYPE;
    v_qty     ORDER_DETAIL.QUANTITY%TYPE;
BEGIN
    -- 1. Cập nhật trạng thái đơn hàng
    UPDATE ORDERS
    SET STATUS = p_new_status,
        UPDATED_AT = SYSDATE
    WHERE ORDER_ID = p_order_id;

    -- 2. Nếu đơn hàng bị Hủy (Sửa lại đúng 'CANCEL' theo CONSTRAINT của bạn)
    IF p_new_status = 'CANCELLED' THEN
        -- Hoàn tiền nếu đã thanh toán
        UPDATE PAYMENT
        SET PAYMENT_STATUS = 'REFUNDED'
        WHERE ORDER_ID = p_order_id
          AND PAYMENT_STATUS = 'COMPLETED';

        -- HOÀN TRẢ SỐ LƯỢNG VÀO BẢNG PRODUCT
        OPEN cur_order_details;
        LOOP
            FETCH cur_order_details INTO v_prod_id, v_qty;
            EXIT WHEN cur_order_details%NOTFOUND;

            UPDATE PRODUCT
            SET STOCK_QUANTITY = STOCK_QUANTITY + v_qty,
                UPDATED_AT = SYSDATE
            WHERE PRODUCT_ID = v_prod_id;
        END LOOP;
        CLOSE cur_order_details;
    END IF;

    COMMIT;
END;

CREATE OR REPLACE PROCEDURE insert_promotion (
    promo_name IN VARCHAR2,
    start_date IN DATE,
    end_date IN DATE
) IS
BEGIN
    INSERT INTO PROMOTION (PROMOTION_NAME, START_AT, END_AT, IS_ACTIVE)
    VALUES (promo_name, start_date, end_date,1);

    COMMIT;
END;


CREATE OR REPLACE PROCEDURE update_promotion (
    promo_id IN NUMBER,
    promo_name IN VARCHAR2,
    start_date IN DATE,
    end_date IN DATE,
    is_active_status IN NUMBER
) IS
BEGIN
    UPDATE PROMOTION
    SET PROMOTION_NAME = promo_name,
        START_AT = start_date,
        END_AT = end_date,
        IS_ACTIVE = is_active_status,
        UPDATED_AT = SYSDATE
    WHERE PROMOTION_ID = promo_id;

    COMMIT;
END;


CREATE OR REPLACE PROCEDURE sp_delete_promotion (
    promo_id IN NUMBER
) IS
BEGIN
    UPDATE PROMOTION
    SET IS_DELETED = 1,
        IS_ACTIVE = 0,
        UPDATED_AT = SYSDATE
    WHERE PROMOTION_ID = promo_id;

    COMMIT;
END;

CREATE OR REPLACE PROCEDURE AddToCart (
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
/

CREATE OR REPLACE PROCEDURE sp_delete_cart (
    p_cart_id IN NUMBER
) IS
BEGIN
    -- Xóa toàn bộ các sản phẩm thuộc giỏ hàng này
    DELETE FROM CART_ITEM
    WHERE CART_ID = p_cart_id;

    -- Cập nhật lại thời gian của giỏ hàng (tùy chọn)
    UPDATE CART
    SET UPDATED_AT = SYSDATE
    WHERE CART_ID = p_cart_id;

    DBMS_OUTPUT.PUT_LINE('Giỏ hàng ' || p_cart_id || ' đã được dọn dẹp.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Lỗi khi xóa giỏ hàng: ' || SQLERRM);
        RAISE;
END;
/

CREATE OR REPLACE PROCEDURE sp_PlaceOrder (
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

    sp_delete_cart(v_cart_id);

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
CREATE OR REPLACE PROCEDURE sp_GetUserOrderHistory (
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






