package module.giaodich;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.pnlNhieuTab;
import javax.swing.JComponent;
import module.giaodich.view.pnlLichSu;
import module.giaodich.view.pnlNapRutKhach;
import module.giaodich.view.pnlNapRutQuay;
import module.giaodich.view.pnlSaoKe;

/** Module 4 - Giao dịch & Sổ cái: nạp/rút, lịch sử, sao kê, đảo giao dịch. */
public class GiaoDichModule implements IModule {

    @Override
    public String ten(PhienDangNhap phien) {
        return "Giao dịch";
    }

    @Override
    public boolean choPhep(PhienDangNhap phien) {
        return true;
    }

    @Override
    public JComponent taoManHinh(PhienDangNhap phien) {
        pnlNhieuTab man = new pnlNhieuTab();
        if (phien.laKhach()) {
            man.them("Nạp / Rút", new pnlNapRutKhach(phien));
            man.them("Lịch sử", new pnlLichSu(phien));
            man.them("Sao kê", new pnlSaoKe(phien));
        } else {
            man.them("Nạp / Rút tại quầy", new pnlNapRutQuay(phien));
            man.them("Tra cứu giao dịch", new pnlLichSu(phien));
            man.them("Sao kê", new pnlSaoKe(phien));
        }
        return man;
    }
}
