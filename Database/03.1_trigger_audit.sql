-- Oracle SQL: Trigger tự động CREATED_AT / UPDATED_AT
-- Áp dụng cho các bảng có cột CREATED_AT và UPDATED_AT

--------------------------------------------------
-- 1. USER
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_USER_AUDIT
BEFORE INSERT OR UPDATE ON "USER"
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        :NEW.CREATED_AT := SYSDATE;
        :NEW.UPDATED_AT := SYSDATE;
    ELSIF UPDATING THEN
        :NEW.UPDATED_AT := SYSDATE;
    END IF;
END;
/

--------------------------------------------------
-- 2. ACCOUNT
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_ACCOUNT_AUDIT
BEFORE INSERT OR UPDATE ON ACCOUNT
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        :NEW.CREATED_AT := SYSDATE;
        :NEW.UPDATED_AT := SYSDATE;
    ELSIF UPDATING THEN
        :NEW.UPDATED_AT := SYSDATE;
    END IF;
END;
/

--------------------------------------------------
-- 3. PRODUCT
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PRODUCT_AUDIT
BEFORE INSERT OR UPDATE ON PRODUCT
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        :NEW.CREATED_AT := SYSDATE;
        :NEW.UPDATED_AT := SYSDATE;
    ELSIF UPDATING THEN
        :NEW.UPDATED_AT := SYSDATE;
    END IF;
END;
/

--------------------------------------------------
-- 4. ORDERS
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_ORDERS_AUDIT
BEFORE INSERT OR UPDATE ON ORDERS
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        :NEW.CREATED_AT := SYSDATE;
        :NEW.UPDATED_AT := SYSDATE;
    ELSIF UPDATING THEN
        :NEW.UPDATED_AT := SYSDATE;
    END IF;
END;
/

--------------------------------------------------
-- 5. INVENTORY (chỉ UPDATED_AT)
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_INVENTORY_UPDATED
BEFORE UPDATE ON INVENTORY
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 6. LOYALTY_POINT (chỉ UPDATED_AT)
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_LOYALTY_POINT_UPDATED
BEFORE UPDATE ON LOYALTY_POINT
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 7. CATEGORY
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_CATEGORY_CREATED
BEFORE INSERT ON CATEGORY
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 8. WAREHOUSE
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_WAREHOUSE_CREATED
BEFORE INSERT ON WAREHOUSE
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 9. PAYMENT
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PAYMENT_CREATED
BEFORE INSERT ON PAYMENT
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 10. REVIEW
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_REVIEW_CREATED
BEFORE INSERT ON REVIEW
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 11. CART
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_CART_CREATED
BEFORE INSERT ON CART
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 12. COUPON
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_COUPON_CREATED
BEFORE INSERT ON COUPON
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 13. PROMOTION
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_PROMOTION_CREATED
BEFORE INSERT ON PROMOTION
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 14. ROLE_GROUP
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_ROLE_GROUP_CREATED
BEFORE INSERT ON ROLE_GROUP
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 15. ROLE
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_ROLE_CREATED
BEFORE INSERT ON ROLE
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/

--------------------------------------------------
-- 16. FUNCTION
--------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_FUNCTION_CREATED
BEFORE INSERT ON FUNCTION
FOR EACH ROW
BEGIN
    :NEW.CREATED_AT := SYSDATE;
END;
/
