/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Business.Main;

import Business.Sql.AccountSql;
import Business.Sql.TokenSql;
import Common.Util.UserSession;
import Model.Account;
import Model.Permission;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class LoginProcess {

    private final TokenSql tokenSql = new TokenSql();
    private final AccountSql accountSql = new AccountSql();

    public Account executeLogin(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) {
            return null;
        }

        Account acc = accountSql.checkLogin(user, pass);

        if (acc != null && "ACTIVE".equalsIgnoreCase(acc.getStatus())) {
            String token = java.util.UUID.randomUUID().toString().replace("-", "");
            tokenSql.createToken(acc.getAccountId(), token);
            List<Permission> perms = accountSql.getPermissionsByAccountId(acc.getAccountId());
            UserSession.startSession(acc, token, perms);
            return acc;
        }
        return null;
    }

}
