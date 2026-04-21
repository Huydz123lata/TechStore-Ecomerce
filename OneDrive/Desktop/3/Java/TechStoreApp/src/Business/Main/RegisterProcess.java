/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Business.Main;

/**
 *
 * @author HUY0406
 */
import Business.Sql.AccountSql;
import Model.Account;
import Model.User;

public class RegisterProcess {

    // Thêm sdt, gioiTinh, ngay, thang, nam vào tham số
    public boolean execute(String fullname, String sdt, String gioiTinh,
            String ngay, String thang, String nam,
            String userName, String pass) {

        // 1. Tạo User với Constructor mới (4 tham số)
        User u = new User(fullname, sdt, gioiTinh);

        // 2. Ép ngày sinh bằng hàm mình vừa viết trong Model
        u.setNgaySinhFromPicker(ngay, thang, nam);

        // 3. Tạo Account
        Account acc = new Account(u, userName, pass);

        // 4. Gọi DB để lưu
        AccountSql sql = new AccountSql();
        return sql.register(acc);
    }
}
