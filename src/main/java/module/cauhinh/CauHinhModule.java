package module.cauhinh;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.pnlNhieuTab;
import javax.swing.JComponent;
import module.cauhinh.view.pnlBieuPhi;
import module.cauhinh.view.pnlLaiSuat;
import module.cauhinh.view.pnlThamSo;

/** Module 9 - Cấu hình: tham số hệ thống, biểu phí, lãi suất. Chỉ quản trị viên được thấy. */
public class CauHinhModule implements IModule {

    @Override
    public String ten(PhienDangNhap phien) {
        return "Cấu hình";
    }

    @Override
    public boolean choPhep(PhienDangNhap phien) {
        return phien.laAdmin();
    }

    @Override
    public JComponent taoManHinh(PhienDangNhap phien) {
        pnlNhieuTab man = new pnlNhieuTab();
        man.them("Tham số hệ thống", new pnlThamSo(phien));
        man.them("Biểu phí chuyển khoản", new pnlBieuPhi(phien));
        man.them("Lãi suất", new pnlLaiSuat(phien));
        return man;
    }
}
