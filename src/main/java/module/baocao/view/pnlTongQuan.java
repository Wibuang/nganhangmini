package module.baocao.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import module.baocao.controller.BaoCaoController;
import module.baocao.model.DongTopSoDu;
import module.baocao.model.ThongKe;

/** Tổng quan hệ thống cho quản trị viên: số liệu chính và top 5 số dư. */
public class pnlTongQuan extends JPanel implements CoTheLamMoi {
    private final BaoCaoController controller;

    private final JLabel lblKhachHang = new JLabel();
    private final JLabel lblTaiKhoan = new JLabel();
    private final JLabel lblTongSoDu = new JLabel();
    private final JLabel lblGDHomNay = new JLabel();
    private final JLabel lblNap = new JLabel();
    private final JLabel lblRut = new JLabel();
    private final JLabel lblChuyen = new JLabel();
    private final JLabel lblPhi = new JLabel();
    private final DefaultTableModel tableModel =
            UI.moHinhBang("Hạng", "Số tài khoản", "Chủ tài khoản", "Số dư (VND)");

    public pnlTongQuan(PhienDangNhap phien) {
        this.controller = new BaoCaoController(phien);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        lblTongSoDu.setFont(UI.dam(15));
        lblTongSoDu.setForeground(UI.XANH);
        JPanel pnlSoLieu = UI.bieuMau(
                new String[]{"Tổng số khách hàng:", "Tài khoản (hoạt động / tổng):",
                             "Tổng số dư toàn hệ thống (VND):", "Số giao dịch hôm nay:",
                             "Tổng tiền nạp hôm nay (VND):", "Tổng tiền rút hôm nay (VND):",
                             "Tổng tiền chuyển khoản hôm nay (VND):", "Phí thu hôm nay (VND):"},
                new JComponent[]{lblKhachHang, lblTaiKhoan, lblTongSoDu, lblGDHomNay,
                                 lblNap, lblRut, lblChuyen, lblPhi});
        UI.coTieuDe(pnlSoLieu, "Tổng quan hệ thống");

        JTable bang = new JTable(tableModel);
        bang.setRowHeight(24);
        UI.rongCot(bang, 60, 160, 260, 160);
        UI.canPhai(bang, 3);
        JScrollPane pnlTop = new JScrollPane(bang);
        pnlTop.setBorder(BorderFactory.createTitledBorder("Top 5 tài khoản có số dư cao nhất"));

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(e -> lamMoi());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlNut.add(btnLamMoi);

        add(pnlSoLieu, BorderLayout.NORTH);
        add(pnlTop, BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        ThongKe tk = controller.thongKe();
        lblKhachHang.setText(String.valueOf(tk.getTongKhachHang()));
        lblTaiKhoan.setText(tk.getTaiKhoanHoatDong() + " / " + tk.getTongTaiKhoan());
        lblTongSoDu.setText(DinhDang.tien(tk.getTongSoDu()));
        lblGDHomNay.setText(String.valueOf(tk.getGiaoDichHomNay()));
        lblNap.setText(DinhDang.tien(tk.getNapHomNay()));
        lblRut.setText(DinhDang.tien(tk.getRutHomNay()));
        lblChuyen.setText(DinhDang.tien(tk.getChuyenHomNay()));
        lblPhi.setText(DinhDang.tien(tk.getPhiHomNay()));

        tableModel.setRowCount(0);
        ArrayList<DongTopSoDu> top = controller.topSoDu(5);
        for (int i = 0; i < top.size(); i++) {
            DongTopSoDu d = top.get(i);
            tableModel.addRow(new Object[]{i + 1, d.getSoTaiKhoan(), d.getTenChuTaiKhoan(),
                DinhDang.tien(d.getSoDu())});
        }
    }
}
