package module.khachhang.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Hồ sơ khách hàng. */
public class KhachHang {
    private int maKH;
    private String hoTen;
    private LocalDate ngaySinh;
    private String cccd;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String trangThaiKYC;     // xem TrangThaiKYC
    private String ghiChuKYC;        // lý do nếu bị từ chối
    private boolean hoatDong;
    private LocalDateTime ngayTao;

    public KhachHang() {}

    public KhachHang(int maKH, String hoTen, LocalDate ngaySinh, String cccd, String soDienThoai,
                     String email, String diaChi, String trangThaiKYC, String ghiChuKYC,
                     boolean hoatDong, LocalDateTime ngayTao) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.cccd = cccd;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.trangThaiKYC = trangThaiKYC;
        this.ghiChuKYC = ghiChuKYC;
        this.hoatDong = hoatDong;
        this.ngayTao = ngayTao;
    }

    public boolean daXacMinh() { return TrangThaiKYC.DA_XAC_MINH.equals(trangThaiKYC); }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getTrangThaiKYC() { return trangThaiKYC; }
    public void setTrangThaiKYC(String trangThaiKYC) { this.trangThaiKYC = trangThaiKYC; }
    public String getGhiChuKYC() { return ghiChuKYC; }
    public void setGhiChuKYC(String ghiChuKYC) { this.ghiChuKYC = ghiChuKYC; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
