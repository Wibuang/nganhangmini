package module.baocao.controller;

import core.phien.PhienDangNhap;
import core.util.DinhDang;
import java.time.LocalDate;
import java.util.ArrayList;
import module.baocao.dao.NhatKyDAO;
import module.baocao.model.NhatKy;

/**
 * Nhật ký kiểm toán. MỌI module gọi ghi(...) khi có thao tác nhạy cảm (khóa tài khoản, duyệt KYC,
 * đổi tham số...). Ghi nhật ký thất bại KHÔNG được làm hỏng nghiệp vụ đang làm, nên ghi() không ném lỗi.
 * Tuyệt đối không ghi mật khẩu hay mã OTP vào nhật ký.
 */
public class NhatKyController {
    public static final int TOI_DA_HIEN_THI = 500;

    private final PhienDangNhap phien;
    private final NhatKyDAO dao = new NhatKyDAO();

    public NhatKyController(PhienDangNhap phien) {
        this.phien = phien;
    }

    /** Ghi một dòng nhật ký với người thực hiện là người đang đăng nhập. */
    public void ghi(String hanhDong, String doiTuong, String chiTiet) {
        Integer maND = phien.getMaND() > 0 ? phien.getMaND() : null;
        ghiVoiTen(phien.getTenDangNhap(), maND, hanhDong, doiTuong, chiTiet);
    }

    /** Dùng khi chưa có phiên đăng nhập (ví dụ đăng nhập thất bại): tự truyền tên đăng nhập. */
    public void ghiVoiTen(String tenDangNhap, Integer maND, String hanhDong, String doiTuong, String chiTiet) {
        try {
            dao.them(maND, cat(tenDangNhap, 50), cat(hanhDong, 40), cat(doiTuong, 100), cat(chiTiet, 400));
        } catch (RuntimeException ex) {
            ex.printStackTrace();               // nhật ký lỗi không được làm hỏng nghiệp vụ chính
        }
    }

    /** Quản trị viên tra cứu nhật ký (mới nhất trước, tối đa 500 dòng). */
    public ArrayList<NhatKy> tim(String tuKhoa, String tuNgay, String denNgay) {
        if (!phien.laAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới xem được nhật ký.");
        }
        LocalDate tu = DinhDang.docNgay(tuNgay);
        LocalDate den = DinhDang.docNgay(denNgay);
        DinhDang.kiemTraKhoangNgay(tu, den);
        return dao.tim(tuKhoa, tu, den, TOI_DA_HIEN_THI);
    }

    private static String cat(String s, int toiDa) {
        if (s == null) {
            return null;
        }
        return s.length() <= toiDa ? s : s.substring(0, toiDa);
    }
}
