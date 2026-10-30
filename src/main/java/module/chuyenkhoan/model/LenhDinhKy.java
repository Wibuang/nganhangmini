package module.chuyenkhoan.model;

import java.time.LocalDate;

/** Lệnh chuyển tiền định kỳ: mỗi chu kỳ tự chuyển một khoản cố định cho một người nhận. */
public class LenhDinhKy {
    private final int maLenh;
    private final int maKH;
    private final int maTKNguon;
    private final String soTKNguon;
    private final String soTKNhan;
    private final String tenNguoiNhan;
    private final long soTien;
    private final String noiDung;
    private final String tanSuat;               // xem TanSuat
    private final LocalDate ngayBatDau;
    private final LocalDate ngayKetThuc;        // null = không kết thúc
    private final LocalDate ngayChayKeTiep;
    private final String trangThai;             // xem TrangThaiLenh
    private final int soLanChay;
    private final String ketQuaLanCuoi;

    public LenhDinhKy(int maLenh, int maKH, int maTKNguon, String soTKNguon, String soTKNhan,
                      String tenNguoiNhan, long soTien, String noiDung, String tanSuat,
                      LocalDate ngayBatDau, LocalDate ngayKetThuc, LocalDate ngayChayKeTiep,
                      String trangThai, int soLanChay, String ketQuaLanCuoi) {
        this.maLenh = maLenh;
        this.maKH = maKH;
        this.maTKNguon = maTKNguon;
        this.soTKNguon = soTKNguon;
        this.soTKNhan = soTKNhan;
        this.tenNguoiNhan = tenNguoiNhan;
        this.soTien = soTien;
        this.noiDung = noiDung;
        this.tanSuat = tanSuat;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.ngayChayKeTiep = ngayChayKeTiep;
        this.trangThai = trangThai;
        this.soLanChay = soLanChay;
        this.ketQuaLanCuoi = ketQuaLanCuoi;
    }

    public int getMaLenh() { return maLenh; }
    public int getMaKH() { return maKH; }
    public int getMaTKNguon() { return maTKNguon; }
    public String getSoTKNguon() { return soTKNguon; }
    public String getSoTKNhan() { return soTKNhan; }
    public String getTenNguoiNhan() { return tenNguoiNhan; }
    public long getSoTien() { return soTien; }
    public String getNoiDung() { return noiDung; }
    public String getTanSuat() { return tanSuat; }
    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public LocalDate getNgayChayKeTiep() { return ngayChayKeTiep; }
    public String getTrangThai() { return trangThai; }
    public int getSoLanChay() { return soLanChay; }
    public String getKetQuaLanCuoi() { return ketQuaLanCuoi; }
}
