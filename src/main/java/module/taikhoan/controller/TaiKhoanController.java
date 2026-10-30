package module.taikhoan.controller;

import core.phien.PhienDangNhap;
import core.util.DinhDang;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import module.baocao.controller.NhatKyController;
import module.baocao.controller.ThongBaoController;
import module.cauhinh.controller.CauHinhController;
import module.cauhinh.model.KhoaThamSo;
import module.khachhang.controller.KhachHangController;
import module.khachhang.model.KhachHang;
import module.khachhang.model.TrangThaiKYC;
import module.taikhoan.dao.TaiKhoanDAO;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.model.TrangThaiTK;

/**
 * Module Tài khoản: mở, đóng băng, đóng, đổi tên gợi nhớ, tra cứu.
 * Các module khác muốn biết thông tin tài khoản chỉ được gọi controller này.
 * Khách hàng chỉ thao tác được trên tài khoản của chính mình.
 */
public class TaiKhoanController {
    private static final int DO_DAI_TEN = 50;
    private static final String TEN_MAC_DINH = "Tài khoản thanh toán";

    private final PhienDangNhap phien;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final KhachHangController khachHangController;
    private final CauHinhController cauHinh;
    private final NhatKyController nhatKy;
    private final ThongBaoController thongBao;

    public TaiKhoanController(PhienDangNhap phien) {
        this.phien = phien;
        this.khachHangController = new KhachHangController(phien);
        this.cauHinh = new CauHinhController(phien);
        this.nhatKy = new NhatKyController(phien);
        this.thongBao = new ThongBaoController(phien);
    }

    // ------------------------------------------------------------ tra cứu
    /** Lấy tài khoản theo mã (số dư luôn mới nhất). Khách hàng chỉ lấy được tài khoản của mình. */
    public TaiKhoan layTheoMa(int maTK) {
        TaiKhoan tk = dao.timTheoMa(maTK);
        return kiemTraQuyenXem(tk);
    }

    /** Như layTheoMa nhưng có điền tên chủ tài khoản (dùng cho sao kê, biên lai). */
    public TaiKhoan layTheoMaCoTenChu(int maTK) {
        TaiKhoan tk = layTheoMa(maTK);
        dienTenChu(tk);
        return tk;
    }

    /** Như layTheoMa nhưng tìm theo số tài khoản; có điền tên chủ tài khoản. */
    public TaiKhoan layTheoSo(String soTaiKhoan) {
        TaiKhoan tk = kiemTraQuyenXem(dao.timTheoSo(soTaiKhoan.trim()));
        dienTenChu(tk);
        return tk;
    }

    /**
     * Tra cứu tài khoản NHẬN của một lần chuyển khoản (có thể của người khác). Chỉ trả về khi tài khoản
     * tồn tại, đang hoạt động, khác tài khoản gửi và chủ hồ sơ còn hoạt động; kèm tên chủ tài khoản.
     */
    public TaiKhoan traCuuNguoiNhan(String soTaiKhoan, int maTKNguon) {
        if (phien.laHeThong() || (!phien.laKhach() && !phien.laNhanVien())) {
            throw new IllegalStateException("Bạn chưa đăng nhập.");
        }
        String so = soTaiKhoan.trim();
        if (!so.matches("\\d{10}")) {
            throw new IllegalArgumentException("Số tài khoản nhận gồm đúng 10 chữ số.");
        }
        TaiKhoan tk = dao.timTheoSo(so);
        if (tk == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản nhận " + so + ".");
        }
        if (tk.getMaTK() == maTKNguon) {
            throw new IllegalArgumentException("Không thể chuyển khoản vào chính tài khoản đang dùng.");
        }
        if (!tk.dangHoatDong()) {
            throw new IllegalArgumentException("Tài khoản nhận đang không hoạt động.");
        }
        // Đọc tên chủ tài khoản bằng phiên hệ thống: chỉ lấy ra tên, không lộ thêm thông tin nào khác
        KhachHang kh = new KhachHangController(PhienDangNhap.heThong()).layTheoMa(tk.getMaKH());
        if (kh == null || !kh.isHoatDong()) {
            throw new IllegalArgumentException("Chủ tài khoản nhận đang không hoạt động.");
        }
        tk.setTenChuTaiKhoan(kh.getHoTen());
        return tk;
    }

