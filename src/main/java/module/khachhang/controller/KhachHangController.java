package module.khachhang.controller;

import core.phien.PhienDangNhap;
import core.util.DinhDang;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import module.khachhang.dao.KhachHangDAO;
import module.khachhang.model.KhachHang;
import module.khachhang.model.TrangThaiKYC;

/**
 * Module Khách hàng: hồ sơ và xác minh KYC.
 * Các module khác chỉ được gọi tới controller này, không được đọc bảng KhachHang trực tiếp.
 * Lỗi nghiệp vụ: IllegalArgumentException (người dùng nhập sai / vi phạm luật),
 * IllegalStateException (không đủ quyền hoặc lỗi hệ thống).
 */
public class KhachHangController {
    private static final int TUOI_TOI_THIEU = 18;

    private final PhienDangNhap phien;
    private final KhachHangDAO dao = new KhachHangDAO();

    public KhachHangController(PhienDangNhap phien) {
        this.phien = phien;
    }

    // ------------------------------------------------------------ đọc
    /**
     * Đọc hồ sơ theo mã (null nếu không có). Nhân viên và hệ thống đọc được mọi hồ sơ;
     * khách hàng chỉ đọc được hồ sơ của chính mình.
     */
    public KhachHang layTheoMa(int maKH) {
        boolean duocXem = phien.laHeThong() || phien.laNhanVien()
                || (phien.laKhach() && phien.getMaKH() != null && phien.getMaKH() == maKH);
        if (!duocXem) {
            throw new IllegalStateException("Bạn không có quyền xem hồ sơ này.");
        }
        return dao.timTheoMa(maKH);
    }

    /** Hồ sơ của chính khách hàng đang đăng nhập. */
    public KhachHang hoSoCuaToi() {
        if (!phien.laKhach() || phien.getMaKH() == null) {
            throw new IllegalStateException("Chức năng này chỉ dành cho khách hàng.");
        }
        return layTheoMa(phien.getMaKH());
    }

    public ArrayList<KhachHang> timKiem(String tuKhoa, String kyc) {
        yeuCauNhanVien();
        return dao.tim(tuKhoa, kyc);
    }

    /** Tìm theo số CCCD (đủ 12 số). Không thấy thì ném lỗi. */
    public KhachHang timTheoCCCD(String cccd) {
        yeuCauNhanVien();
        if (!cccd.trim().matches("\\d{12}")) {
            throw new IllegalArgumentException("CCCD phải gồm đúng 12 chữ số.");
        }
        KhachHang kh = dao.timTheoCCCD(cccd.trim());
        if (kh == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng có CCCD " + cccd.trim() + ".");
        }
        return kh;
    }

    // ------------------------------------------------------------ nhân viên: tạo / sửa hồ sơ
    public KhachHang taoHoSo(String hoTen, String ngaySinh, String cccd,
                             String sdt, String email, String diaChi) {
        yeuCauNhanVien();
        KhachHang kh = kiemTraVaTao(0, hoTen, ngaySinh, sdt, email, diaChi);
        if (!cccd.trim().matches("\\d{12}")) {
            throw new IllegalArgumentException("CCCD phải gồm đúng 12 chữ số.");
        }
        kh.setCccd(cccd.trim());
        int kq = dao.them(kh);
        if (kq == KhachHangDAO.TRUNG_DU_LIEU) {
            throw new IllegalArgumentException("CCCD " + kh.getCccd() + " đã có hồ sơ khách hàng.");
        }
        if (kq == KhachHangDAO.LOI) {
            throw new IllegalStateException("Không tạo được hồ sơ, vui lòng thử lại.");
        }
        return dao.timTheoMa(kq);
    }

    public void capNhat(int maKH, String hoTen, String ngaySinh, String sdt, String email, String diaChi) {
        yeuCauNhanVien();
        KhachHang kh = kiemTraVaTao(maKH, hoTen, ngaySinh, sdt, email, diaChi);
        if (!dao.capNhat(kh)) {
            throw new IllegalStateException("Cập nhật thất bại, vui lòng thử lại.");
        }
    }

    // ------------------------------------------------------------ khách hàng: tự sửa liên hệ
    public void capNhatLienHe(String sdt, String email, String diaChi) {
        if (!phien.laKhach() || phien.getMaKH() == null) {
            throw new IllegalStateException("Chức năng này chỉ dành cho khách hàng.");
        }
        kiemTraLienHe(sdt, email);
        if (!dao.capNhatLienHe(phien.getMaKH(), sdt.trim(), email.trim(), diaChi.trim())) {
            throw new IllegalStateException("Cập nhật thất bại, vui lòng thử lại.");
        }
    }

    // ------------------------------------------------------------ quản trị viên: KYC, hoạt động
    public void duyetKYC(KhachHang kh) {
        yeuCauAdmin();
        if (kh.daXacMinh()) {
            throw new IllegalArgumentException("Hồ sơ này đã được xác minh rồi.");
        }
        if (!dao.capNhatKYC(kh.getMaKH(), TrangThaiKYC.DA_XAC_MINH, null)) {
            throw new IllegalStateException("Không duyệt được hồ sơ, vui lòng thử lại.");
        }
    }

    public void tuChoiKYC(KhachHang kh, String lyDo) {
        yeuCauAdmin();
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        }
        if (lyDo.trim().length() > 200) {
            throw new IllegalArgumentException("Lý do tối đa 200 ký tự.");
        }
        if (!dao.capNhatKYC(kh.getMaKH(), TrangThaiKYC.TU_CHOI, lyDo.trim())) {
            throw new IllegalStateException("Không cập nhật được, vui lòng thử lại.");
        }
    }

    /** Đang hoạt động thì ngừng, đang ngừng thì kích hoạt lại. Hồ sơ không bao giờ bị xóa. */
    public void doiHoatDong(KhachHang kh) {
        yeuCauAdmin();
        if (!dao.doiHoatDong(kh.getMaKH(), !kh.isHoatDong())) {
            throw new IllegalStateException("Không đổi được trạng thái hồ sơ, vui lòng thử lại.");
        }
    }

    // ------------------------------------------------------------ kiểm tra dữ liệu
    private KhachHang kiemTraVaTao(int maKH, String hoTen, String ngaySinh,
                                   String sdt, String email, String diaChi) {
        if (hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (hoTen.trim().length() > 100) {
            throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
        }
        LocalDate ns = DinhDang.docNgayBatBuoc("ngày sinh", ngaySinh);
        if (ns.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        }
        if (Period.between(ns, LocalDate.now()).getYears() < TUOI_TOI_THIEU) {
            throw new IllegalArgumentException(
                    "Khách hàng phải từ đủ " + TUOI_TOI_THIEU + " tuổi trở lên.");
        }
        kiemTraLienHe(sdt, email);
        if (diaChi.trim().length() > 200) {
            throw new IllegalArgumentException("Địa chỉ tối đa 200 ký tự.");
        }
        KhachHang kh = new KhachHang();
        kh.setMaKH(maKH);
        kh.setHoTen(hoTen.trim());
        kh.setNgaySinh(ns);
        kh.setSoDienThoai(sdt.trim());
        kh.setEmail(email.trim());
        kh.setDiaChi(diaChi.trim());
        return kh;
    }

    private void kiemTraLienHe(String sdt, String email) {
        if (!sdt.trim().isEmpty() && !DinhDang.laSoDienThoaiHopLe(sdt)) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 9–11 chữ số.");
        }
        if (!email.trim().isEmpty() && !DinhDang.laEmailHopLe(email)) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
    }

    // ------------------------------------------------------------ kiểm tra quyền
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
