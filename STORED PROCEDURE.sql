-- PART 1: FUNCTIONS

-- FUNCTION 1: CALCULATE ORDER TOTAL --
CREATE OR REPLACE FUNCTION FN_CALCULATE_ORDER_TOTAL (
    f_order_id IN ORDERS.ORDER_ID%TYPE
) RETURN NUMBER
IS
    v_total NUMBER := 0;
BEGIN
    SELECT NVL(SUM(QUANTITY * PRICE), 0)
    INTO v_total
    FROM ORDER_DETAIL
    WHERE ORDER_ID = f_order_id;

    RETURN v_total;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
END;


-- FUNCTION 2: CHECK CURRENT STOCK --
CREATE OR REPLACE FUNCTION FN_GET_CURRENT_STOCK (
    f_product_id IN INVENTORY.PRODUCT_ID%TYPE,
    f_warehouse_id IN INVENTORY.WAREHOUSE_ID%TYPE
) RETURN NUMBER
IS
    v_stock NUMBER := 0;
BEGIN
    SELECT QUANTITY
    INTO v_stock
    FROM INVENTORY
    WHERE PRODUCT_ID = f_product_id
      AND WAREHOUSE_ID = f_warehouse_id;

    RETURN v_stock;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
END;

-- PART 2: STORED PROCEDURES

-- 1. CREATE ACCOUNT --
CREATE OR REPLACE PROCEDURE SP_CREATE_ACCOUNT (
    p_user_id IN ACCOUNT.USER_ID%TYPE,
    p_username IN ACCOUNT.USERNAME%TYPE,
    p_password_hash IN ACCOUNT.PASSWORD_HASH%TYPE
) IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ACCOUNT WHERE USERNAME = p_username;

    IF v_count > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Username already exists.');
    ELSE
        INSERT INTO ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, STATUS)
        VALUES (p_user_id, p_username, p_password_hash, 'ACTIVE');

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Account created successfully.');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('System error: ' || SQLERRM);
END;


-- 2. CHANGE PASSWORD --
CREATE OR REPLACE PROCEDURE SP_CHANGE_PASSWORD (
    p_account_id IN ACCOUNT.ACCOUNT_ID%TYPE,
    p_new_password_hash IN ACCOUNT.PASSWORD_HASH%TYPE
) IS
    v_account_id ACCOUNT.ACCOUNT_ID%TYPE;
BEGIN
    SELECT ACCOUNT_ID INTO v_account_id
    FROM ACCOUNT
    WHERE ACCOUNT_ID = p_account_id AND IS_DELETED = 0;

    IF v_account_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('Account does not exist or has been deleted.');
    ELSE
        UPDATE ACCOUNT
        SET PASSWORD_HASH = p_new_password_hash,
            UPDATED_AT = SYSDATE
        WHERE ACCOUNT_ID = p_account_id;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Password changed successfully.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Account does not exist.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;


-- 3. VERIFY LOGIN --
CREATE OR REPLACE PROCEDURE SP_VERIFY_LOGIN (
    p_username IN ACCOUNT.USERNAME%TYPE,
    p_password_hash IN ACCOUNT.PASSWORD_HASH%TYPE
) IS
    v_user_id ACCOUNT.USER_ID%TYPE;
    v_status ACCOUNT.STATUS%TYPE;
BEGIN
    SELECT USER_ID, STATUS
    INTO v_user_id, v_status
    FROM ACCOUNT
    WHERE USERNAME = p_username
      AND PASSWORD_HASH = p_password_hash
      AND IS_DELETED = 0;

    IF v_status != 'ACTIVE' THEN
        DBMS_OUTPUT.PUT_LINE('Login failed: Account is locked or inactive.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Login successful. User ID: ' || v_user_id);
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Invalid username or password.');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('System error: ' || SQLERRM);
END;


-- 4.DELETE CATEGORY --
CREATE OR REPLACE PROCEDURE SP_DELETE_CATEGORY_SOFT (
    p_category_id IN CATEGORY.CATEGORY_ID%TYPE
) IS
    v_cat_id CATEGORY.CATEGORY_ID%TYPE;
    v_is_deleted CATEGORY.IS_DELETED%TYPE;
