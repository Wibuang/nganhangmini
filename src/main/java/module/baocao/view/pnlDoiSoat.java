package module.baocao.view;

import core.phien.PhienDangNhap;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import module.baocao.controller.BaoCaoController;
import module.baocao.model.ChenhLech;
import module.baocao.model.DoiSoat;

/** Đối soát: tổng số dư tài khoản phải bằng tổng của sổ cái, và từng tài khoản phải khớp giao dịch cuối. */
public class pnlDoiSoat extends JPanel {
    private final BaoCaoController controller;

    private final JLabel lblSoTK = new JLabel(" ");
    private final JLabel lblTongTK = new JLabel(" ");
    private final JLabel lblTongSoCai = new JLabel(" ");
    private final JLabel lblKetQua = new JLabel("Chưa chạy đối soát");
    private final DefaultTableModel tableModel =
            UI.moHinhBang("Số tài khoản", "Số dư tài khoản", "Số dư theo sổ cái", "Chênh lệch");

    public pnlDoiSoat(PhienDangNhap phien) {
        this.controller = new BaoCaoController(phien);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        lblKetQua.setFont(UI.dam(18));
        JPanel pnlTren = UI.bieuMau(
                new String[]{"Số tài khoản đã đối soát:", "Tổng số dư các tài khoản (VND):",
                             "Tổng theo sổ cái: vào − ra (VND):", "Kết quả:"},
                new JComponent[]{lblSoTK, lblTongTK, lblTongSoCai, lblKetQua});
        UI.coTieuDe(pnlTren, "Kết quả đối soát");

        JTable bang = new JTable(tableModel);
        bang.setRowHeight(24);
        UI.rongCot(bang, 160, 160, 160, 160);
        UI.canPhai(bang, 1, 2, 3);
        JScrollPane pnlLech = new JScrollPane(bang);
        pnlLech.setBorder(BorderFactory.createTitledBorder("Tài khoản bị lệch (nếu có)"));

        JButton btnChay = new JButton("Chạy đối soát");
        btnChay.addActionListener(e -> chay());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlNut.add(btnChay);

        add(pnlTren, BorderLayout.NORTH);
        add(pnlLech, BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
    }

    private void chay() {
        try {
            DoiSoat ds = controller.doiSoat();
            lblSoTK.setText(String.valueOf(ds.getSoTaiKhoan()));
            lblTongTK.setText(DinhDang.tien(ds.getTongSoDuTaiKhoan()));
            lblTongSoCai.setText(DinhDang.tien(ds.getTongSoCai()));
            lblKetQua.setText(ds.khop() ? "KHỚP" : "LỆCH - cần kiểm tra ngay");
            lblKetQua.setForeground(ds.khop() ? UI.XANH : UI.DO);
            tableModel.setRowCount(0);
            for (ChenhLech c : ds.getTaiKhoanLech()) {
                tableModel.addRow(new Object[]{c.getSoTaiKhoan(), DinhDang.tien(c.getSoDuTaiKhoan()),
                    DinhDang.tien(c.getSoDuSoCai()), DinhDang.tien(c.getChenhLech())});
            }
        } catch (IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
