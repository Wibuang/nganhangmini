package module.giaodich.controller;

import core.phien.PhienDangNhap;
import core.util.CsvUtil;
import core.util.DinhDang;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import module.giaodich.dao.GiaoDichDAO;
import module.giaodich.model.GiaoDich;
import module.giaodich.model.KenhGD;
import module.giaodich.model.LoaiGD;
import module.giaodich.model.SaoKe;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;

/**
 * Module Giao dịch & Sổ cái. MỌI thay đổi số dư của hệ thống phải đi qua module này.
 * (Các module sau như Chuyển khoản, Tiết kiệm, Khoản vay sẽ gọi vào đây để ghi sổ.)
 * Khách hàng chỉ thao tác được trên tài khoản của chính mình.
 */
public class GiaoDichController {
    private static final long TOI_DA_QUAY = 500_000_000L;
    private static final long TOI_DA_UNG_DUNG = 50_000_000L;
    private static final int  TOI_DA_LICH_SU = 500;
    private static final int  TOI_DA_SAO_KE = 5000;

    private final PhienDangNhap phien;
    private final GiaoDichDAO dao = new GiaoDichDAO();
    private final TaiKhoanController taiKhoanController;

    public GiaoDichController(PhienDangNhap phien) {
        this.phien = phien;
        this.taiKhoanController = new TaiKhoanController(phien);
    }

    // ================================================================ nạp / rút
    /** Khách hàng nạp tiền vào tài khoản của mình trên ứng dụng. Trả về biên lai (dòng sổ cái). */
    public GiaoDich napTienUngDung(int maTK, String soTienText) {
        return khachThucHien(maTK, LoaiGD.NAP, soTienText, "Nạp tiền trên ứng dụng");
    }

    /** Khách hàng rút tiền từ tài khoản của mình trên ứng dụng. */
    public GiaoDich rutTienUngDung(int maTK, String soTienText) {
        return khachThucHien(maTK, LoaiGD.RUT, soTienText, "Rút tiền trên ứng dụng");
    }

    /** Nhân viên nạp tiền cho khách tại quầy. */
    public GiaoDich napTienQuay(String soTaiKhoan, String soTienText, String ghiChu) {
        return nhanVienThucHien(soTaiKhoan, LoaiGD.NAP, soTienText, ghiChu, "Nạp tiền tại quầy");
    }

    /** Nhân viên rút tiền cho khách tại quầy. */
    public GiaoDich rutTienQuay(String soTaiKhoan, String soTienText, String ghiChu) {
        return nhanVienThucHien(soTaiKhoan, LoaiGD.RUT, soTienText, ghiChu, "Rút tiền tại quầy");
    }

    private GiaoDich khachThucHien(int maTK, String loai, String soTienText, String ghiChu) {
        if (!phien.laKhach()) {
            throw new IllegalStateException("Chức năng này chỉ dành cho khách hàng.");
        }
        long soTien = kiemTraSoTien(soTienText, TOI_DA_UNG_DUNG);
        TaiKhoan tk = taiKhoanController.layTheoMa(maTK);      // kiểm tra tài khoản này là của khách
        return thucHien(tk, loai, soTien, KenhGD.UNG_DUNG, ghiChu);
    }

    private GiaoDich nhanVienThucHien(String soTaiKhoan, String loai, String soTienText,
                                      String ghiChu, String ghiChuMacDinh) {
        if (!phien.laNhanVien()) {
            throw new IllegalStateException("Chỉ nhân viên ngân hàng mới giao dịch tại quầy.");
        }
        long soTien = kiemTraSoTien(soTienText, TOI_DA_QUAY);
        String nd = ghiChu.trim();
        if (nd.length() > 150) {
            throw new IllegalArgumentException("Ghi chú tối đa 150 ký tự.");
        }
        TaiKhoan tk = taiKhoanController.layTheoSo(soTaiKhoan);
        return thucHien(tk, loai, soTien, KenhGD.QUAY, nd.isEmpty() ? ghiChuMacDinh : nd);
    }

    /** Đọc và kiểm tra số tiền trước khi chạm vào CSDL. */
    private long kiemTraSoTien(String soTienText, long toiDa) {
        long soTien = DinhDang.docTien(soTienText);
        if (soTien <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0.");
        }
        if (soTien > toiDa) {
            throw new IllegalArgumentException("Mỗi giao dịch tối đa " + DinhDang.tien(toiDa) + " VND.");
        }
        return soTien;
    }

