package module.baocao.view;

import core.phien.PhienDangNhap;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import module.baocao.controller.BaoCaoController;
import module.baocao.model.DongBaoCao;

/** Báo cáo giao dịch theo ngày hoặc theo tháng, có tổng cộng và xuất CSV. */
public class pnlBaoCaoGiaoDich extends JPanel {
    private final BaoCaoController controller;
    private ArrayList<DongBaoCao> dsHienTai = new ArrayList<>();
    private boolean dangTheoThang;

    private final JTextField txtTu = new JTextField(9);
    private final JTextField txtDen = new JTextField(9);
    private final JComboBox<String> cboKy = new JComboBox<>(new String[]{"Theo ngày", "Theo tháng"});
    private final DefaultTableModel tableModel = UI.moHinhBang("Kỳ", "Số giao dịch", "Tổng nạp",
            "Tổng rút", "Tổng chuyển khoản", "Phí thu", "Hoàn giao dịch");
    private final JLabel lblTong = new JLabel(" ");

    public pnlBaoCaoGiaoDich(PhienDangNhap phien) {
        this.controller = new BaoCaoController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        LocalDate homNay = LocalDate.now();
        txtTu.setText(DinhDang.ngay(homNay.withDayOfMonth(1)));
        txtDen.setText(DinhDang.ngay(homNay));

        JButton btnXem = new JButton("Xem báo cáo");
        JButton btnXuat = new JButton("Xuất CSV");
        btnXem.addActionListener(e -> xem());
        btnXuat.addActionListener(e -> xuat());
        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlLoc.add(new JLabel("Từ ngày:"));
        pnlLoc.add(txtTu);
        pnlLoc.add(new JLabel("Đến ngày:"));
        pnlLoc.add(txtDen);
        pnlLoc.add(cboKy);
        pnlLoc.add(btnXem);
        pnlLoc.add(btnXuat);

        JTable bang = new JTable(tableModel);
        bang.setRowHeight(24);
        UI.rongCot(bang, 110, 100, 130, 130, 150, 100, 120);
        UI.canPhai(bang, 1, 2, 3, 4, 5, 6);

        lblTong.setFont(UI.dam(14));
        add(pnlLoc, BorderLayout.NORTH);
        add(new JScrollPane(bang), BorderLayout.CENTER);
        add(lblTong, BorderLayout.SOUTH);
    }

    private void xem() {
        try {
            dangTheoThang = cboKy.getSelectedIndex() == 1;
            dsHienTai = controller.baoCao(txtTu.getText(), txtDen.getText(), dangTheoThang);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
            return;
        }
        tableModel.setRowCount(0);
        int soGD = 0;
        long nap = 0;
        long rut = 0;
        long chuyen = 0;
        long phi = 0;
        long hoan = 0;
        for (DongBaoCao d : dsHienTai) {
            tableModel.addRow(new Object[]{d.getKy(), d.getSoGiaoDich(), DinhDang.tien(d.getTongNap()),
                DinhDang.tien(d.getTongRut()), DinhDang.tien(d.getTongChuyen()),
                DinhDang.tien(d.getTongPhi()), DinhDang.tien(d.getTongHoan())});
            soGD += d.getSoGiaoDich();
            nap += d.getTongNap();
            rut += d.getTongRut();
            chuyen += d.getTongChuyen();
            phi += d.getTongPhi();
            hoan += d.getTongHoan();
        }
        lblTong.setText("Tổng cộng: " + soGD + " giao dịch | Nạp " + DinhDang.tien(nap)
                + " | Rút " + DinhDang.tien(rut) + " | Chuyển khoản " + DinhDang.tien(chuyen)
                + " | Phí " + DinhDang.tien(phi) + " | Hoàn " + DinhDang.tien(hoan) + " (VND)");
    }

    private void xuat() {
        if (dsHienTai.isEmpty()) {
            UI.thongBao(this, "Hãy bấm \"Xem báo cáo\" và có dữ liệu trước khi xuất.");
            return;
        }
        File f = UI.chonFileCsv(this, "baocao_giaodich.csv");
        if (f == null) {
            return;
        }
        try {
            controller.xuatBaoCao(dsHienTai, dangTheoThang, f);
            UI.thongBao(this, "Đã xuất báo cáo ra file:\n" + f.getAbsolutePath());
        } catch (IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
