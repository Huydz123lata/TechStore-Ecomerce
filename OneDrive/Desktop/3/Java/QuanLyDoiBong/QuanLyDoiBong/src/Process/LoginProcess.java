/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Process;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 *
 * @author user
 */
public class LoginProcess {

    /**
     *
     * @param username
     * @param password
     * @return
     */
    public boolean loginProcess(String username, String password) {
        boolean isSucess = false;
        try (Connection con = ConnectionUtils.getMyConnection()) {

            String SQL = "SELECT * "
                    + " FROM ACCOUNT "
                    + " WHERE USERNAME = ? "
                    + " AND PASSWORD_HASH = ? ";
            PreparedStatement ps = con.prepareStatement(SQL);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            System.out.println(rs);

            if (rs.next()) {
                isSucess = true;
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        System.out.print(isSucess);
        return isSucess;
    }

}