BEGIN
    SELECT CATEGORY_ID, IS_DELETED
    INTO v_cat_id, v_is_deleted
    FROM CATEGORY
    WHERE CATEGORY_ID = p_category_id;

    IF v_is_deleted = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Category has already been deleted.');
    ELSE
        UPDATE CATEGORY
        SET IS_DELETED = 1,
            UPDATED_AT = SYSDATE
        WHERE CATEGORY_ID = p_category_id;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Category soft-deleted successfully.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Category not found.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error deleting category: ' || SQLERRM);
END;


-- 5.DELETE PRODUCT --
CREATE OR REPLACE PROCEDURE SP_DELETE_PRODUCT_SOFT (
    p_product_id IN PRODUCT.PRODUCT_ID%TYPE
) IS
    v_prod_id PRODUCT.PRODUCT_ID%TYPE;
    v_is_deleted PRODUCT.IS_DELETED%TYPE;
BEGIN
    SELECT PRODUCT_ID, IS_DELETED
    INTO v_prod_id, v_is_deleted
    FROM PRODUCT
    WHERE PRODUCT_ID = p_product_id;

    IF v_is_deleted = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Product has already been deleted.');
    ELSE
        UPDATE PRODUCT
        SET IS_DELETED = 1,
            STATUS = 0,
            UPDATED_AT = SYSDATE
        WHERE PRODUCT_ID = p_product_id;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Product soft-deleted successfully.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Product not found.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error deleting product: ' || SQLERRM);
END;


-- 6.DELETE CUSTOMER --
CREATE OR REPLACE PROCEDURE SP_DELETE_CUSTOMER_SOFT (
    p_customer_id IN CUSTOMER.CUSTOMER_ID%TYPE
) IS
    v_cust_id CUSTOMER.CUSTOMER_ID%TYPE;
    v_is_deleted CUSTOMER.IS_DELETED%TYPE;
BEGIN
    SELECT CUSTOMER_ID, IS_DELETED
    INTO v_cust_id, v_is_deleted
    FROM CUSTOMER
    WHERE CUSTOMER_ID = p_customer_id;

    IF v_is_deleted = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Customer has already been deleted.');
    ELSE
        UPDATE CUSTOMER
        SET IS_DELETED = 1,
            UPDATED_AT = SYSDATE
        WHERE CUSTOMER_ID = p_customer_id;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Customer soft-deleted successfully.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Customer not found.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error deleting customer: ' || SQLERRM);
END;


-- 7. ADD PRODUCT --
CREATE OR REPLACE PROCEDURE SP_ADD_PRODUCT (
    p_name IN PRODUCT.NAME%TYPE,
    p_price IN PRODUCT.PRICE%TYPE,
    p_brand IN PRODUCT.BRAND%TYPE,
    p_warranty IN PRODUCT.WARRANTY_MONTH%TYPE,
    p_category_id IN PRODUCT.CATEGORY_ID%TYPE
) IS
    v_cat_id CATEGORY.CATEGORY_ID%TYPE;
BEGIN
    SELECT CATEGORY_ID INTO v_cat_id FROM CATEGORY WHERE CATEGORY_ID = p_category_id;

    INSERT INTO PRODUCT (NAME, PRICE, BRAND, WARRANTY_MONTH, CATEGORY_ID)
    VALUES (p_name, p_price, p_brand, p_warranty, p_category_id);

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Product added successfully.');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Error: Provided Category ID does not exist.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('System error: ' || SQLERRM);
END;


