package module.taikhoan.model;

import java.time.LocalDateTime;

/** Một tài khoản ngân hàng của khách hàng. */
public class TaiKhoan {
    private int maTK;
    private String soTaiKhoan;
    private int maKH;
    private String tenGoiNho;
    private long soDu;                  // tiền VND không có số lẻ nên dùng long
    private String trangThai;           // xem TrangThaiTK
    private LocalDateTime ngayMo;
    private LocalDateTime ngayDong;
    private String tenChuTaiKhoan;      // chỉ để hiển thị; controller điền từ module Khách hàng

    public TaiKhoan() {}

    public TaiKhoan(int maTK, String soTaiKhoan, int maKH, String tenGoiNho, long soDu,
                    String trangThai, LocalDateTime ngayMo, LocalDateTime ngayDong) {
        this.maTK = maTK;
        this.soTaiKhoan = soTaiKhoan;
        this.maKH = maKH;
        this.tenGoiNho = tenGoiNho;
        this.soDu = soDu;
        this.trangThai = trangThai;
        this.ngayMo = ngayMo;
        this.ngayDong = ngayDong;
    }

    public boolean dangHoatDong() { return TrangThaiTK.HOAT_DONG.equals(trangThai); }

    /** Tên hiển thị trong danh sách chọn: "1000000001 - Tài khoản chính". */
    public String tenHienThi() {
        String ten = tenGoiNho == null || tenGoiNho.isEmpty() ? "Tài khoản thanh toán" : tenGoiNho;
        return soTaiKhoan + " - " + ten;
    }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getSoTaiKhoan() { return soTaiKhoan; }
    public void setSoTaiKhoan(String soTaiKhoan) { this.soTaiKhoan = soTaiKhoan; }
    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }
    public String getTenGoiNho() { return tenGoiNho; }
    public void setTenGoiNho(String tenGoiNho) { this.tenGoiNho = tenGoiNho; }
    public long getSoDu() { return soDu; }
    public void setSoDu(long soDu) { this.soDu = soDu; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayMo() { return ngayMo; }
    public void setNgayMo(LocalDateTime ngayMo) { this.ngayMo = ngayMo; }
    public LocalDateTime getNgayDong() { return ngayDong; }
    public void setNgayDong(LocalDateTime ngayDong) { this.ngayDong = ngayDong; }
    public String getTenChuTaiKhoan() { return tenChuTaiKhoan; }
    public void setTenChuTaiKhoan(String tenChuTaiKhoan) { this.tenChuTaiKhoan = tenChuTaiKhoan; }
}
