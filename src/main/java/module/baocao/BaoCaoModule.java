package module.baocao;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.pnlNhieuTab;
import javax.swing.JComponent;
import module.baocao.view.pnlBaoCaoGiaoDich;
import module.baocao.view.pnlDoiSoat;
import module.baocao.view.pnlNhatKy;
import module.baocao.view.pnlThongBao;
import module.baocao.view.pnlTongQuan;


/**
 * Module 10 - Báo cáo, Nhật ký & Thông báo.
 * Khách hàng: thông báo. Giao dịch viên: báo cáo giao dịch.
 * Quản trị viên: tổng quan, báo cáo giao dịch, đối soát, nhật ký kiểm toán.
 */
public class BaoCaoModule implements IModule {

    @Override
    public String ten(PhienDangNhap phien) {
        return phien.laKhach() ? "Thông báo" : "Báo cáo";
    }

    @Override
    public boolean choPhep(PhienDangNhap phien) {
        return true;
    }

    @Override
    public JComponent taoManHinh(PhienDangNhap phien) {
        pnlNhieuTab man = new pnlNhieuTab();
        if (phien.laKhach()) {
            man.them("Thông báo của tôi", new pnlThongBao(phien));
        } else if (phien.laAdmin()) {
            man.them("Tổng quan", new pnlTongQuan(phien));
            man.them("Báo cáo giao dịch", new pnlBaoCaoGiaoDich(phien));
            man.them("Đối soát", new pnlDoiSoat(phien));
            man.them("Nhật ký", new pnlNhatKy(phien));
        } else {
            man.them("Báo cáo giao dịch", new pnlBaoCaoGiaoDich(phien));
        }
        return man;
    }
}
