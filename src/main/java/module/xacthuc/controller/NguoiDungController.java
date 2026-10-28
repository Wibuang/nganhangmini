package module.xacthuc.controller;

import core.phien.PhienDangNhap;
import core.phien.VaiTro;
import core.util.MatKhauUtil;
import java.util.ArrayList;
import module.khachhang.controller.KhachHangController;
import module.khachhang.model.KhachHang;
import module.xacthuc.dao.NguoiDungDAO;
import module.xacthuc.model.NguoiDung;

/**
 * Chức năng của người đã đăng nhập: đổi mật khẩu của mình; nhân viên cấp đăng nhập cho khách;
 * quản trị viên quản lý người dùng (tạo nhân viên, khóa/mở khóa, đặt lại mật khẩu).
 */
public class NguoiDungController {
    private static final int DO_DAI_MAT_KHAU = 6;

    private final PhienDangNhap phien;
    private final NguoiDungDAO dao = new NguoiDungDAO();

    public NguoiDungController(PhienDangNhap phien) {
        this.phien = phien;
    }

    // ------------------------------------------------------------ mọi người dùng
    public void doiMatKhau(String cu, String moi, String xacNhan) {
        if (phien.laHeThong()) {
            throw new IllegalStateException("Bạn chưa đăng nhập.");
        }
        if (cu.isEmpty() || moi.isEmpty() || xacNhan.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ các ô.");
        }
        String tenDN = phien.getTenDangNhap();
        if (!dao.kiemTraMatKhau(phien.getMaND(), MatKhauUtil.bam(tenDN, cu))) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }
        kiemTraMatKhauMoi(moi);
        if (moi.equals(cu)) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        if (!moi.equals(xacNhan)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
        }
        if (!dao.doiMatKhau(phien.getMaND(), MatKhauUtil.bam(tenDN, moi))) {
            throw new IllegalStateException("Không đổi được mật khẩu, vui lòng thử lại.");
        }
    }

    // ------------------------------------------------------------ nhân viên: cấp đăng nhập cho khách
    /** Cấp tên đăng nhập + mật khẩu ban đầu cho khách hàng đã được xác minh KYC. */
    public void capDangNhapChoKhach(int maKH, String tenDangNhap, String matKhau) {
        yeuCauNhanVien();
        KhachHang kh = new KhachHangController(phien).layTheoMa(maKH);
        if (kh == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng.");
        }
        if (!kh.isHoatDong()) {
            throw new IllegalArgumentException("Hồ sơ khách hàng đang ngừng hoạt động.");
        }
        if (!kh.daXacMinh()) {
            throw new IllegalArgumentException(
                    "Khách hàng chưa được xác minh (KYC), chưa thể cấp tài khoản đăng nhập.");
        }
        if (dao.timTheoMaKH(maKH) != null) {
            throw new IllegalArgumentException("Khách hàng này đã có tài khoản đăng nhập.");
        }
        kiemTraTenDangNhap(tenDangNhap);
        kiemTraMatKhauMoi(matKhau);
        int kq = dao.them(tenDangNhap.trim(), MatKhauUtil.bam(tenDangNhap, matKhau),
                null, VaiTro.KHACH, maKH);
        xuLyKetQuaThem(kq);
    }

    // ------------------------------------------------------------ quản trị viên
    public void taoNhanVien(String hoTen, String tenDangNhap, String matKhau, String vaiTro) {
        yeuCauAdmin();
        if (hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (!VaiTro.GIAODICHVIEN.equals(vaiTro) && !VaiTro.ADMIN.equals(vaiTro)) {
            throw new IllegalArgumentException("Vai trò của nhân viên phải là Giao dịch viên hoặc Quản trị viên.");
        }
        kiemTraTenDangNhap(tenDangNhap);
        kiemTraMatKhauMoi(matKhau);
        int kq = dao.them(tenDangNhap.trim(), MatKhauUtil.bam(tenDangNhap, matKhau),
                hoTen.trim(), vaiTro, null);
        xuLyKetQuaThem(kq);
    }

    public ArrayList<NguoiDung> timKiem(String tuKhoa) {
        yeuCauAdmin();
        return dao.getAll(tuKhoa);
    }

    /** Đang hoạt động thì khóa, đang khóa thì mở khóa. Không tự khóa chính mình. */
    public void doiTrangThai(NguoiDung nd) {
        yeuCauAdmin();
        if (nd.getMaND() == phien.getMaND()) {
            throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản của chính mình.");
        }
        if (!dao.doiTrangThai(nd.getMaND(), !nd.isTrangThai())) {
            throw new IllegalStateException("Không đổi được trạng thái, vui lòng thử lại.");
        }
    }

    /** Đặt mật khẩu tạm mới và trả về mật khẩu đó để báo cho người dùng. */
    public String datLaiMatKhau(NguoiDung nd) {
        yeuCauAdmin();
        String tam = MatKhauUtil.taoMatKhauTam();
        if (!dao.doiMatKhau(nd.getMaND(), MatKhauUtil.bam(nd.getTenDangNhap(), tam))) {
            throw new IllegalStateException("Không đặt lại được mật khẩu, vui lòng thử lại.");
        }
        return tam;
    }

    // ------------------------------------------------------------ kiểm tra dữ liệu, quyền
    private void kiemTraTenDangNhap(String ten) {
        if (!ten.trim().matches("[A-Za-z0-9_]{4,20}")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập 4–20 ký tự, chỉ gồm chữ không dấu, số và dấu gạch dưới.");
        }
    }

    private void kiemTraMatKhauMoi(String mk) {
        if (mk.length() < DO_DAI_MAT_KHAU) {
            throw new IllegalArgumentException("Mật khẩu tối thiểu " + DO_DAI_MAT_KHAU + " ký tự.");
        }
    }

    private void xuLyKetQuaThem(int kq) {
        if (kq == NguoiDungDAO.TRUNG_DU_LIEU) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        if (kq == NguoiDungDAO.LOI) {
            throw new IllegalStateException("Không tạo được tài khoản đăng nhập, vui lòng thử lại.");
        }
    }

    private void yeuCauNhanVien() {
        if (!phien.laNhanVien()) {
            throw new IllegalStateException("Chỉ nhân viên ngân hàng mới dùng được chức năng này.");
        }
    }

    private void yeuCauAdmin() {
        if (!phien.laAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới dùng được chức năng này.");
        }
    }
}
