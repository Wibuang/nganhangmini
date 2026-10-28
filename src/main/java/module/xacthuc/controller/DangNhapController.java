package module.xacthuc.controller;

import core.phien.PhienDangNhap;
import core.util.MatKhauUtil;
import module.khachhang.controller.KhachHangController;
import module.khachhang.model.KhachHang;
import module.xacthuc.dao.NguoiDungDAO;
import module.xacthuc.model.NguoiDung;

/**
 * Đăng nhập: kiểm tra mật khẩu đã băm, đếm số lần sai và khóa khi sai quá nhiều.
 * (Đợt 2: số lần sai tối đa sẽ lấy từ module Cấu hình thay vì hằng số.)
 */
public class DangNhapController {
    public static final int TOI_DA_SAI = 5;

    private final NguoiDungDAO dao = new NguoiDungDAO();

    /** Đúng thì trả về PhienDangNhap; sai thì ném IllegalArgumentException kèm thông báo. */
    public PhienDangNhap dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap.trim().isEmpty() || matKhau.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        NguoiDung nd = dao.timTheoTenDangNhap(tenDangNhap.trim());
        if (nd == null) {
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu.");
        }
        if (!nd.isTrangThai()) {
            throw new IllegalArgumentException(
                    "Tài khoản đăng nhập đã bị khóa. Vui lòng liên hệ ngân hàng.");
        }

        String matKhauDaBam = MatKhauUtil.bam(nd.getTenDangNhap(), matKhau);
        if (!dao.kiemTraMatKhau(nd.getMaND(), matKhauDaBam)) {
            throw xuLySaiMatKhau(nd);
        }
        if (nd.getSoLanSai() > 0) {
            dao.resetSoLanSai(nd.getMaND());             // đăng nhập đúng → xóa bộ đếm sai
        }
        return taoPhien(nd);
    }

    /** Đếm số lần sai, khóa nếu cần, và trả về lỗi để hiển thị. */
    private IllegalArgumentException xuLySaiMatKhau(NguoiDung nd) {
        if ("ADMIN".equals(nd.getVaiTro())) {
            // Không khóa admin theo số lần sai, nếu không sẽ không còn ai mở khóa được
            return new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu.");
        }
        int soLanSai = dao.tangSoLanSai(nd.getMaND());
        if (soLanSai >= TOI_DA_SAI) {
            dao.doiTrangThai(nd.getMaND(), false);
            return new IllegalArgumentException("Bạn đã nhập sai " + TOI_DA_SAI
                    + " lần. Tài khoản đăng nhập đã bị khóa, vui lòng liên hệ ngân hàng.");
        }
        return new IllegalArgumentException("Sai mật khẩu. Bạn còn "
                + (TOI_DA_SAI - soLanSai) + " lần thử.");
    }

    /** Ghép thông tin người dùng (và hồ sơ khách hàng nếu là khách) thành phiên đăng nhập. */
    private PhienDangNhap taoPhien(NguoiDung nd) {
        if (nd.getMaKH() == null) {                      // nhân viên
            return new PhienDangNhap(nd.getMaND(), nd.getTenDangNhap(), nd.getVaiTro(),
                    nd.getHoTen(), null);
        }
        // Khách hàng: lấy tên từ module Khách hàng (chỉ gọi qua controller)
        KhachHang kh = new KhachHangController(PhienDangNhap.heThong()).layTheoMa(nd.getMaKH());
        if (kh == null) {
            throw new IllegalArgumentException("Không tìm thấy hồ sơ khách hàng, vui lòng liên hệ ngân hàng.");
        }
        if (!kh.isHoatDong()) {
            throw new IllegalArgumentException("Hồ sơ khách hàng đã ngừng hoạt động, vui lòng liên hệ ngân hàng.");
        }
        return new PhienDangNhap(nd.getMaND(), nd.getTenDangNhap(), nd.getVaiTro(),
                kh.getHoTen(), kh.getMaKH());
    }
}
