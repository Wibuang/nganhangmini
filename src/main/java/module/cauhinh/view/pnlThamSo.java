package module.cauhinh.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.cauhinh.controller.CauHinhController;
import module.cauhinh.model.KhoaThamSo;
import module.cauhinh.model.ThamSo;

/** Quản trị viên xem và sửa các tham số hệ thống (hạn mức, số lần sai, tuổi tối thiểu, OTP...). */
public class pnlThamSo extends JPanel implements CoTheLamMoi {
    private final CauHinhController controller;
    private ArrayList<ThamSo> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel = UI.moHinhBang("Mã tham số", "Mô tả", "Giá trị", "Đơn vị");
    private final JTable tbl = new JTable(tableModel);

    public pnlThamSo(PhienDangNhap phien) {
        this.controller = new CauHinhController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 200, 420, 120, 80);
        UI.canPhai(tbl, 2);

        JButton btnSua = new JButton("Sửa giá trị");
        JButton btnLamMoi = new JButton("Làm mới");
        btnSua.addActionListener(e -> sua());
        btnLamMoi.addActionListener(e -> lamMoi());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnSua);
        pnlNut.add(btnLamMoi);

        add(new JLabel("Thay đổi có hiệu lực ngay với giao dịch tiếp theo (riêng thời gian hết phiên áp dụng từ lần đăng nhập sau)."),
                BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.layTatCaThamSo();
        tableModel.setRowCount(0);
        for (ThamSo t : dsHienTai) {
            tableModel.addRow(new Object[]{t.getMa(), t.getMoTa(), DinhDang.tien(t.getGiaTri()),
                t.getDonVi() == null ? "" : t.getDonVi()});
        }
    }

    private void sua() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một tham số trong bảng.");
            return;
        }
        ThamSo t = dsHienTai.get(dong);
        String khoang = KhoaThamSo.tonTai(t.getMa())
                ? "\n(cho phép từ " + DinhDang.tien(KhoaThamSo.nhoNhat(t.getMa()))
                  + " đến " + DinhDang.tien(KhoaThamSo.lonNhat(t.getMa())) + ")" : "";
        String giaTri = UI.hoi(this, t.getMoTa() + khoang + "\nGiá trị hiện tại: " + DinhDang.tien(t.getGiaTri())
                + "\nGiá trị mới:");
        if (giaTri == null) {
            return;
        }
        try {
            controller.capNhatThamSo(t.getMa(), giaTri);
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
