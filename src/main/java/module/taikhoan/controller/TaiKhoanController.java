package module.taikhoan.controller;

import core.phien.PhienDangNhap;
import core.util.DinhDang;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private static final int TOI_DA_TAI_KHOAN = 5;
    private static final int DO_DAI_TEN = 50;
    private static final String TEN_MAC_DINH = "Tài khoản thanh toán";

    private final PhienDangNhap phien;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final KhachHangController khachHangController;

    public TaiKhoanController(PhienDangNhap phien) {
        this.phien = phien;
        this.khachHangController = new KhachHangController(phien);
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
        if (dao.theoKhachHang(kh.getMaKH(), true).size() >= TOI_DA_TAI_KHOAN) {
            throw new IllegalArgumentException("Mỗi khách hàng chỉ được có tối đa "
                    + TOI_DA_TAI_KHOAN + " tài khoản đang sử dụng.");
        }
        int ma = dao.moTaiKhoan(kh.getMaKH(), ten);
        if (ma == TaiKhoanDAO.LOI) {
            throw new IllegalStateException("Không mở được tài khoản, vui lòng thử lại.");
        }
        TaiKhoan tk = dao.timTheoMa(ma);
        tk.setTenChuTaiKhoan(kh.getHoTen());
        return tk;
    }

    /** Đang hoạt động thì đóng băng, đang đóng băng thì mở băng. */
    public void doiDongBang(TaiKhoan tk) {
        yeuCauNhanVien();
        TaiKhoan moi = layTheoMa(tk.getMaTK());           // đọc lại để chắc chắn trạng thái hiện tại
        if (TrangThaiTK.DA_DONG.equals(moi.getTrangThai())) {
            throw new IllegalArgumentException("Tài khoản đã đóng, không thể thay đổi trạng thái.");
        }
        String trangThaiMoi = moi.dangHoatDong() ? TrangThaiTK.DONG_BANG : TrangThaiTK.HOAT_DONG;
        if (!dao.doiTrangThai(moi.getMaTK(), trangThaiMoi)) {
            throw new IllegalStateException("Không đổi được trạng thái, vui lòng thử lại.");
        }
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
