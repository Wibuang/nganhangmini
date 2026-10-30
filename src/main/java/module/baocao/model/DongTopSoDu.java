package module.baocao.model;

/** Một dòng trong bảng "top tài khoản có số dư cao nhất". */
public class DongTopSoDu {
    private final String soTaiKhoan;
    private final String tenChuTaiKhoan;
    private final long soDu;

    public DongTopSoDu(String soTaiKhoan, String tenChuTaiKhoan, long soDu) {
        this.soTaiKhoan = soTaiKhoan;
        this.tenChuTaiKhoan = tenChuTaiKhoan;
        this.soDu = soDu;
    }

    public String getSoTaiKhoan() { return soTaiKhoan; }
    public String getTenChuTaiKhoan() { return tenChuTaiKhoan; }
    public long getSoDu() { return soDu; }
}
