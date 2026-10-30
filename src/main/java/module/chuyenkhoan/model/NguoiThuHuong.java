package module.chuyenkhoan.model;

/** Một người nhận được lưu trong danh bạ của khách hàng. */
public class NguoiThuHuong {
    private final int maNTH;
    private final String soTaiKhoan;
    private final String tenGoiNho;
    private final String tenNguoiNhan;      // tên chủ tài khoản tại thời điểm lưu

    public NguoiThuHuong(int maNTH, String soTaiKhoan, String tenGoiNho, String tenNguoiNhan) {
        this.maNTH = maNTH;
        this.soTaiKhoan = soTaiKhoan;
        this.tenGoiNho = tenGoiNho;
        this.tenNguoiNhan = tenNguoiNhan;
    }

    public int getMaNTH() { return maNTH; }
    public String getSoTaiKhoan() { return soTaiKhoan; }
    public String getTenGoiNho() { return tenGoiNho; }
    public String getTenNguoiNhan() { return tenNguoiNhan; }

    @Override
    public String toString() {
        return tenGoiNho + " - " + soTaiKhoan;
    }
}