-- 8. CANCEL ORDER & REFUND STOCK (USING EXPLICIT CURSOR) --
CREATE OR REPLACE PROCEDURE SP_PRINT_ORDER_INVOICE (
    p_order_id INCREATE OR REPLACE PROCEDURE SP_CANCEL_ORDER (
    p_order_id IN ORDERS.ORDER_ID%TYPE
) IS
    v_current_status ORDERS.STATUS%TYPE;
    v_product_id ORDER_DETAIL.PRODUCT_ID%TYPE;
    v_quantity ORDER_DETAIL.QUANTITY%TYPE;

    CURSOR c_order_items IS
        SELECT PRODUCT_ID, QUANTITY
        FROM ORDER_DETAIL
        WHERE ORDER_ID = p_order_id;
BEGIN
    SELECT STATUS INTO v_current_status
    FROM ORDERS
    WHERE ORDER_ID = p_order_id;

    IF v_current_status = 'DELIVERED' THEN
        DBMS_OUTPUT.PUT_LINE('Cannot cancel a delivered order.');
    ELSE
        UPDATE ORDERS SET STATUS = 'CANCELLED' WHERE ORDER_ID = p_order_id;

        OPEN c_order_items;
        LOOP
            FETCH c_order_items INTO v_product_id, v_quantity;
            EXIT WHEN c_order_items%NOTFOUND;

            UPDATE INVENTORY
            SET QUANTITY = QUANTITY + v_quantity,
                UPDATED_AT = SYSDATE
            WHERE PRODUCT_ID = v_product_id AND ROWNUM = 1;
        END LOOP;
        CLOSE c_order_items;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Order cancelled and stock refunded successfully.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Order not found.');
    WHEN OTHERS THEN
        ROLLBACK;
        IF c_order_items%ISOPEN THEN CLOSE c_order_items; END IF;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;


-- 9. PRINT ORDER INVOICE (USING IMPLICIT CURSOR & FUNCTION) --
 CREATE OR REPLACE PROCEDURE SP_PRINT_ORDER_INVOICE (
    p_order_id IN ORDERS.ORDER_ID%TYPE
) IS
    v_total_amount NUMBER;
    v_customer_id ORDERS.CUSTOMER_ID%TYPE;

    CURSOR c_invoice_items IS
        SELECT P.NAME, OD.QUANTITY, OD.PRICE, (OD.QUANTITY * OD.PRICE) AS SUBTOTAL
        FROM ORDER_DETAIL OD
        JOIN PRODUCT P ON OD.PRODUCT_ID = P.PRODUCT_ID
        WHERE OD.ORDER_ID = p_order_id;
BEGIN
    SELECT CUSTOMER_ID INTO v_customer_id FROM ORDERS WHERE ORDER_ID = p_order_id;

    v_total_amount := FN_CALCULATE_ORDER_TOTAL(p_order_id);

    DBMS_OUTPUT.PUT_LINE('=========================================');
    DBMS_OUTPUT.PUT_LINE('INVOICE FOR ORDER ID: ' || p_order_id);
    DBMS_OUTPUT.PUT_LINE('=========================================');
    DBMS_OUTPUT.PUT_LINE('PRODUCT NAME | QTY | PRICE | SUBTOTAL');
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------');

    FOR item IN c_invoice_items LOOP
        DBMS_OUTPUT.PUT_LINE(item.NAME || ' | ' || item.QUANTITY || ' | $' || item.PRICE || ' | $' || item.SUBTOTAL);
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('-----------------------------------------');
    DBMS_OUTPUT.PUT_LINE('GRAND TOTAL: $' || v_total_amount);
    DBMS_OUTPUT.PUT_LINE('=========================================');

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Invoice error: Order not found.');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error printing invoice: ' || SQLERRM);
END;


-- 10. CONFIRM PAYMENT --
CREATE OR REPLACE PROCEDURE SP_CONFIRM_PAYMENT (
    p_order_id IN PAYMENT.ORDER_ID%TYPE,
    p_trans_code IN PAYMENT.TRANSACTION_CODE%TYPE
) IS
    v_payment_id PAYMENT.PAYMENT_ID%TYPE;
BEGIN
    SELECT PAYMENT_ID INTO v_payment_id
    FROM PAYMENT
    WHERE ORDER_ID = p_order_id AND PAYMENT_STATUS = 'PENDING';

    IF v_payment_id IS NOT NULL THEN
        UPDATE PAYMENT
        SET PAYMENT_STATUS = 'SUCCESS',
            TRANSACTION_CODE = p_trans_code,
            PAID_AT = SYSDATE
        WHERE PAYMENT_ID = v_payment_id;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Payment confirmed successfully.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Payment does not exist or is already processed.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;


-- 11. ADD CUSTOMER --
CREATE OR REPLACE PROCEDURE SP_ADD_CUSTOMER (
    p_name IN CUSTOMER.NAME%TYPE,
    p_email IN CUSTOMER.EMAIL%TYPE,
    p_address IN CUSTOMER.ADDRESS%TYPE,
    p_phone IN CUSTOMER.PHONE_NUMBER%TYPE
) IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM CUSTOMER WHERE EMAIL = p_email;

    IF v_count > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Error: Email already exists in the system.');
    ELSE
        INSERT INTO CUSTOMER (NAME, EMAIL, ADDRESS, PHONE_NUMBER)
        VALUES (p_name, p_email, p_address, p_phone);

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Customer added successfully.');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('System error: ' || SQLERRM);
END;


