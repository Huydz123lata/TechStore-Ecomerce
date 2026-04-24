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
    private static String currentTokenValue;

    // 1. Lưu thông tin (Gọi lúc Login thành công)
    public static void startSession(Account account, String tokenValue) {
        currentAccount = account;
        currentTokenValue = tokenValue;
    }

    // 2. Xóa thông tin (Gọi lúc Đăng xuất / Bị Kick)
    public static void clearSession() {
        currentAccount = null;
        currentTokenValue = null;
    }

    public static String getCurrentTokenValue() {
        return currentTokenValue;
    }

    public static int getCurrentAccountId() {
        return currentAccount.getAccountId();
    }
}
