package module.baocao.model;

/** Một tài khoản có số dư khác với số dư sau của giao dịch cuối cùng trong sổ cái. */
public class ChenhLech {
    private final String soTaiKhoan;
    private final long soDuTaiKhoan;
    private final long soDuSoCai;

    public ChenhLech(String soTaiKhoan, long soDuTaiKhoan, long soDuSoCai) {
        this.soTaiKhoan = soTaiKhoan;
        this.soDuTaiKhoan = soDuTaiKhoan;
        this.soDuSoCai = soDuSoCai;
    }

    public String getSoTaiKhoan() { return soTaiKhoan; }
    public long getSoDuTaiKhoan() { return soDuTaiKhoan; }
    public long getSoDuSoCai() { return soDuSoCai; }
    public long getChenhLech() { return soDuTaiKhoan - soDuSoCai; }
}
