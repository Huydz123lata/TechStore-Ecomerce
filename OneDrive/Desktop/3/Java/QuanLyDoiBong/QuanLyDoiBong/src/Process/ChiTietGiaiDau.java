/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Process;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

/**
 *
 * @author nguyenminhnhut
 */
public class ChiTietGiaiDau {
    /**
     * Hàm thêm chi tiết Giải đấu
     * 
     * @param maGiai
     * @param maDoi
     * @return 
     */
    public int themChiTietGiaiDau(String maGiai, String maDoi)
    {
        int countResult = 0;
        
        try (Connection con = ConnectionUtils.getMyConnection()) {
         String query = "INSERT INTO CT_GD VALUES(?,?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, maGiai);
            ps.setString(2, maDoi);
            countResult = ps.executeUpdate();   
        }
        catch(Exception e){
            System.out.println(e);
            return countResult;
        }
        
        return countResult;
    }
}
