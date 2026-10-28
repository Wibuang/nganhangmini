/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.phien;


/**
 * Cho biết ai đang đăng nhập và vai trò gì. Mọi Controller nhận đối tượng này để kiểm tra quyền.
 * Chỉ chứa dữ liệu đơn giản (không phụ thuộc module nào) để lớp core độc lập.
 */
public class PhienDangNhap {
    private final int maND;
    private final String tenDangNhap;
    private final String vaiTro;
    private final String tenHienThi;
    private final Integer maKH;         // chỉ có với khách hàng

    public PhienDangNhap(int maND, String tenDangNhap, String vaiTro, String tenHienThi, Integer maKH) {
        this.maND = maND;
        this.tenDangNhap = tenDangNhap;
        this.vaiTro = vaiTro;
        this.tenHienThi = tenHienThi;
        this.maKH = maKH;
    }

    /** Phiên nội bộ, dùng khi hệ thống cần đọc dữ liệu trước lúc có người đăng nhập. */
    public static PhienDangNhap heThong() {
        return new PhienDangNhap(0, "he_thong", VaiTro.HE_THONG, "Hệ thống", null);
    }

    public int getMaND() { return maND; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getVaiTro() { return vaiTro; }
    public String getTenHienThi() { return tenHienThi; }
    public Integer getMaKH() { return maKH; }

    public boolean laKhach() { return VaiTro.KHACH.equals(vaiTro); }
    public boolean laGiaoDichVien() { return VaiTro.GIAODICHVIEN.equals(vaiTro); }
    public boolean laAdmin() { return VaiTro.ADMIN.equals(vaiTro); }
    public boolean laNhanVien() { return laGiaoDichVien() || laAdmin(); }
    public boolean laHeThong() { return VaiTro.HE_THONG.equals(vaiTro); }
}
