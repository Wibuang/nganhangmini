package module.chuyenkhoan.controller;

import core.phien.PhienDangNhap;
import core.util.DinhDang;
import java.time.LocalDate;
import java.util.ArrayList;
import module.baocao.controller.NhatKyController;
import module.baocao.controller.ThongBaoController;
import module.cauhinh.controller.CauHinhController;
import module.cauhinh.model.KhoaThamSo;
import module.chuyenkhoan.dao.LenhDinhKyDAO;
import module.chuyenkhoan.dao.NguoiThuHuongDAO;
import module.chuyenkhoan.model.KetQuaChayLenh;
import module.chuyenkhoan.model.KetQuaChuyenKhoan;
import module.chuyenkhoan.model.LenhDinhKy;
import module.chuyenkhoan.model.NguoiThuHuong;
import module.chuyenkhoan.model.TanSuat;
import module.chuyenkhoan.model.TrangThaiLenh;
import module.chuyenkhoan.model.XemTruocChuyenKhoan;
import module.giaodich.controller.GiaoDichController;
import module.giaodich.model.GiaoDich;
import module.giaodich.model.KenhGD;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.xacthuc.controller.OtpController;

/**
 * Module Chuyển khoản: luật nghiệp vụ của việc chuyển tiền (hạn mức mỗi lần và mỗi ngày, phí theo bậc,
 * OTP cho số tiền lớn, danh bạ người nhận, lệnh chuyển định kỳ).
 * Module này KHÔNG tự thay đổi số dư: việc ghi sổ do module Giao dịch (GiaoDichController.chuyenNoiBo) làm.
 * Mọi hạn mức, phí, ngưỡng OTP lấy từ module Cấu hình.
 */
public class ChuyenKhoanController {
    private static final int DO_DAI_NOI_DUNG = 100;
    private static final int DO_DAI_TEN_GOI_NHO = 50;
    private static final int TOI_DA_LENH_DANG_DUNG = 10;

    private final PhienDangNhap phien;
    private final CauHinhController cauHinh;
    private final TaiKhoanController taiKhoanController;
    private final GiaoDichController giaoDichController;
    private final OtpController otp;
    private final NhatKyController nhatKy;
    private final ThongBaoController thongBao;
    private final NguoiThuHuongDAO danhBaDao = new NguoiThuHuongDAO();
    private final LenhDinhKyDAO lenhDao = new LenhDinhKyDAO();

    public ChuyenKhoanController(PhienDangNhap phien) {
        this.phien = phien;
        this.cauHinh = new CauHinhController(phien);
        this.taiKhoanController = new TaiKhoanController(phien);
        this.giaoDichController = new GiaoDichController(phien);
        this.otp = new OtpController(phien);
        this.nhatKy = new NhatKyController(phien);
        this.thongBao = new ThongBaoController(phien);
    }

    // ============================================================ chuyển khoản (khách hàng)
    /** Kiểm tra số tài khoản người nhận và trả về tài khoản đó (có tên chủ tài khoản). */
    public TaiKhoan traCuuNguoiNhan(int maTKNguon, String soTKNhan) {
        yeuCauKhach();
        taiKhoanController.layTheoMa(maTKNguon);                    // bảo đảm tài khoản nguồn là của mình
        return taiKhoanController.traCuuNguoiNhan(soTKNhan, maTKNguon);
    }

    /** Kiểm tra mọi điều kiện và cho biết người nhận, phí, tổng tiền bị trừ, có cần OTP không. */
    public XemTruocChuyenKhoan xemTruoc(int maTKNguon, String soTKNhan, String soTienText) {
        yeuCauKhach();
        long soTien = kiemTraSoTien(DinhDang.docTien(soTienText));
        return chuanBi(maTKNguon, soTKNhan, soTien);
    }

    /** Tạo mã OTP giả lập cho giao dịch lớn (hệ thống thật sẽ gửi qua SMS). */
    public String taoOtp() {
        yeuCauKhach();
        return otp.taoOtp();
    }

    /**
     * Thực hiện chuyển khoản. Nếu số tiền từ ngưỡng OTP trở lên thì phải truyền đúng mã OTP (otpNhap).
     * Thứ tự: kiểm tra dữ liệu nhập → kiểm tra điều kiện (CSDL) → xác nhận OTP → ghi sổ.
     */
    public KetQuaChuyenKhoan chuyenKhoan(int maTKNguon, String soTKNhan, String soTienText,
                                         String noiDung, String otpNhap) {
        yeuCauKhach();
        long soTien = kiemTraSoTien(DinhDang.docTien(soTienText));
        String nd = chuanHoaNoiDung(noiDung);
        XemTruocChuyenKhoan xt = chuanBi(maTKNguon, soTKNhan, soTien);
        if (xt.isCanOtp()) {
            otp.kiemTra(otpNhap);
        }
        GiaoDich di = giaoDichController.chuyenNoiBo(xt.getNguon(), xt.getNhan(), soTien,
                xt.getPhi(), nd, KenhGD.UNG_DUNG);
        return new KetQuaChuyenKhoan(di, xt.getPhi(), xt.getNhan().getTenChuTaiKhoan(),
                xt.getNhan().getSoTaiKhoan());
    }

