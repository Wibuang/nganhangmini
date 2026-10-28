package module.giaodich.view;

import core.ui.UI;
import core.util.DinhDang;
import java.awt.Component;
import module.giaodich.model.GiaoDich;
import module.giaodich.model.LoaiGD;

/** Hộp thoại biên lai sau mỗi giao dịch. */
public final class BienLai {
    private BienLai() {}

    public static void hienThi(Component cha, String tieuDe, GiaoDich g) {
        UI.thongBao(cha, tieuDe + "\n\n"
                + "Mã giao dịch: " + g.getMaThamChieu() + "\n"
                + "Loại: " + LoaiGD.tenHienThi(g.getLoaiGD()) + "\n"
                + "Tài khoản: " + g.getSoTaiKhoan() + "\n"
                + "Số tiền: " + DinhDang.tien(g.getSoTien()) + " VND\n"
                + "Số dư sau giao dịch: " + DinhDang.tien(g.getSoDuSau()) + " VND\n"
                + "Thời gian: " + DinhDang.ngayGio(g.getNgayGD()));
    }
}
