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
import module.cauhinh.model.BieuPhi;

/** Biểu phí chuyển khoản theo bậc số tiền; quản trị viên sửa mức phí của từng bậc. */
public class pnlBieuPhi extends JPanel implements CoTheLamMoi {
    private final CauHinhController controller;
    private ArrayList<BieuPhi> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel =
            UI.moHinhBang("Bậc", "Từ số tiền (VND)", "Đến số tiền (VND)", "Phí (VND)");
    private final JTable tbl = new JTable(tableModel);

    public pnlBieuPhi(PhienDangNhap phien) {
        this.controller = new CauHinhController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 60, 200, 200, 120);
        UI.canPhai(tbl, 1, 2, 3);

        JButton btnSua = new JButton("Sửa phí của bậc đã chọn");
        btnSua.addActionListener(e -> sua());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnSua);

        add(new JLabel("Phí chuyển khoản trừ vào tài khoản người gửi, tính theo bậc chứa số tiền chuyển."),
                BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.layBieuPhiChuyenKhoan();
        tableModel.setRowCount(0);
        int bac = 1;
        for (BieuPhi b : dsHienTai) {
            tableModel.addRow(new Object[]{bac++, DinhDang.tien(b.getTuSoTien()),
                b.getDenSoTien() == null ? "Không giới hạn" : DinhDang.tien(b.getDenSoTien()),
                DinhDang.tien(b.getPhi())});
        }
    }

    private void sua() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một bậc phí trong bảng.");
            return;
        }
        BieuPhi b = dsHienTai.get(dong);
        String phi = UI.hoi(this, "Phí mới (VND) cho bậc từ " + DinhDang.tien(b.getTuSoTien())
                + "\nPhí hiện tại: " + DinhDang.tien(b.getPhi()));
        if (phi == null) {
            return;
        }
        try {
            controller.capNhatPhi(b, phi);
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
