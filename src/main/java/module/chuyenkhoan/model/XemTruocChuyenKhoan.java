package module.chuyenkhoan.model;

import module.taikhoan.model.TaiKhoan;

/** Thông tin hiển thị cho khách xác nhận trước khi chuyển: người nhận, số tiền, phí, có cần OTP không. */
public class XemTruocChuyenKhoan {
    private final TaiKhoan nguon;
    private final TaiKhoan nhan;            // có tên chủ tài khoản
    private final long soTien;
    private final long phi;
    private final boolean canOtp;

    public XemTruocChuyenKhoan(TaiKhoan nguon, TaiKhoan nhan, long soTien, long phi, boolean canOtp) {
        this.nguon = nguon;
        this.nhan = nhan;
        this.soTien = soTien;
        this.phi = phi;
        this.canOtp = canOtp;
    }

    public TaiKhoan getNguon() { return nguon; }
    public TaiKhoan getNhan() { return nhan; }
    public long getSoTien() { return soTien; }
    public long getPhi() { return phi; }
    public long getTongTru() { return soTien + phi; }
    public boolean isCanOtp() { return canOtp; }
}
