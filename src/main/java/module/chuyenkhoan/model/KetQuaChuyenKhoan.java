package module.chuyenkhoan.model;

import module.giaodich.model.GiaoDich;

/** Biên lai một lần chuyển khoản thành công. */
public class KetQuaChuyenKhoan {
    private final GiaoDich giaoDich;        // dòng CHUYEN_DI của bên gửi
    private final long phi;
    private final String tenNguoiNhan;
    private final String soTKNhan;

    public KetQuaChuyenKhoan(GiaoDich giaoDich, long phi, String tenNguoiNhan, String soTKNhan) {
        this.giaoDich = giaoDich;
        this.phi = phi;
        this.tenNguoiNhan = tenNguoiNhan;
        this.soTKNhan = soTKNhan;
    }

    public GiaoDich getGiaoDich() { return giaoDich; }
    public long getPhi() { return phi; }
    public String getTenNguoiNhan() { return tenNguoiNhan; }
    public String getSoTKNhan() { return soTKNhan; }
    /** Số dư của tài khoản gửi sau khi đã trừ cả phí. */
    public long getSoDuSau() { return giaoDich.getSoDuSau() - phi; }
}
