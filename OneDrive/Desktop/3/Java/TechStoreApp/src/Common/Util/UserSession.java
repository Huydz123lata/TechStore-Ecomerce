/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common.Util;

/**
 *
 * @author HUY0406
 */
import Model.Account;

public class UserSession {

    private static Account currentAccount;
    private static String sessionToken;

    public static void init(Account acc, String token) {
        currentAccount = acc;
        sessionToken = token;
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static void clear() {
        currentAccount = null;
        sessionToken = null;
    }
}
