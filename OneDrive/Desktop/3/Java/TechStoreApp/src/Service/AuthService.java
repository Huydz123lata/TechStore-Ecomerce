/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DAO.AccountDAO;
import DAO.AuthDAO;
import DAO.RoleDAO;
import DAO.TokenDAO;
import Model.AccountModel;
import Model.Role;
import Model.User;
import Util.UserSession;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class AuthService {

    private final TokenDAO tokenDAO = new TokenDAO();
    private final AuthDAO authDAO = new AuthDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    //xử lý login
    public AccountModel executeLogin(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) {
            return null;
        }

        AccountModel acc = authDAO.checkLogin(user, pass);

        if (acc != null && "ACTIVE".equalsIgnoreCase(acc.getStatus())) {
            String token = java.util.UUID.randomUUID().toString().replace("-", "");
            tokenDAO.createToken(acc.getAccountId(), token);
            List<Role> perms = roleDAO.getPermissionsByAccountId(acc.getAccountId());
            UserSession.startSession(acc, token, perms);
            return acc;
        }
        return null;
    }

    //xử lý đăng ký
    public boolean excuteRegister(String fullname, String sdt, String gioiTinh,
            String ngay, String thang, String nam,
            String userName, String pass) throws Exception {

        User u = new User(fullname, sdt, gioiTinh);
        u.setNgaySinhFromPicker(ngay, thang, nam);

        AccountModel acc = new AccountModel(u, userName, pass);

        return authDAO.register(acc);
    }
}
