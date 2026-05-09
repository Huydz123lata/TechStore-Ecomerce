/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

/**
 *
 * @author HUY0406
 */
public class StringUtils {

    public static String getAutoRoleName(String functionName, boolean a, boolean d, boolean e, boolean dl, boolean v) {
        if (!v && !a && !e && !d && !dl) {
            return "";
        }
        if (v && a && e && d && dl) {
            return "Toàn quyền " + functionName;
        }

        java.util.StringJoiner sj = new java.util.StringJoiner(", ");
        if (v) {
            sj.add("Xem");
        }
        if (a) {
            sj.add("Thêm");
        }
        if (e) {
            sj.add("Sửa");
        }
        if (d) {
            sj.add("Xóa");
        }
        if (dl) {
            sj.add("Tải xuống");
        }

        return functionName + " (" + sj.toString() + ")";
    }
}
