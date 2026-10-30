package module.baocao.model;

import java.util.ArrayList;

/**
 * Kết quả đối soát số dư với sổ cái. KHỚP khi:
 * (1) tổng số dư các tài khoản = tổng (tiền vào − tiền ra) trong sổ cái, và
 * (2) không có tài khoản nào lệch so với số dư sau của giao dịch cuối cùng.
 */
public class DoiSoat {
    private final int soTaiKhoan;
    private final long tongSoDuTaiKhoan;
    private final long tongSoCai;
    private final ArrayList<ChenhLech> taiKhoanLech;

    public DoiSoat(int soTaiKhoan, long tongSoDuTaiKhoan, long tongSoCai,
                   ArrayList<ChenhLech> taiKhoanLech) {
        this.soTaiKhoan = soTaiKhoan;
        this.tongSoDuTaiKhoan = tongSoDuTaiKhoan;
        this.tongSoCai = tongSoCai;
        this.taiKhoanLech = taiKhoanLech;
    }

    public boolean khop() {
        return tongSoDuTaiKhoan == tongSoCai && taiKhoanLech.isEmpty();
    }

    public int getSoTaiKhoan() { return soTaiKhoan; }
    public long getTongSoDuTaiKhoan() { return tongSoDuTaiKhoan; }
    public long getTongSoCai() { return tongSoCai; }
    public ArrayList<ChenhLech> getTaiKhoanLech() { return taiKhoanLech; }
}
