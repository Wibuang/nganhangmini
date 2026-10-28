package module.taikhoan;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.pnlNhieuTab;
import javax.swing.JComponent;
import module.taikhoan.view.pnlQuanLyTaiKhoan;
import module.taikhoan.view.pnlTaiKhoanCuaToi;

/** Module 3 - Tài khoản: nhân viên mở/đóng băng/đóng; khách hàng xem các tài khoản của mình. */
public class TaiKhoanModule implements IModule {

    @Override
    public String ten(PhienDangNhap phien) {
        return "Tài khoản";
    }

    @Override
    public boolean choPhep(PhienDangNhap phien) {
        return true;
    }

    @Override
    public JComponent taoManHinh(PhienDangNhap phien) {
        pnlNhieuTab man = new pnlNhieuTab();
        if (phien.laKhach()) {
            man.them("Tài khoản của tôi", new pnlTaiKhoanCuaToi(phien));
        } else {
            man.them("Quản lý tài khoản", new pnlQuanLyTaiKhoan(phien));
        }
        return man;
    }
}
