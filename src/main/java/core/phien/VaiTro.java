/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.phien;

/** Các vai trò trong hệ thống. */
public final class VaiTro {
    public static final String KHACH         = "KHACH";
    public static final String GIAODICHVIEN  = "GIAODICHVIEN";
    public static final String ADMIN         = "ADMIN";
    /** Phiên nội bộ của hệ thống (ví dụ lúc đăng nhập), không dành cho người dùng. */
    public static final String HE_THONG      = "HE_THONG";

    private VaiTro() {}

    public static String ten(String ma) {
        if (KHACH.equals(ma)) return "Khách hàng";
        if (GIAODICHVIEN.equals(ma)) return "Giao dịch viên";
        if (ADMIN.equals(ma)) return "Quản trị viên";
        return ma == null ? "" : ma;
    }
}

