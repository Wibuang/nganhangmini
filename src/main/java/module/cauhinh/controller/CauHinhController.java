package module.cauhinh.controller;

import core.phien.PhienDangNhap;
import core.util.DinhDang;
import java.math.BigDecimal;
import java.util.ArrayList;
import module.baocao.controller.NhatKyController;
import module.cauhinh.dao.CauHinhDAO;
import module.cauhinh.model.BieuPhi;
import module.cauhinh.model.KhoaThamSo;
import module.cauhinh.model.LaiSuat;
import module.cauhinh.model.ThamSo;

/**
 * Module Cấu hình. Các module khác ĐỌC tham số qua laySo(...) (ai cũng đọc được, kể cả phiên hệ thống);
 * chỉ quản trị viên được SỬA, và mọi thay đổi đều được ghi nhật ký.
 * CSDL chưa có tham số nào thì dùng giá trị mặc định trong KhoaThamSo.
 */
public class CauHinhController {
    private final PhienDangNhap phien;
    private final CauHinhDAO dao = new CauHinhDAO();
    private final NhatKyController nhatKy;

    public CauHinhController(PhienDangNhap phien) {
        this.phien = phien;
        this.nhatKy = new NhatKyController(phien);
    }

    // ------------------------------------------------------------ đọc (mọi module)
    /** Giá trị tham số (mã lấy từ KhoaThamSo). Thiếu trong CSDL thì trả về giá trị mặc định. */
    public long laySo(String ma) {
        Long v = dao.layGiaTri(ma);
        return v != null ? v : KhoaThamSo.macDinh(ma);
    }

    /** Phí chuyển khoản cho số tiền này theo biểu phí hiện hành (0 nếu không có bậc phù hợp). */
    public long tinhPhiChuyenKhoan(long soTien) {
        Long phi = dao.phiTheoSoTien(BieuPhi.CHUYEN_KHOAN, soTien);
        return phi == null ? 0 : phi;
    }

    // ------------------------------------------------------------ quản trị viên xem
    public ArrayList<ThamSo> layTatCaThamSo() {
        yeuCauAdmin();
        return dao.layTatCaThamSo();
    }

    public ArrayList<BieuPhi> layBieuPhiChuyenKhoan() {
        yeuCauAdmin();
        return dao.layBieuPhi(BieuPhi.CHUYEN_KHOAN);
    }

    public ArrayList<LaiSuat> layLaiSuat() {
        yeuCauAdmin();
        return dao.layLaiSuat();
    }

    // ------------------------------------------------------------ quản trị viên sửa
    public void capNhatThamSo(String ma, String giaTriText) {
        long giaTri = DinhDang.docTien(giaTriText);
        if (!KhoaThamSo.tonTai(ma)) {
            throw new IllegalArgumentException("Tham số không tồn tại: " + ma);
        }
        long nho = KhoaThamSo.nhoNhat(ma);
        long lon = KhoaThamSo.lonNhat(ma);
        if (giaTri < nho || giaTri > lon) {
            throw new IllegalArgumentException("Giá trị phải từ " + DinhDang.tien(nho)
                    + " đến " + DinhDang.tien(lon) + ".");
        }
        yeuCauAdmin();
        kiemTraTuongQuan(ma, giaTri);
        long cu = laySo(ma);
        if (!dao.datThamSo(ma, giaTri)) {
            throw new IllegalStateException("Không lưu được tham số, vui lòng thử lại.");
        }
        nhatKy.ghi("DOI_THAM_SO", ma, "Từ " + cu + " thành " + giaTri);
    }

    public void capNhatPhi(BieuPhi bac, String phiText) {
        long phi = DinhDang.docTien(phiText);
        if (phi < 0 || phi > 10_000_000L) {
            throw new IllegalArgumentException("Phí phải từ 0 đến 10.000.000 VND.");
        }
        yeuCauAdmin();
        if (!dao.capNhatPhi(bac.getMaPhi(), phi)) {
            throw new IllegalStateException("Không lưu được biểu phí, vui lòng thử lại.");
        }
        nhatKy.ghi("DOI_BIEU_PHI", "Bậc phí " + bac.getMaPhi(),
                "Từ " + bac.getPhi() + " thành " + phi + " VND");
    }

    public void capNhatLaiSuat(LaiSuat ls, String laiSuatText) {
        BigDecimal moi;
        try {
            moi = new BigDecimal(laiSuatText.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Lãi suất không hợp lệ, nhập số, ví dụ 5.8");
        }
        if (moi.signum() < 0 || moi.compareTo(new BigDecimal("30")) > 0) {
            throw new IllegalArgumentException("Lãi suất phải từ 0 đến 30 %/năm.");
        }
        yeuCauAdmin();
        if (!dao.capNhatLaiSuat(ls.getMaLS(), moi.setScale(2, java.math.RoundingMode.HALF_UP))) {
            throw new IllegalStateException("Không lưu được lãi suất, vui lòng thử lại.");
        }
        nhatKy.ghi("DOI_LAI_SUAT", ls.getLoaiLai() + " " + ls.getKyHanThang() + " tháng",
                "Từ " + ls.getLaiSuatNam() + " thành " + moi + " %/năm");
    }

    // ------------------------------------------------------------ kiểm tra
    /** Các tham số liên quan nhau phải hợp lý: hạn mức mỗi lần ≤ hạn mức mỗi ngày, mức tối thiểu ≤ hạn mức. */
    private void kiemTraTuongQuan(String ma, long moi) {
        long moiLan = KhoaThamSo.HAN_MUC_CHUYEN_MOI_LAN.equals(ma) ? moi : laySo(KhoaThamSo.HAN_MUC_CHUYEN_MOI_LAN);
        long moiNgay = KhoaThamSo.HAN_MUC_CHUYEN_MOI_NGAY.equals(ma) ? moi : laySo(KhoaThamSo.HAN_MUC_CHUYEN_MOI_NGAY);
        long toiThieu = KhoaThamSo.CHUYEN_TOI_THIEU.equals(ma) ? moi : laySo(KhoaThamSo.CHUYEN_TOI_THIEU);
        if (moiLan > moiNgay) {
            throw new IllegalArgumentException(
                    "Hạn mức chuyển khoản mỗi lần không được lớn hơn hạn mức mỗi ngày.");
        }
        if (toiThieu > moiLan) {
            throw new IllegalArgumentException(
                    "Số tiền chuyển tối thiểu không được lớn hơn hạn mức mỗi lần.");
        }
    }

    private void yeuCauAdmin() {
        if (!phien.laAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới dùng được chức năng này.");
        }
    }
}
