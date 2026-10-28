package module.giaodich.model;

import java.time.LocalDate;
import java.util.ArrayList;

/** Sao kê một tài khoản trong một kỳ: số dư đầu kỳ + tiền vào − tiền ra = số dư cuối kỳ. */
public class SaoKe {
    private final String soTaiKhoan;
    private final String tenChuTaiKhoan;
    private final LocalDate tuNgay;
    private final LocalDate denNgay;
    private final long soDuDau;
    private final long tongVao;
    private final long tongRa;
    private final long soDuCuoi;
    private final ArrayList<GiaoDich> danhSach;     // sắp theo thời gian tăng dần

    public SaoKe(String soTaiKhoan, String tenChuTaiKhoan, LocalDate tuNgay, LocalDate denNgay,
                 long soDuDau, long tongVao, long tongRa, long soDuCuoi, ArrayList<GiaoDich> danhSach) {
        this.soTaiKhoan = soTaiKhoan;
        this.tenChuTaiKhoan = tenChuTaiKhoan;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.soDuDau = soDuDau;
        this.tongVao = tongVao;
        this.tongRa = tongRa;
        this.soDuCuoi = soDuCuoi;
        this.danhSach = danhSach;
    }

    public String getSoTaiKhoan() { return soTaiKhoan; }
    public String getTenChuTaiKhoan() { return tenChuTaiKhoan; }
    public LocalDate getTuNgay() { return tuNgay; }
    public LocalDate getDenNgay() { return denNgay; }
    public long getSoDuDau() { return soDuDau; }
    public long getTongVao() { return tongVao; }
    public long getTongRa() { return tongRa; }
    public long getSoDuCuoi() { return soDuCuoi; }
    public ArrayList<GiaoDich> getDanhSach() { return danhSach; }
}
