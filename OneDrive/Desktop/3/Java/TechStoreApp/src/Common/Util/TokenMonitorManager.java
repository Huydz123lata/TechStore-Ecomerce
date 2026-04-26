/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common.Util;

/**
 *
 * @author HUY0406
 */
import Business.Sql.TokenSql;
import View.LoginForm;
import java.awt.*;
import javax.swing.Timer;
import javax.swing.JOptionPane;

public class TokenMonitorManager {

    private static Timer timer;
    private static final TokenSql tokenSql = new TokenSql();

    public static void start() {
        if (timer != null && timer.isRunning()) {
            return;
        }
        timer = new Timer(5000, e -> {
            String token = UserSession.getCurrentTokenValue();
            if (token == null || "Y".equals(tokenSql.checkStatus(token))) {

                if (timer != null) {
                    timer.stop();
                }
                handleKick();
            }
        });
        timer.start();
    }

    public static void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private static void handleKick() {
        UserSession.clearSession();
        JOptionPane.showMessageDialog(null, "Phiên đăng nhập đã hết hạn!");
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
        new LoginForm().setVisible(true);

    }
}
