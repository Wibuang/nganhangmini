package module.baocao.controller;

import core.phien.PhienDangNhap;
import java.util.ArrayList;
import module.baocao.dao.ThongBaoDAO;
import module.baocao.model.ThongBao;

/**
 * Thông báo cho khách hàng. Các module khác gọi guiChoKhach(...) sau mỗi sự kiện đáng chú ý
 * (biến động số dư, kết quả KYC, khóa/mở khóa tài khoản...). Gửi thất bại không làm hỏng nghiệp vụ.
 */
public class ThongBaoController {
    private static final int TOI_DA_HIEN_THI = 200;

    private final PhienDangNhap phien;
    private final ThongBaoDAO dao = new ThongBaoDAO();

    public ThongBaoController(PhienDangNhap phien) {
        this.phien = phien;
    }

    /** Gửi thông báo cho một khách hàng (do hệ thống / các module khác gọi). Không ném lỗi. */
    public void guiChoKhach(int maKH, String tieuDe, String noiDung) {
        try {
            dao.them(maKH, cat(tieuDe, 100), cat(noiDung, 400));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    // ------------------------------------------------------------ khách hàng đọc thông báo của mình
    public ArrayList<ThongBao> thongBaoCuaToi() {
        return dao.theoKhachHang(maKHCuaToi(), TOI_DA_HIEN_THI);
    }

    public int soChuaDoc() {
        return dao.demChuaDoc(maKHCuaToi());
    }

    public void danhDauTatCaDaDoc() {
        if (!dao.danhDauTatCaDaDoc(maKHCuaToi())) {
            throw new IllegalStateException("Không cập nhật được, vui lòng thử lại.");
        }
    }

    private int maKHCuaToi() {
        if (!phien.laKhach() || phien.getMaKH() == null) {
            throw new IllegalStateException("Chức năng này chỉ dành cho khách hàng.");
        }
        return phien.getMaKH();
    }

    private static String cat(String s, int toiDa) {
        return s == null || s.length() <= toiDa ? s : s.substring(0, toiDa);
    }
}