    /** Tài khoản của khách hàng đang đăng nhập. chiHoatDong = true thì bỏ tài khoản đóng băng / đã đóng. */
    public ArrayList<TaiKhoan> taiKhoanCuaToi(boolean chiHoatDong) {
        if (!phien.laKhach() || phien.getMaKH() == null) {
            throw new IllegalStateException("Chức năng này chỉ dành cho khách hàng.");
        }
        ArrayList<TaiKhoan> ds = dao.theoKhachHang(phien.getMaKH(), false);
        if (chiHoatDong) {
            ds.removeIf(tk -> !tk.dangHoatDong());
        }
        return ds;
    }

    /** Nhân viên tìm tài khoản theo số tài khoản hoặc theo thông tin khách hàng (tên, CCCD, SĐT, email). */
    public ArrayList<TaiKhoan> timKiem(String tuKhoa, String trangThai) {
        yeuCauNhanVien();
        ArrayList<Integer> dsMaKH = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            for (KhachHang kh : khachHangController.timKiem(tuKhoa, null)) {
                dsMaKH.add(kh.getMaKH());
            }
        }
        ArrayList<TaiKhoan> ds = dao.tim(tuKhoa, dsMaKH, trangThai);
        Map<Integer, String> tenTheoMa = new HashMap<>();
        for (TaiKhoan tk : ds) {                         // điền tên chủ tài khoản (mỗi khách chỉ hỏi 1 lần)
            if (!tenTheoMa.containsKey(tk.getMaKH())) {
                KhachHang kh = khachHangController.layTheoMa(tk.getMaKH());
                tenTheoMa.put(tk.getMaKH(), kh == null ? "" : kh.getHoTen());
            }
            tk.setTenChuTaiKhoan(tenTheoMa.get(tk.getMaKH()));
        }
        return ds;
    }

    // ------------------------------------------------------------ nhân viên: mở / đóng băng / đóng
    /** Mở tài khoản cho khách hàng có CCCD này (khách phải đã xác minh KYC và đang hoạt động). */
    public TaiKhoan moTaiKhoan(String cccd, String tenGoiNho) {
        yeuCauNhanVien();
        KhachHang kh = khachHangController.timTheoCCCD(cccd);
        if (!kh.isHoatDong()) {
            throw new IllegalArgumentException("Hồ sơ khách hàng đang ngừng hoạt động.");
        }
        if (!kh.daXacMinh()) {
            throw new IllegalArgumentException("Khách hàng chưa được xác minh (KYC: "
                    + TrangThaiKYC.ten(kh.getTrangThaiKYC()) + "), chưa thể mở tài khoản.");
        }
        String ten = tenGoiNho.trim().isEmpty() ? TEN_MAC_DINH : tenGoiNho.trim();
        if (ten.length() > DO_DAI_TEN) {
            throw new IllegalArgumentException("Tên gợi nhớ tối đa " + DO_DAI_TEN + " ký tự.");
        }
        int toiDa = (int) cauHinh.laySo(KhoaThamSo.SO_TAI_KHOAN_TOI_DA);
        if (dao.theoKhachHang(kh.getMaKH(), true).size() >= toiDa) {
            throw new IllegalArgumentException("Mỗi khách hàng chỉ được có tối đa "
                    + toiDa + " tài khoản đang sử dụng.");
        }
        int ma = dao.moTaiKhoan(kh.getMaKH(), ten);
        if (ma == TaiKhoanDAO.LOI) {
            throw new IllegalStateException("Không mở được tài khoản, vui lòng thử lại.");
        }
        TaiKhoan tk = dao.timTheoMa(ma);
        tk.setTenChuTaiKhoan(kh.getHoTen());
        nhatKy.ghi("MO_TAI_KHOAN", "TK " + tk.getSoTaiKhoan(), "Mở cho khách hàng " + kh.getMaKH() + " - " + kh.getHoTen());
        thongBao.guiChoKhach(kh.getMaKH(), "Mở tài khoản mới",
                "Ngân hàng đã mở tài khoản " + tk.getSoTaiKhoan() + " cho bạn.");
        return tk;
    }

    /** Đang hoạt động thì đóng băng, đang đóng băng thì mở băng. */
    public void doiDongBang(TaiKhoan tk) {
        yeuCauNhanVien();
        TaiKhoan moi = layTheoMa(tk.getMaTK());           // đọc lại để chắc chắn trạng thái hiện tại
        if (TrangThaiTK.DA_DONG.equals(moi.getTrangThai())) {
            throw new IllegalArgumentException("Tài khoản đã đóng, không thể thay đổi trạng thái.");
        }
        boolean dongBang = moi.dangHoatDong();
        String trangThaiMoi = dongBang ? TrangThaiTK.DONG_BANG : TrangThaiTK.HOAT_DONG;
        if (!dao.doiTrangThai(moi.getMaTK(), trangThaiMoi)) {
            throw new IllegalStateException("Không đổi được trạng thái, vui lòng thử lại.");
        }
        nhatKy.ghi(dongBang ? "DONG_BANG_TAI_KHOAN" : "MO_BANG_TAI_KHOAN", "TK " + moi.getSoTaiKhoan(), "");
        thongBao.guiChoKhach(moi.getMaKH(), dongBang ? "Tài khoản bị đóng băng" : "Tài khoản được mở băng",
                "Tài khoản " + moi.getSoTaiKhoan() + (dongBang
                        ? " đã bị đóng băng, tạm thời không giao dịch được. Vui lòng liên hệ ngân hàng."
                        : " đã được mở băng, bạn có thể giao dịch bình thường."));
    }

    /** Đóng tài khoản vĩnh viễn; yêu cầu số dư bằng 0. */
    public void dongTaiKhoan(TaiKhoan tk) {
        yeuCauNhanVien();
        TaiKhoan moi = layTheoMa(tk.getMaTK());
        if (TrangThaiTK.DA_DONG.equals(moi.getTrangThai())) {
            throw new IllegalArgumentException("Tài khoản này đã đóng rồi.");
        }
        if (moi.getSoDu() != 0) {
            throw new IllegalArgumentException(
                    "Chỉ đóng được tài khoản có số dư bằng 0 (hiện còn "
                    + DinhDang.tien(moi.getSoDu()) + " VND). Hãy rút hết tiền trước.");
        }
        if (!dao.dongTaiKhoan(moi.getMaTK())) {
            throw new IllegalStateException("Không đóng được tài khoản, vui lòng thử lại.");
        }
        nhatKy.ghi("DONG_TAI_KHOAN", "TK " + moi.getSoTaiKhoan(), "");
        thongBao.guiChoKhach(moi.getMaKH(), "Tài khoản đã đóng",
                "Tài khoản " + moi.getSoTaiKhoan() + " đã được đóng theo yêu cầu.");
    }

    // ------------------------------------------------------------ khách hàng và nhân viên
    public void doiTenGoiNho(int maTK, String tenMoi) {
        TaiKhoan tk = layTheoMa(maTK);                    // đã kiểm tra quyền sở hữu
        String ten = tenMoi.trim();
        if (ten.length() > DO_DAI_TEN) {
            throw new IllegalArgumentException("Tên gợi nhớ tối đa " + DO_DAI_TEN + " ký tự.");
        }
        if (!dao.doiTenGoiNho(tk.getMaTK(), ten.isEmpty() ? TEN_MAC_DINH : ten)) {
            throw new IllegalStateException("Không đổi được tên, vui lòng thử lại.");
        }
    }

    // ------------------------------------------------------------ nội bộ
    /** null → báo không tìm thấy; khách hàng xem tài khoản người khác → báo không có quyền. */
    private TaiKhoan kiemTraQuyenXem(TaiKhoan tk) {
        if (tk == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }
        boolean duocXem = phien.laHeThong() || phien.laNhanVien()
                || (phien.laKhach() && phien.getMaKH() != null && phien.getMaKH() == tk.getMaKH());
        if (!duocXem) {
            throw new IllegalStateException("Bạn không có quyền thao tác trên tài khoản này.");
        }
        return tk;
    }

    private void dienTenChu(TaiKhoan tk) {
        KhachHang kh = khachHangController.layTheoMa(tk.getMaKH());
        tk.setTenChuTaiKhoan(kh == null ? "" : kh.getHoTen());
    }

    private void yeuCauNhanVien() {
        if (!phien.laNhanVien()) {
            throw new IllegalStateException("Chỉ nhân viên ngân hàng mới dùng được chức năng này.");
        }
    }
}