-- 12. LOCK ACCOUNT --
CREATE OR REPLACE PROCEDURE SP_LOCK_ACCOUNT (
    p_account_id IN ACCOUNT.ACCOUNT_ID%TYPE
) IS
    v_status ACCOUNT.STATUS%TYPE;
BEGIN
    SELECT STATUS INTO v_status FROM ACCOUNT WHERE ACCOUNT_ID = p_account_id;

    IF v_status = 'LOCKED' THEN
        DBMS_OUTPUT.PUT_LINE('Account is already locked.');
    ELSE
        UPDATE ACCOUNT
        SET STATUS = 'LOCKED',
            UPDATED_AT = SYSDATE
        WHERE ACCOUNT_ID = p_account_id;

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Account locked successfully.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Account not found.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;


-- 13. ADD WAREHOUSE --
CREATE OR REPLACE PROCEDURE SP_ADD_WAREHOUSE (
    p_name IN WAREHOUSE.NAME%TYPE,
    p_address IN WAREHOUSE.ADDRESS%TYPE,
    p_phone IN WAREHOUSE.PHONE%TYPE
) IS
BEGIN
    INSERT INTO WAREHOUSE (NAME, ADDRESS, PHONE)
    VALUES (p_name, p_address, p_phone);

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Warehouse created successfully.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('System error: ' || SQLERRM);
END;


-- 14. IMPORT STOCK --
CREATE OR REPLACE PROCEDURE SP_IMPORT_STOCK (
    p_product_id IN INVENTORY.PRODUCT_ID%TYPE,
    p_warehouse_id IN INVENTORY.WAREHOUSE_ID%TYPE,
    p_qty_added IN INVENTORY.QUANTITY%TYPE
) IS
    v_current_qty INVENTORY.QUANTITY%TYPE;
BEGIN
    SELECT QUANTITY INTO v_current_qty
    FROM INVENTORY
    WHERE PRODUCT_ID = p_product_id AND WAREHOUSE_ID = p_warehouse_id;

    UPDATE INVENTORY
    SET QUANTITY = QUANTITY + p_qty_added,
        UPDATED_AT = SYSDATE
    WHERE PRODUCT_ID = p_product_id AND WAREHOUSE_ID = p_warehouse_id;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Stock imported successfully. New quantity: ' || (v_current_qty + p_qty_added));
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Product not configured in this warehouse. Please create inventory record first.');
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error importing stock: ' || SQLERRM);
END;


-- 15. CREATE COUPON --
CREATE OR REPLACE PROCEDURE SP_CREATE_COUPON (
    p_code IN COUPON.CODE%TYPE,
    p_discount_value IN COUPON.DISCOUNT_VALUE%TYPE,
    p_limit IN COUPON.USAGE_LIMIT%TYPE
) IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM COUPON WHERE CODE = p_code;

    IF v_count > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Error: Coupon code already exists.');
    ELSE
        INSERT INTO COUPON (CODE, DISCOUNT_TYPE, DISCOUNT_VALUE, START_AT, END_AT, USAGE_LIMIT)
        VALUES (p_code, 'AMOUNT', p_discount_value, SYSDATE, SYSDATE + 30, p_limit);

        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Coupon created successfully. Valid for 30 days.');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('System error: ' || SQLERRM);
END;


-- 16. CLOSE EXPIRED GROUP BUYS (USING IMPLICIT CURSOR) --
CREATE OR REPLACE PROCEDURE SP_CLOSE_EXPIRED_GROUP_BUYS
IS
    v_count NUMBER := 0;
BEGIN
    FOR rec IN (
        SELECT GROUP_ID
        FROM GROUP_BUY
        WHERE STATUS = 'WAITING'
          AND EXPIRY_TIME < SYSDATE
    ) LOOP
        UPDATE GROUP_BUY
        SET STATUS = 'EXPIRED',
            UPDATED_AT = SYSDATE
        WHERE GROUP_ID = rec.GROUP_ID;

        v_count := v_count + 1;
    END LOOP;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Successfully closed ' || v_count || ' expired group buy sessions.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('System error: ' || SQLERRM);
END;
