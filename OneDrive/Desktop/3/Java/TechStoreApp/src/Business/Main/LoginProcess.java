/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Business.Main;

import Common.DB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import Business.Sql.AccountSql;
import Common.Util.UserSession;
import Model.Account;

/**
 *
 * @author HUY0406
 */
public class LoginProcess {

    private AccountSql accountSql = new AccountSql();

    public Account executeLogin(String user, String pass) {
        if (user.trim().isEmpty() || pass.trim().isEmpty()) {
            return null;
        }

        Account acc = accountSql.checkLogin(user, pass);

        if (acc != null && "ACTIVE".equalsIgnoreCase(acc.getStatus())) {
            String token = java.util.UUID.randomUUID().toString().replace("-", "");
            AccountSql.createToken(acc.getAccountId(), token);
            UserSession.init(acc, token);
            return acc;
        }
        return null;
    }

}
