/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.FunctionModel;
import config.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HUY0406
 */
public class FunctionDAO {

    public List<FunctionModel> getAllFunctionName() {
        List<FunctionModel> name = new ArrayList<>();
        String sql = "SELECT FUNCTION_ID, NAME_FUNCTION FROM FUNCTION";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FunctionModel f = new FunctionModel(
                        rs.getInt("FUNCTION_ID"),
                        rs.getString("NAME_FUNCTION")
                );
                name.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

}
