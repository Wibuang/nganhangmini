package module.giaodich.model;

import java.time.LocalDateTime;

/** Một dòng trong sổ cái. Đã ghi thì không sửa, không xóa. */
public class GiaoDich {
    private final int maGD;
    private final String maThamChieu;       // ví dụ GD00000012, dùng để tra cứu và in biên lai
    private final int maTK;
    private final String soTaiKhoan;
    private final String loaiGD;            // xem LoaiGD
    private final long soTien;
    private final long soDuSau;
    private final LocalDateTime ngayGD;
    private final String kenh;              // xem KenhGD
    private final String ghiChu;
    private final String soTKDoiTac;        // tài khoản bên kia (chỉ có khi chuyển khoản)
    private final Integer maGDGoc;          // giao dịch gốc nếu đây là giao dịch đảo
    private final Integer maND;             // người thực hiện

    public GiaoDich(int maGD, String maThamChieu, int maTK, String soTaiKhoan, String loaiGD,
                    long soTien, long soDuSau, LocalDateTime ngayGD, String kenh, String ghiChu,
                    String soTKDoiTac, Integer maGDGoc, Integer maND) {
        this.maGD = maGD;
        this.maThamChieu = maThamChieu;
        this.maTK = maTK;
        this.soTaiKhoan = soTaiKhoan;
        this.loaiGD = loaiGD;
        this.soTien = soTien;
        this.soDuSau = soDuSau;
        this.ngayGD = ngayGD;
        this.kenh = kenh;
        this.ghiChu = ghiChu;
        this.soTKDoiTac = soTKDoiTac;
        this.maGDGoc = maGDGoc;
        this.maND = maND;
    }

    public int getMaGD() { return maGD; }
    public String getMaThamChieu() { return maThamChieu; }
    public int getMaTK() { return maTK; }
    public String getSoTaiKhoan() { return soTaiKhoan; }
    public String getLoaiGD() { return loaiGD; }
    public long getSoTien() { return soTien; }
    public long getSoDuSau() { return soDuSau; }
    public LocalDateTime getNgayGD() { return ngayGD; }
    public String getKenh() { return kenh; }
    public String getGhiChu() { return ghiChu; }
    public String getSoTKDoiTac() { return soTKDoiTac; }
    public Integer getMaGDGoc() { return maGDGoc; }
    public Integer getMaND() { return maND; }
}
