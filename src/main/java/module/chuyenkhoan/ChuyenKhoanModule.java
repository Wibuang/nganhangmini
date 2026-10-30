package module.chuyenkhoan;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.pnlNhieuTab;
import javax.swing.JComponent;
import module.chuyenkhoan.view.pnlChuyenKhoan;
import module.chuyenkhoan.view.pnlDanhBa;
import module.chuyenkhoan.view.pnlLenhDinhKy;
import module.chuyenkhoan.view.pnlLenhDinhKyQuanTri;

/**
 * Module 5 - Chuyển khoản. Khách hàng: chuyển khoản, danh bạ người nhận, lệnh định kỳ.
 * Nhân viên: giám sát và chạy các lệnh định kỳ đến hạn.
 */
public class ChuyenKhoanModule implements IModule {

    @Override
    public String ten(PhienDangNhap phien) {
        return "Chuyển khoản";
    }

    @Override
    public boolean choPhep(PhienDangNhap phien) {
        return true;
    }

    @Override
    public JComponent taoManHinh(PhienDangNhap phien) {
        pnlNhieuTab man = new pnlNhieuTab();
        if (phien.laKhach()) {
            man.them("Chuyển khoản", new pnlChuyenKhoan(phien));
            man.them("Danh bạ người nhận", new pnlDanhBa(phien));
            man.them("Lệnh định kỳ", new pnlLenhDinhKy(phien));
        } else {
            man.them("Lệnh định kỳ", new pnlLenhDinhKyQuanTri(phien));
        }
        return man;
    }
}