    // ------------------------------------------------------------ các bước kiểm tra dùng chung
    private long kiemTraSoTien(long soTien) {
        long toiThieu = cauHinh.laySo(KhoaThamSo.CHUYEN_TOI_THIEU);
        long toiDa = cauHinh.laySo(KhoaThamSo.HAN_MUC_CHUYEN_MOI_LAN);
        if (soTien < toiThieu) {
            throw new IllegalArgumentException("Số tiền chuyển tối thiểu " + DinhDang.tien(toiThieu) + " VND.");
        }
        if (soTien > toiDa) {
            throw new IllegalArgumentException("Mỗi lần chuyển tối đa " + DinhDang.tien(toiDa) + " VND.");
        }
        return soTien;
    }

    private String chuanHoaNoiDung(String noiDung) {
        String nd = noiDung.trim();
        if (nd.length() > DO_DAI_NOI_DUNG) {
            throw new IllegalArgumentException("Nội dung tối đa " + DO_DAI_NOI_DUNG + " ký tự.");
        }
        return nd.isEmpty() ? phien.getTenHienThi() + " chuyển tiền" : nd;
    }

    /** Các điều kiện cần đọc CSDL: tài khoản nguồn, người nhận, hạn mức ngày, số dư. */
    private XemTruocChuyenKhoan chuanBi(int maTKNguon, String soTKNhan, long soTien) {
        TaiKhoan nguon = taiKhoanController.layTheoMa(maTKNguon);   // đã kiểm tra quyền sở hữu
        if (!nguon.dangHoatDong()) {
            throw new IllegalArgumentException("Tài khoản nguồn đang không hoạt động (đóng băng hoặc đã đóng).");
        }
        TaiKhoan nhan = taiKhoanController.traCuuNguoiNhan(soTKNhan, nguon.getMaTK());
        kiemTraHanMucNgay(nguon, soTien);
        long phi = cauHinh.tinhPhiChuyenKhoan(soTien);
        if (nguon.getSoDu() < soTien + phi) {
            throw new IllegalArgumentException("Số dư không đủ: cần " + DinhDang.tien(soTien + phi)
                    + " VND (gồm phí " + DinhDang.tien(phi) + " VND), hiện có "
                    + DinhDang.tien(nguon.getSoDu()) + " VND.");
        }
        boolean canOtp = soTien >= cauHinh.laySo(KhoaThamSo.NGUONG_OTP);
        return new XemTruocChuyenKhoan(nguon, nhan, soTien, phi, canOtp);
    }

    private void kiemTraHanMucNgay(TaiKhoan nguon, long soTien) {
        long daChuyen = giaoDichController.tongChuyenDiHomNay(nguon.getMaTK());
        long hanMuc = cauHinh.laySo(KhoaThamSo.HAN_MUC_CHUYEN_MOI_NGAY);
        if (daChuyen + soTien > hanMuc) {
            long conLai = Math.max(0, hanMuc - daChuyen);
            throw new IllegalArgumentException("Vượt hạn mức chuyển khoản trong ngày (hạn mức "
                    + DinhDang.tien(hanMuc) + " VND, đã chuyển " + DinhDang.tien(daChuyen)
                    + " VND, còn được chuyển " + DinhDang.tien(conLai) + " VND).");
        }
    }

    // ============================================================ danh bạ người nhận (khách hàng)
    public ArrayList<NguoiThuHuong> danhBa() {
        yeuCauKhach();
        return danhBaDao.theoKhachHang(phien.getMaKH());
    }

    /** Lưu người nhận vào danh bạ; tenGoiNho để trống thì dùng tên chủ tài khoản. */
    public void luuDanhBa(String soTaiKhoan, String tenGoiNho) {
        yeuCauKhach();
        String ten = tenGoiNho.trim();
        if (ten.length() > DO_DAI_TEN_GOI_NHO) {
            throw new IllegalArgumentException("Tên gợi nhớ tối đa " + DO_DAI_TEN_GOI_NHO + " ký tự.");
        }
        TaiKhoan nhan = taiKhoanController.traCuuNguoiNhan(soTaiKhoan, 0);
        String tenChu = nhan.getTenChuTaiKhoan();
        String tenLuu = ten.isEmpty() ? cat(tenChu, DO_DAI_TEN_GOI_NHO) : ten;
        int kq = danhBaDao.them(phien.getMaKH(), nhan.getSoTaiKhoan(), tenLuu, tenChu);
        if (kq == NguoiThuHuongDAO.TRUNG_DU_LIEU) {
            throw new IllegalArgumentException("Số tài khoản này đã có trong danh bạ.");
        }
        if (kq == NguoiThuHuongDAO.LOI) {
            throw new IllegalStateException("Không lưu được vào danh bạ, vui lòng thử lại.");
        }
    }

