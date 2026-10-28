package module.xacthuc.model;

import java.time.LocalDateTime;

/** Một tài khoản đăng nhập (không chứa mật khẩu). Khác với tài khoản ngân hàng. */
public class NguoiDung {
    private int maND;
    private String tenDangNhap;
    private String hoTen;           // chỉ nhân viên; khách hàng lấy tên từ hồ sơ khách hàng
    private String vaiTro;          // xem core.phien.VaiTro
    private boolean trangThai;      // true: hoạt động, false: bị khóa
    private int soLanSai;
    private Integer maKH;           // chỉ có với vai trò KHACH
    private LocalDateTime ngayTao;

    public NguoiDung() {}

    public NguoiDung(int maND, String tenDangNhap, String hoTen, String vaiTro, boolean trangThai,
                     int soLanSai, Integer maKH, LocalDateTime ngayTao) {
        this.maND = maND;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.soLanSai = soLanSai;
        this.maKH = maKH;
        this.ngayTao = ngayTao;
    }

    public int getMaND() { return maND; }
    public void setMaND(int maND) { this.maND = maND; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public int getSoLanSai() { return soLanSai; }
    public void setSoLanSai(int soLanSai) { this.soLanSai = soLanSai; }
    public Integer getMaKH() { return maKH; }
    public void setMaKH(Integer maKH) { this.maKH = maKH; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
