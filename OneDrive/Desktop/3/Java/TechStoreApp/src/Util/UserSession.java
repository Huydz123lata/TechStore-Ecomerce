package Util;

import Model.AccountModel;
import Model.RoleModel;
import java.util.List;

/**
 * @author HUY0406
 */
public class UserSession {

    private static AccountModel currentAccount;
    private static String currentTokenValue;
    private static List<RoleModel> currentPermissions;

    public static void startSession(AccountModel account, String tokenValue, List<RoleModel> perm) {
        currentAccount = account;
        currentTokenValue = tokenValue;
        currentPermissions = perm;
    }

    public static void clearSession() {
        currentAccount = null;
        currentTokenValue = null;
        currentPermissions = null;
    }

    public static String getCurrentTokenValue() {
        return currentTokenValue;
    }

    public static int getCurrentAccountId() {
        return currentAccount.getAccountId();
    }

    public static int canDo(String functionName, String action) {
        if (currentPermissions == null) {
            return 0;
        }
        for (RoleModel p : currentPermissions) {
            if (p.getFunctionName().equals(functionName)) {
                switch (action) {
                    case "ADD":
                        return p.getCanAdd();
                    case "EDIT":
                        return p.getCanEdit();
                    case "DELETE":
                        return p.getCanDelete();
                    case "VIEW":
                        return p.getCanView();
                    case "DOWNLOAD":
                        return p.getCanDownload();
                }
            }
        }
        return 0;
    }
}