    public void xoaDanhBa(NguoiThuHuong nguoi) {
        yeuCauKhach();
        if (!danhBaDao.xoa(nguoi.getMaNTH(), phien.getMaKH())) {
            throw new IllegalStateException("Không xóa được, vui lòng thử lại.");
        }
    }

    // ============================================================ lệnh chuyển định kỳ (khách hàng)
    public ArrayList<LenhDinhKy> lenhCuaToi() {
        yeuCauKhach();
        return lenhDao.theoKhachHang(phien.getMaKH());
    }

    /** Tạo lệnh chuyển tiền định kỳ. Ngày kết thúc để trống = không kết thúc. */
    public void taoLenh(int maTKNguon, String soTKNhan, String soTienText, String noiDung,
                        String tanSuat, String ngayBatDau, String ngayKetThuc) {
        yeuCauKhach();
        long soTien = kiemTraSoTien(DinhDang.docTien(soTienText));
        String nd = chuanHoaNoiDung(noiDung);
        if (!TanSuat.hopLe(tanSuat)) {
            throw new IllegalArgumentException("Vui lòng chọn tần suất.");
        }
        LocalDate batDau = DinhDang.docNgayBatBuoc("ngày bắt đầu", ngayBatDau);
        LocalDate ketThuc = DinhDang.docNgay(ngayKetThuc);
        if (batDau.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được ở trong quá khứ.");
        }
        if (ketThuc != null && ketThuc.isBefore(batDau)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
        TaiKhoan nguon = taiKhoanController.layTheoMa(maTKNguon);
        if (!nguon.dangHoatDong()) {
            throw new IllegalArgumentException("Tài khoản nguồn đang không hoạt động.");
        }
        TaiKhoan nhan = taiKhoanController.traCuuNguoiNhan(soTKNhan, nguon.getMaTK());
        int dangDung = 0;
        for (LenhDinhKy l : lenhDao.theoKhachHang(phien.getMaKH())) {
            if (TrangThaiLenh.HOAT_DONG.equals(l.getTrangThai()) || TrangThaiLenh.TAM_DUNG.equals(l.getTrangThai())) {
                dangDung++;
            }
        }
        if (dangDung >= TOI_DA_LENH_DANG_DUNG) {
            throw new IllegalArgumentException("Mỗi khách hàng chỉ có tối đa " + TOI_DA_LENH_DANG_DUNG
                    + " lệnh định kỳ đang dùng. Hãy hủy bớt lệnh cũ.");
        }
        int ma = lenhDao.them(phien.getMaKH(), nguon.getMaTK(), nguon.getSoTaiKhoan(), nhan.getSoTaiKhoan(),
                nhan.getTenChuTaiKhoan(), soTien, nd, tanSuat, batDau, ketThuc);
        if (ma == LenhDinhKyDAO.LOI) {
            throw new IllegalStateException("Không tạo được lệnh, vui lòng thử lại.");
        }
        nhatKy.ghi("TAO_LENH_DINH_KY", "Lệnh " + ma, nguon.getSoTaiKhoan() + " → " + nhan.getSoTaiKhoan()
                + ", " + soTien + " VND, " + TanSuat.ten(tanSuat));
    }

    /** hanhDong: TAM_DUNG, HOAT_DONG (tiếp tục) hoặc DA_HUY. */
    public void doiTrangThaiLenh(LenhDinhKy lenh, String hanhDong) {
        yeuCauKhach();
        boolean hopLe = TrangThaiLenh.TAM_DUNG.equals(hanhDong) || TrangThaiLenh.HOAT_DONG.equals(hanhDong)
                || TrangThaiLenh.DA_HUY.equals(hanhDong);
        if (!hopLe) {
            throw new IllegalArgumentException("Thao tác không hợp lệ.");
        }
        if (TrangThaiLenh.HET_HAN.equals(lenh.getTrangThai()) || TrangThaiLenh.DA_HUY.equals(lenh.getTrangThai())) {
            throw new IllegalArgumentException("Lệnh đã hết hạn hoặc đã hủy, không thể thay đổi.");
        }
        if (!lenhDao.doiTrangThai(lenh.getMaLenh(), phien.getMaKH(), hanhDong)) {
            throw new IllegalStateException("Không cập nhật được lệnh, vui lòng thử lại.");
        }
        if (TrangThaiLenh.HOAT_DONG.equals(hanhDong) && lenh.getNgayChayKeTiep().isBefore(LocalDate.now())) {
            // tiếp tục sau thời gian dài: không chạy bù các kỳ đã qua, chạy từ kỳ kế tiếp
            lenhDao.datNgayChayKeTiep(lenh.getMaLenh(), phien.getMaKH(),
                    TanSuat.ngayChaySau(lenh.getNgayBatDau(), lenh.getTanSuat(), LocalDate.now().minusDays(1)));
        }
        nhatKy.ghi("DOI_TRANG_THAI_LENH", "Lệnh " + lenh.getMaLenh(), hanhDong);
    }

    // ============================================================ nhân viên: giám sát và chạy lệnh đến hạn
    public ArrayList<LenhDinhKy> tatCaLenh(String trangThai) {
        yeuCauNhanVien();
        return lenhDao.tatCa(trangThai);
    }

    /**
     * Chạy các lệnh định kỳ đã đến hạn (hệ thống thật chạy tự động mỗi ngày; ở đây nhân viên bấm chạy).
     * Lệnh nào lỗi (thiếu tiền, tài khoản nhận bị khóa...) được ghi kết quả, báo cho khách và chờ kỳ sau;
     * lỗi của một lệnh không làm hỏng các lệnh khác. Không chạy bù các kỳ đã qua.
     */
    public KetQuaChayLenh chayLenhDenHan() {
        yeuCauNhanVien();
        LocalDate homNay = LocalDate.now();
        KetQuaChayLenh kq = new KetQuaChayLenh();
        for (LenhDinhKy l : lenhDao.denHan(homNay)) {
            chayMotLenh(l, homNay, kq);
        }
        nhatKy.ghi("CHAY_LENH_DINH_KY", "Ngày " + DinhDang.ngay(homNay), kq.getSoLenh() + " lệnh: "
                + kq.getThanhCong() + " thành công, " + kq.getThatBai() + " thất bại");
        return kq;
    }

    private void chayMotLenh(LenhDinhKy l, LocalDate homNay, KetQuaChayLenh kq) {
        boolean ok;
        String ketQua;
        try {
            TaiKhoan nguon = taiKhoanController.layTheoMa(l.getMaTKNguon());
            if (!nguon.dangHoatDong()) {
                throw new IllegalArgumentException("Tài khoản nguồn đang không hoạt động.");
            }
            long soTien = kiemTraSoTien(l.getSoTien());           // hạn mức hiện hành có thể đã đổi
            TaiKhoan nhan = taiKhoanController.traCuuNguoiNhan(l.getSoTKNhan(), nguon.getMaTK());
            kiemTraHanMucNgay(nguon, soTien);
            long phi = cauHinh.tinhPhiChuyenKhoan(soTien);
            String nd = l.getNoiDung() == null || l.getNoiDung().isEmpty() ? "Chuyển tiền định kỳ" : l.getNoiDung();
            GiaoDich di = giaoDichController.chuyenNoiBo(nguon, nhan, soTien, phi, nd, KenhGD.HE_THONG);
            ok = true;
            ketQua = "Thành công " + di.getMaThamChieu() + " ngày " + DinhDang.ngay(homNay);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ok = false;
            ketQua = "Thất bại: " + ex.getMessage();
            thongBao.guiChoKhach(l.getMaKH(), "Chuyển tiền định kỳ không thực hiện được",
                    "Lệnh " + l.getMaLenh() + " (" + l.getSoTKNguon() + " → " + l.getSoTKNhan() + ", "
                    + DinhDang.tien(l.getSoTien()) + " VND) chưa thực hiện được: " + ex.getMessage());
        }
        LocalDate ke = TanSuat.ngayChaySau(l.getNgayBatDau(), l.getTanSuat(), homNay);
        String trangThai = l.getNgayKetThuc() != null && ke.isAfter(l.getNgayKetThuc())
                ? TrangThaiLenh.HET_HAN : TrangThaiLenh.HOAT_DONG;
        lenhDao.capNhatSauChay(l.getMaLenh(), ke, trangThai, cat(ketQua, 200), ok);
        kq.them(ok, "Lệnh " + l.getMaLenh() + " (" + l.getSoTKNguon() + " → " + l.getSoTKNhan() + "): " + ketQua);
    }

    // ============================================================ quyền, tiện ích
    private void yeuCauKhach() {
        if (!phien.laKhach() || phien.getMaKH() == null) {
            throw new IllegalStateException("Chức năng này chỉ dành cho khách hàng.");
        }
    }

    private void yeuCauNhanVien() {
        if (!phien.laNhanVien()) {
            throw new IllegalStateException("Chỉ nhân viên ngân hàng mới dùng được chức năng này.");
        }
    }

    private static String cat(String s, int toiDa) {
        return s == null || s.length() <= toiDa ? s : s.substring(0, toiDa);
    }
}
