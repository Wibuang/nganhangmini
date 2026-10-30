package module.baocao.controller;

import core.phien.PhienDangNhap;
import core.util.CsvUtil;
import core.util.DinhDang;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import module.baocao.dao.BaoCaoDAO;
import module.baocao.model.DoiSoat;
import module.baocao.model.DongBaoCao;
import module.baocao.model.DongTopSoDu;
import module.baocao.model.ThongKe;

/** Báo cáo và đối soát. Nhân viên xem báo cáo giao dịch; tổng quan và đối soát chỉ dành cho quản trị viên. */
public class BaoCaoController {
    private final PhienDangNhap phien;
    private final BaoCaoDAO dao = new BaoCaoDAO();

    public BaoCaoController(PhienDangNhap phien) {
        this.phien = phien;
    }

    public ThongKe thongKe() {
        yeuCauAdmin();
        return dao.layThongKe();
    }

    public ArrayList<DongTopSoDu> topSoDu(int soLuong) {
        yeuCauAdmin();
        return dao.topSoDu(soLuong);
    }

    /** Báo cáo giao dịch theo ngày (tối đa 366 ngày) hoặc theo tháng (tối đa 5 năm). Cả hai ngày đều bắt buộc. */
    public ArrayList<DongBaoCao> baoCao(String tuNgay, String denNgay, boolean theoThang) {
        LocalDate tu = DinhDang.docNgayBatBuoc("từ ngày", tuNgay);
        LocalDate den = DinhDang.docNgayBatBuoc("đến ngày", denNgay);
        DinhDang.kiemTraKhoangNgay(tu, den);
        if (!theoThang && tu.plusDays(366).isBefore(den)) {
            throw new IllegalArgumentException("Báo cáo theo ngày tối đa 366 ngày; hãy chọn báo cáo theo tháng.");
        }
        if (theoThang && tu.plusYears(5).isBefore(den)) {
            throw new IllegalArgumentException("Báo cáo theo tháng tối đa 5 năm.");
        }
        yeuCauNhanVien();
        return dao.baoCaoTheoKy(tu, den, theoThang);
    }

    public DoiSoat doiSoat() {
        yeuCauAdmin();
        return dao.doiSoat();
    }

    public void xuatBaoCao(ArrayList<DongBaoCao> ds, boolean theoThang, File file) {
        ArrayList<String[]> dong = new ArrayList<>();
        dong.add(new String[]{theoThang ? "Tháng" : "Ngày", "Số giao dịch", "Tổng nạp", "Tổng rút",
                              "Tổng chuyển khoản", "Tổng phí thu", "Tổng hoàn"});
        for (DongBaoCao d : ds) {
            dong.add(new String[]{d.getKy(), String.valueOf(d.getSoGiaoDich()),
                    String.valueOf(d.getTongNap()), String.valueOf(d.getTongRut()),
                    String.valueOf(d.getTongChuyen()), String.valueOf(d.getTongPhi()),
                    String.valueOf(d.getTongHoan())});
        }
        try {
            CsvUtil.ghi(file, dong);
        } catch (IOException ex) {
            throw new IllegalStateException("Không ghi được file: " + ex.getMessage());
        }
    }

    private void yeuCauNhanVien() {
        if (!phien.laNhanVien()) {
            throw new IllegalStateException("Chỉ nhân viên ngân hàng mới xem được báo cáo.");
        }
    }

    private void yeuCauAdmin() {
        if (!phien.laAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới dùng được chức năng này.");
        }
    }
}
