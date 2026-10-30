package module.cauhinh.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.cauhinh.controller.CauHinhController;
import module.cauhinh.model.LaiSuat;

/** Lãi suất tiết kiệm và khoản vay theo kỳ hạn (chuẩn bị cho các module ở đợt sau). */
public class pnlLaiSuat extends JPanel implements CoTheLamMoi {
    private final CauHinhController controller;
    private ArrayList<LaiSuat> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel =
            UI.moHinhBang("Loại", "Kỳ hạn", "Lãi suất (%/năm)");
    private final JTable tbl = new JTable(tableModel);

    public pnlLaiSuat(PhienDangNhap phien) {
        this.controller = new CauHinhController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 160, 160, 160);
        UI.canPhai(tbl, 2);

        JButton btnSua = new JButton("Sửa lãi suất đã chọn");
        btnSua.addActionListener(e -> sua());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnSua);

        add(new JLabel("Các mức lãi suất này sẽ được module Tiết kiệm và Khoản vay sử dụng ở đợt sau."),
                BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.layLaiSuat();
        tableModel.setRowCount(0);
        for (LaiSuat l : dsHienTai) {
            tableModel.addRow(new Object[]{
                LaiSuat.TIET_KIEM.equals(l.getLoaiLai()) ? "Tiết kiệm" : "Vay",
                l.getKyHanThang() == 0 ? "Không kỳ hạn" : l.getKyHanThang() + " tháng",
                String.format(Locale.ROOT, "%.2f", l.getLaiSuatNam())});
        }
    }

    private void sua() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một dòng trong bảng.");
            return;
        }
        LaiSuat l = dsHienTai.get(dong);
        String moi = UI.hoi(this, "Lãi suất mới (%/năm, từ 0 đến 30)\nHiện tại: "
                + String.format(Locale.ROOT, "%.2f", l.getLaiSuatNam()));
        if (moi == null) {
            return;
        }
        try {
            controller.capNhatLaiSuat(l, moi);
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