    private GiaoDich thucHien(TaiKhoan tk, String loai, long soTien, String kenh, String ghiChu) {
        if (!tk.dangHoatDong()) {
            throw new IllegalArgumentException(
                    "Tài khoản đang ở trạng thái không cho phép giao dịch (đóng băng hoặc đã đóng).");
        }
        long kq = dao.ghiSo(tk.getMaTK(), tk.getSoTaiKhoan(), loai, soTien, kenh, ghiChu,
                phien.getMaND(), null);
        return xuLyKetQua(kq, "Số dư không đủ để rút.");
    }

    private GiaoDich xuLyKetQua(long ketQua, String thongBaoKhongDuTien) {
        if (ketQua == GiaoDichDAO.KHONG_DU_SO_DU) {
            throw new IllegalArgumentException(thongBaoKhongDuTien);
        }
        if (ketQua <= 0) {
            throw new IllegalStateException("Giao dịch thất bại, vui lòng thử lại.");
        }
        return dao.timTheoMa((int) ketQua);
    }

    // ================================================================ đảo giao dịch
    /**
     * Quản trị viên đảo một giao dịch nạp hoặc rút bằng cách ghi thêm một giao dịch ngược
     * (giao dịch gốc không bị sửa hay xóa). Mỗi giao dịch chỉ đảo được một lần.
     */
    public GiaoDich daoGiaoDich(int maGD, String lyDo) {
        if (!phien.laAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới đảo được giao dịch.");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do đảo giao dịch.");
        }
        GiaoDich goc = dao.timTheoMa(maGD);
        if (goc == null) {
            throw new IllegalArgumentException("Không tìm thấy giao dịch.");
        }
        if (!LoaiGD.NAP.equals(goc.getLoaiGD()) && !LoaiGD.RUT.equals(goc.getLoaiGD())) {
            throw new IllegalArgumentException("Chỉ đảo được giao dịch nạp tiền hoặc rút tiền.");
        }
        if (dao.daDaoNguoc(maGD)) {
            throw new IllegalArgumentException("Giao dịch " + goc.getMaThamChieu() + " đã được đảo trước đó.");
        }
        TaiKhoan tk = taiKhoanController.layTheoMa(goc.getMaTK());
        if (!tk.dangHoatDong()) {
            throw new IllegalArgumentException(
                    "Tài khoản đang đóng băng hoặc đã đóng, hãy mở băng trước khi đảo giao dịch.");
        }
        boolean daoNap = LoaiGD.NAP.equals(goc.getLoaiGD());
        String ghiChu = "Đảo giao dịch " + goc.getMaThamChieu() + ": " + lyDo.trim();
        if (ghiChu.length() > 200) {
            ghiChu = ghiChu.substring(0, 200);
        }
        long kq = dao.ghiSo(tk.getMaTK(), tk.getSoTaiKhoan(),
                daoNap ? LoaiGD.HOAN_NAP : LoaiGD.HOAN_RUT, goc.getSoTien(), KenhGD.HE_THONG,
                ghiChu, phien.getMaND(), goc.getMaGD());
        return xuLyKetQua(kq, "Số dư tài khoản không đủ để đảo giao dịch nạp tiền này.");
    }

    // ================================================================ lịch sử, sao kê
    /** Lịch sử của một tài khoản (khách hàng: của mình). loai = mã LoaiGD hoặc null; ngày dd/MM/yyyy hoặc rỗng. */
    public ArrayList<GiaoDich> lichSuTaiKhoan(int maTK, String loai, String tuNgay, String denNgay) {
        LocalDate tu = DinhDang.docNgay(tuNgay);               // kiểm tra dữ liệu nhập trước
        LocalDate den = DinhDang.docNgay(denNgay);
        DinhDang.kiemTraKhoangNgay(tu, den);
        TaiKhoan tk = taiKhoanController.layTheoMa(maTK);      // rồi kiểm tra quyền + đọc CSDL
        return dao.truyVan(tk.getMaTK(), null, loai, tu, den, false, TOI_DA_LICH_SU);
    }

    /** Nhân viên tra cứu giao dịch toàn hệ thống theo số tài khoản hoặc mã giao dịch. */
    public ArrayList<GiaoDich> timGiaoDich(String tuKhoa, String loai, String tuNgay, String denNgay) {
        if (!phien.laNhanVien()) {
            throw new IllegalStateException("Chỉ nhân viên ngân hàng mới tra cứu được toàn bộ giao dịch.");
        }
        LocalDate tu = DinhDang.docNgay(tuNgay);
        LocalDate den = DinhDang.docNgay(denNgay);
        DinhDang.kiemTraKhoangNgay(tu, den);
        return dao.truyVan(null, tuKhoa, loai, tu, den, false, TOI_DA_LICH_SU);
    }

    /** Sao kê tài khoản trong kỳ [tuNgay, denNgay] (bắt buộc nhập cả hai, tối đa 1 năm). */
    public SaoKe saoKe(int maTK, String tuNgay, String denNgay) {
        LocalDate tu = DinhDang.docNgayBatBuoc("từ ngày", tuNgay);   // kiểm tra dữ liệu nhập trước
        LocalDate den = DinhDang.docNgayBatBuoc("đến ngày", denNgay);
        DinhDang.kiemTraKhoangNgay(tu, den);
        if (tu.plusYears(1).isBefore(den)) {
            throw new IllegalArgumentException("Khoảng thời gian sao kê tối đa 1 năm.");
        }
        TaiKhoan tk = taiKhoanController.layTheoMaCoTenChu(maTK);    // rồi kiểm tra quyền + đọc CSDL
        long dau = dao.soDuTruocNgay(tk.getMaTK(), tu);
        ArrayList<GiaoDich> ds = dao.truyVan(tk.getMaTK(), null, null, tu, den, true, TOI_DA_SAO_KE);
        long vao = 0;
        long ra = 0;
        for (GiaoDich g : ds) {
            if (LoaiGD.laTienVao(g.getLoaiGD())) {
                vao += g.getSoTien();
            } else {
                ra += g.getSoTien();
            }
        }
        long cuoi = ds.isEmpty() ? dau : ds.get(ds.size() - 1).getSoDuSau();
        String chu = tk.getTenChuTaiKhoan() == null ? "" : tk.getTenChuTaiKhoan();
        return new SaoKe(tk.getSoTaiKhoan(), chu, tu, den, dau, vao, ra, cuoi, ds);
    }

    // ================================================================ xuất file
    public void xuatLichSu(ArrayList<GiaoDich> ds, File file) {
        ArrayList<String[]> dong = new ArrayList<>();
        dong.add(new String[]{"Mã giao dịch", "Thời gian", "Số tài khoản", "Loại", "Số tiền",
                              "Số dư sau", "Kênh", "Tài khoản đối tác", "Nội dung"});
        for (GiaoDich g : ds) {
            dong.add(dongCsv(g));
        }
        ghiFile(file, dong);
    }

    public void xuatSaoKe(SaoKe sk, File file) {
        ArrayList<String[]> dong = new ArrayList<>();
        dong.add(new String[]{"SAO KÊ TÀI KHOẢN " + sk.getSoTaiKhoan()});
        dong.add(new String[]{"Chủ tài khoản", sk.getTenChuTaiKhoan()});
        dong.add(new String[]{"Từ ngày", DinhDang.ngay(sk.getTuNgay()), "Đến ngày", DinhDang.ngay(sk.getDenNgay())});
        dong.add(new String[]{"Số dư đầu kỳ", String.valueOf(sk.getSoDuDau())});
        dong.add(new String[]{"Tổng tiền vào", String.valueOf(sk.getTongVao())});
        dong.add(new String[]{"Tổng tiền ra", String.valueOf(sk.getTongRa())});
        dong.add(new String[]{"Số dư cuối kỳ", String.valueOf(sk.getSoDuCuoi())});
        dong.add(new String[]{});
        dong.add(new String[]{"Mã giao dịch", "Thời gian", "Số tài khoản", "Loại", "Số tiền",
                              "Số dư sau", "Kênh", "Tài khoản đối tác", "Nội dung"});
        for (GiaoDich g : sk.getDanhSach()) {
            dong.add(dongCsv(g));
        }
        ghiFile(file, dong);
    }

    private String[] dongCsv(GiaoDich g) {
        String dau = LoaiGD.laTienVao(g.getLoaiGD()) ? "+" : "-";
        return new String[]{g.getMaThamChieu(), DinhDang.ngayGio(g.getNgayGD()), g.getSoTaiKhoan(),
                LoaiGD.tenHienThi(g.getLoaiGD()), dau + g.getSoTien(), String.valueOf(g.getSoDuSau()),
                KenhGD.ten(g.getKenh()), g.getSoTKDoiTac(), g.getGhiChu()};
    }

    private void ghiFile(File file, ArrayList<String[]> dong) {
        try {
            CsvUtil.ghi(file, dong);
        } catch (IOException ex) {
            throw new IllegalStateException("Không ghi được file: " + ex.getMessage());
        }
    }
}
