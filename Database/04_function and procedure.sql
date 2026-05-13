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
BEGIN
    -- 1. Lấy hoặc tạo mã giỏ hàng
    BEGIN
        SELECT CART_ID INTO v_cart_id
        FROM CART
        WHERE USER_ID = p_user_id AND IS_DELETED = 0;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            INSERT INTO CART (USER_ID) VALUES (p_user_id)
            RETURNING CART_ID INTO v_cart_id;
    END;

    -- 2. Thêm hoặc cập nhật chi tiết giỏ hàng
    MERGE INTO CART_ITEM ci
    USING DUAL ON (ci.CART_ID = v_cart_id AND ci.PRODUCT_ID = p_product_id)
    WHEN MATCHED THEN
        UPDATE SET ci.QUANTITY = ci.QUANTITY + p_quantity
    WHEN NOT MATCHED THEN
        INSERT (CART_ID, PRODUCT_ID, QUANTITY)
        VALUES (v_cart_id, p_product_id, p_quantity);

    -- 3. Cập nhật thời gian cho giỏ hàng
    UPDATE CART SET UPDATED_AT = SYSDATE WHERE CART_ID = v_cart_id;

    -- 4. HIỂN THỊ THEO ĐÚNG MẪU ẢNH CỦA BẠN
    DBMS_OUTPUT.PUT_LINE('Sản phẩm đã được thêm vào giỏ hàng.');
    DBMS_OUTPUT.PUT_LINE('Mã sản phẩm: ' || p_product_id);
    DBMS_OUTPUT.PUT_LINE('Số lượng: ' || p_quantity);
    DBMS_OUTPUT.PUT_LINE('Giỏ hàng ID: ' || v_cart_id);

    COMMIT;
END;
/

DROP PROCEDURE AddToCart;

-- 1. Thêm Thương hiệu
INSERT INTO BRAND (NAME) VALUES ('Apple');
INSERT INTO BRAND (NAME) VALUES ('Samsung');

-- 2. Thêm Danh mục
INSERT INTO CATEGORY (NAME) VALUES ('Điện thoại');
INSERT INTO CATEGORY (NAME) VALUES ('Phụ kiện');

-- 3. Thêm Sản phẩm (Để ý ID thường sẽ là 1 và 2)
INSERT INTO PRODUCT (NAME, IMAGE_NAME, DESCRIPTION, PRICE, STOCK_QUANTITY, WARRANTY_MONTH, CATEGORY_ID, BRAND_ID)
VALUES ('iPhone 15 Pro', 'iphone15.jpg', 'Chính hãng VN/A', 28000000, 50, 12, 1, 1);

INSERT INTO PRODUCT (NAME, IMAGE_NAME, DESCRIPTION, PRICE, STOCK_QUANTITY, WARRANTY_MONTH, CATEGORY_ID, BRAND_ID)
VALUES ('Ốp lưng MagSafe', 'op-magsafe.jpg', 'Chống sốc tốt', 1500000, 100, 6, 2, 1);

-- 4. Thêm Người dùng
INSERT INTO APP_USER (FULL_NAME, PHONE_NUMBER, ADDRESS, GENDER)
VALUES ('Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Nam');

COMMIT;

BEGIN
    -- Thêm 2 chiếc iPhone (ID = 1) cho người dùng (ID = 1)
    AddToCart(
        p_user_id    => 1,
        p_product_id => 2,
        p_quantity   => 2
    );
END;
/
select * from CART_ITEM;