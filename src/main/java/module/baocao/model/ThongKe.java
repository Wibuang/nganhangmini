package module.baocao.model;

/** Số liệu tổng quan cho quản trị viên. */
public class ThongKe {
    private final int tongKhachHang;
    private final int tongTaiKhoan;
    private final int taiKhoanHoatDong;
    private final long tongSoDu;
    private final int giaoDichHomNay;
    private final long napHomNay;
    private final long rutHomNay;
    private final long chuyenHomNay;
    private final long phiHomNay;

    public ThongKe(int tongKhachHang, int tongTaiKhoan, int taiKhoanHoatDong, long tongSoDu,
                   int giaoDichHomNay, long napHomNay, long rutHomNay, long chuyenHomNay, long phiHomNay) {
        this.tongKhachHang = tongKhachHang;
        this.tongTaiKhoan = tongTaiKhoan;
        this.taiKhoanHoatDong = taiKhoanHoatDong;
        this.tongSoDu = tongSoDu;
        this.giaoDichHomNay = giaoDichHomNay;
        this.napHomNay = napHomNay;
        this.rutHomNay = rutHomNay;
        this.chuyenHomNay = chuyenHomNay;
        this.phiHomNay = phiHomNay;
    }

    public int getTongKhachHang() { return tongKhachHang; }
    public int getTongTaiKhoan() { return tongTaiKhoan; }
    public int getTaiKhoanHoatDong() { return taiKhoanHoatDong; }
    public int getTaiKhoanKhongHoatDong() { return tongTaiKhoan - taiKhoanHoatDong; }
    public long getTongSoDu() { return tongSoDu; }
    public int getGiaoDichHomNay() { return giaoDichHomNay; }
    public long getNapHomNay() { return napHomNay; }
    public long getRutHomNay() { return rutHomNay; }
    public long getChuyenHomNay() { return chuyenHomNay; }
    public long getPhiHomNay() { return phiHomNay; }
}
