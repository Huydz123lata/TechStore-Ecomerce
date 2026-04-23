/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Process;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author HUY0406
 */
public class DangKyProcess {

    public int DangKyProcess(String username, String password) {
        int i = 0;
        // TODO add your handling code here:
        try (Connection con = ConnectionUtils.getMyConnection()) {

//            String query = "INSERT INTO "
//                    + "DOIBONG(MAD,TENDOI,QUOCGIA)"
//                    +" VALUES('"
//                    +maDoi+"','"+tenDoi+"','"+quocGia+"')";
            String query = "INSERT INTO ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, STATUS) VALUES(1,?,?,'ACTIVE')";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            i = ps.executeUpdate();
//            Statement stat = con.createStatement();
//            i = stat.executeUpdate(query);

        } catch (Exception e) {
            System.out.println(e);
        }
        return i;
    }
}
