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
    IF v_discount_type = 'PERCENT' THEN
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
CREATE OR REPLACE PROCEDURE sp_print_order_info (order_id IN NUMBER)
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
        WHERE od.ORDER_ID = order_id;

BEGIN

    SELECT TOTAL, STATUS, COUPON_ID
    INTO v_total, v_status, v_coupon_id
    FROM ORDERS
    WHERE ORDER_ID = order_id;

    DBMS_OUTPUT.PUT_LINE('=== THÔNG TIN ĐƠN HÀNG [' || order_id || '] ===');
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
	p_name IN VARCHAR2, p_description IN VARCHAR2, p_price IN NUMBER,
	p_brand IN VARCHAR2, p_category_id IN NUMBER, p_warranty IN NUMBER
) IS
BEGIN
	INSERT INTO PRODUCT (NAME, DESCRIPTION, PRICE, BRAND, CATEGORY_ID,    WARRANTY_MONTH, STATUS)
	VALUES (p_name, p_description, p_price, p_brand, p_category_id, p_warranty,1);
	COMMIT;
END;


CREATE OR REPLACE PROCEDURE update_product (
	prod_id IN NUMBER, name IN VARCHAR2, description IN VARCHAR2,
	price IN NUMBER, brand IN VARCHAR2, cat_id IN NUMBER,
	warranty IN NUMBER, status IN NUMBER
) IS
BEGIN
	UPDATE PRODUCT
	SET NAME = name, DESCRIPTION = description, PRICE = price,
    	BRAND = brand, CATEGORY_ID = cat_id, WARRANTY_MONTH = warranty,
    	STATUS = status, UPDATED_AT = SYSDATE
	WHERE PRODUCT_ID = prod_id;
	COMMIT;
END;

CREATE OR REPLACE PROCEDURE sp_delete_product (
    prod_id IN NUMBER
) IS
BEGIN
    UPDATE PRODUCT
    SET IS_DELETED = 1, UPDATED_AT = SYSDATE
    WHERE PRODUCT_ID = prod_id;

    COMMIT;
END;


CREATE OR REPLACE PROCEDURE insert_category (
	name IN VARCHAR2, parent_id IN NUMBER
) IS
BEGIN
	INSERT INTO CATEGORY (NAME, PARENT_CATEGORY_ID)
	VALUES (name, parent_id);
	COMMIT;
END;


CREATE OR REPLACE PROCEDURE update_category(
cat_id IN NUMBER, name IN VARCHAR2, parent_id IN NUMBER
) IS
BEGIN
	UPDATE CATEGORY
	SET NAME = name, PARENT_CATEGORY_ID = parent_id, UPDATED_AT = SYSDATE
	WHERE CATEGORY_ID = cat_id;
	COMMIT;
END;


CREATE OR REPLACE PROCEDURE sp_delete_category (
    cat_id IN NUMBER
) IS
BEGIN
    UPDATE CATEGORY
    SET IS_DELETED = 1, UPDATED_AT = SYSDATE
    WHERE CATEGORY_ID = cat_id;

    COMMIT;
END;


CREATE OR REPLACE PROCEDURE insert_coupon (
	code_str IN VARCHAR2, disc_type IN VARCHAR2, disc_value IN NUMBER,
	min_order IN NUMBER, max_disc IN NUMBER, start_date IN DATE,
	end_date IN DATE, desc_info IN VARCHAR2, usage_limit IN NUMBER
) IS
BEGIN
	INSERT INTO COUPON (CODE, DISCOUNT_TYPE, DISCOUNT_VALUE,   MIN_ORDER_VALUE, MAX_DISCOUNT, START_AT, END_AT, IS_ACTIVE, DESCRIPTION, USAGE_LIMIT)
	VALUES (code_str, disc_type, disc_value, min_order, max_disc, start_date, end_date, 1,desc_info, usage_limit);
	COMMIT;
END;


CREATE OR REPLACE PROCEDURE update_coupon (
	coupon_id IN NUMBER, code_str IN VARCHAR2, disc_type IN VARCHAR2,
	disc_value IN NUMBER, min_order IN NUMBER, max_disc IN NUMBER,
	start_date IN DATE, end_date IN DATE, is_active IN NUMBER,
	desc_info IN VARCHAR2, usage_limit IN NUMBER
) IS
BEGIN
	UPDATE COUPON
	SET CODE = code_str, DISCOUNT_TYPE = disc_type, DISCOUNT_VALUE = disc_value,
    	MIN_ORDER_VALUE = min_order, MAX_DISCOUNT = max_disc,
    	START_AT = start_date, END_AT = end_date, IS_ACTIVE = is_active,
    	DESCRIPTION = desc_info, USAGE_LIMIT = usage_limit, UPDATED_AT = SYSDATE
	WHERE COUPON_ID = coupon_id;
	COMMIT;
END;


CREATE OR REPLACE PROCEDURE sp_delete_coupon (
    coupon_id IN NUMBER
) IS
BEGIN
    UPDATE COUPON
    SET IS_DELETED = 1, IS_ACTIVE = 0, UPDATED_AT = SYSDATE
    WHERE COUPON_ID = coupon_id;

    COMMIT;
END;


CREATE OR REPLACE PROCEDURE update_order_status (
    order_id IN NUMBER,
    new_status IN VARCHAR2
) IS

    CURSOR cur_order_details IS
        SELECT PRODUCT_ID, WAREHOUSE_ID, QUANTITY
        FROM ORDER_DETAIL
        WHERE ORDER_ID = update_order_status.order_id;

    v_prod_id ORDER_DETAIL.PRODUCT_ID%TYPE;
    v_wh_id ORDER_DETAIL.WAREHOUSE_ID%TYPE;
    v_qty ORDER_DETAIL.QUANTITY%TYPE;

BEGIN

    UPDATE ORDERS
    SET STATUS = update_order_status.new_status,
        UPDATED_AT = SYSDATE
    WHERE ORDER_ID = update_order_status.order_id;

    -- Xử lý phát sinh nếu trạng thái là 'CANCELLED'
    IF new_status = 'CANCELLED' THEN

        UPDATE PAYMENT
        SET PAYMENT_STATUS = 'REFUNDED'
        WHERE ORDER_ID = update_order_status.order_id
          AND PAYMENT_STATUS = 'COMPLETED';

        -- HOÀN TRẢ SỐ LƯỢNG VỀ KHO
        OPEN cur_order_details;
        LOOP
            FETCH cur_order_details INTO v_prod_id, v_wh_id, v_qty;
            EXIT WHEN cur_order_details%NOTFOUND;

            UPDATE INVENTORY
            SET QUANTITY = QUANTITY + v_qty,
                UPDATED_AT = SYSDATE
            WHERE PRODUCT_ID = v_prod_id
              AND WAREHOUSE_ID = v_wh_id;
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

