package module.xacthuc;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.pnlNhieuTab;
import javax.swing.JComponent;
import module.xacthuc.view.pnlDoiMatKhau;
import module.xacthuc.view.pnlNguoiDung;

/** Module 1 - Xác thực & Phân quyền: đổi mật khẩu (mọi vai trò), quản lý người dùng (admin). */
public class XacThucModule implements IModule {

    @Override
    public String ten(PhienDangNhap phien) {
        return "Bảo mật";
    }

    @Override
    public boolean choPhep(PhienDangNhap phien) {
        return true;
    }

    @Override
    public JComponent taoManHinh(PhienDangNhap phien) {
        pnlNhieuTab man = new pnlNhieuTab();
        man.them("Đổi mật khẩu", new pnlDoiMatKhau(phien));
        if (phien.laAdmin()) {
            man.them("Người dùng", new pnlNguoiDung(phien));
        }
        return man;
    }
}
