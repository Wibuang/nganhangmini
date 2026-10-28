package module.khachhang;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.pnlNhieuTab;
import javax.swing.JComponent;
import module.khachhang.view.pnlHoSoCuaToi;
import module.khachhang.view.pnlQuanLyKhachHang;

/** Module 2 - Khách hàng (KYC): nhân viên quản lý hồ sơ; khách hàng xem và cập nhật hồ sơ của mình. */
public class KhachHangModule implements IModule {

    @Override
    public String ten(PhienDangNhap phien) {
        return phien.laKhach() ? "Hồ sơ" : "Khách hàng";
    }

    @Override
    public boolean choPhep(PhienDangNhap phien) {
        return true;
    }

    @Override
    public JComponent taoManHinh(PhienDangNhap phien) {
        pnlNhieuTab man = new pnlNhieuTab();
        if (phien.laKhach()) {
            man.them("Hồ sơ của tôi", new pnlHoSoCuaToi(phien));
        } else {
            man.them("Quản lý khách hàng", new pnlQuanLyKhachHang(phien));
        }
        return man;
    }
}
