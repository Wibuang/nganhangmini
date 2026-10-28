package module.giaodich.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import module.giaodich.controller.GiaoDichController;
import module.giaodich.model.GiaoDich;
import module.giaodich.model.KenhGD;
import module.giaodich.model.LoaiGD;
import module.giaodich.model.SaoKe;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.view.cboTaiKhoan;

/**
 * Sao kê một tài khoản theo kỳ: số dư đầu kỳ, tổng tiền vào, tổng tiền ra, số dư cuối kỳ và chi tiết.
 * Khách hàng chọn tài khoản của mình; nhân viên nhập số tài khoản.
 */
public class pnlSaoKe extends JPanel implements CoTheLamMoi {
    private final PhienDangNhap phien;
    private final GiaoDichController controller;
    private final TaiKhoanController taiKhoanController;

    private final cboTaiKhoan cboTK = new cboTaiKhoan();
    private final JTextField txtSoTK = new JTextField(14);
    private final JTextField txtTu = new JTextField(9);
    private final JTextField txtDen = new JTextField(9);

    private final JLabel lblChu = new JLabel(" ");
    private final JLabel lblDau = new JLabel(" ");
    private final JLabel lblVao = new JLabel(" ");
    private final JLabel lblRa = new JLabel(" ");
    private final JLabel lblCuoi = new JLabel(" ");
    private final DefaultTableModel tableModel =
            UI.moHinhBang("Mã GD", "Thời gian", "Loại", "Số tiền (VND)", "Số dư sau", "Nội dung");
    private SaoKe saoKeHienTai;

    public pnlSaoKe(PhienDangNhap phien) {
        this.phien = phien;
        this.controller = new GiaoDichController(phien);
        this.taiKhoanController = new TaiKhoanController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        LocalDate homNay = LocalDate.now();
        txtTu.setText(DinhDang.ngay(homNay.withDayOfMonth(1)));   // mặc định: từ đầu tháng
        txtDen.setText(DinhDang.ngay(homNay));

        JButton btnXem = new JButton("Xem sao kê");
        JButton btnXuat = new JButton("Xuất CSV");
        btnXem.addActionListener(e -> xem());
        btnXuat.addActionListener(e -> xuat());

        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        if (phien.laKhach()) {
            pnlLoc.add(new JLabel("Tài khoản:"));
            pnlLoc.add(cboTK);
        } else {
            pnlLoc.add(new JLabel("Số tài khoản:"));
            pnlLoc.add(txtSoTK);
        }
        pnlLoc.add(new JLabel("Từ ngày:"));
        pnlLoc.add(txtTu);
        pnlLoc.add(new JLabel("Đến ngày:"));
        pnlLoc.add(txtDen);
        pnlLoc.add(btnXem);
        pnlLoc.add(btnXuat);

        lblCuoi.setFont(UI.dam(14));
        JPanel pnlTongKet = UI.bieuMau(
                new String[]{"Chủ tài khoản:", "Số dư đầu kỳ (VND):", "Tổng tiền vào (VND):",
                             "Tổng tiền ra (VND):", "Số dư cuối kỳ (VND):"},
                new JComponent[]{lblChu, lblDau, lblVao, lblRa, lblCuoi});
        UI.coTieuDe(pnlTongKet, "Tổng kết kỳ sao kê");

        JTable tbl = new JTable(tableModel);
        tbl.setRowHeight(24);
        UI.rongCot(tbl, 90, 150, 90, 110, 110, 300);
        UI.mauTien(tbl, 3);
        UI.canPhai(tbl, 4);

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.add(pnlLoc, BorderLayout.NORTH);
        pnlTren.add(pnlTongKet, BorderLayout.CENTER);
        add(pnlTren, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
    }

    @Override
    public void lamMoi() {
        if (phien.laKhach()) {
            cboTK.napLai(taiKhoanController.taiKhoanCuaToi(false));
        }
    }

    private void xem() {
        try {
            int maTK;
            if (phien.laKhach()) {
                TaiKhoan tk = cboTK.getChon();
                if (tk == null) {
                    UI.loi(this, "Bạn chưa có tài khoản nào.");
                    return;
                }
                maTK = tk.getMaTK();
            } else {
                maTK = taiKhoanController.layTheoSo(txtSoTK.getText()).getMaTK();
            }
            saoKeHienTai = controller.saoKe(maTK, txtTu.getText(), txtDen.getText());
            hienThi(saoKeHienTai);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }

    private void hienThi(SaoKe sk) {
        lblChu.setText(sk.getTenChuTaiKhoan() + "  (STK " + sk.getSoTaiKhoan() + ")");
        lblDau.setText(DinhDang.tien(sk.getSoDuDau()));
        lblVao.setText("+" + DinhDang.tien(sk.getTongVao()));
        lblRa.setText("-" + DinhDang.tien(sk.getTongRa()));
        lblCuoi.setText(DinhDang.tien(sk.getSoDuCuoi()));
        tableModel.setRowCount(0);
        for (GiaoDich g : sk.getDanhSach()) {
            String dau = LoaiGD.laTienVao(g.getLoaiGD()) ? "+" : "-";
            tableModel.addRow(new Object[]{
                g.getMaThamChieu(), DinhDang.ngayGio(g.getNgayGD()), LoaiGD.tenHienThi(g.getLoaiGD()),
                dau + DinhDang.tien(g.getSoTien()), DinhDang.tien(g.getSoDuSau()),
                g.getGhiChu() == null ? "" : g.getGhiChu() + " [" + KenhGD.ten(g.getKenh()) + "]"});
        }
    }

    private void xuat() {
        if (saoKeHienTai == null) {
            UI.thongBao(this, "Hãy bấm \"Xem sao kê\" trước khi xuất.");
            return;
        }
        File f = UI.chonFileCsv(this, "saoke_" + saoKeHienTai.getSoTaiKhoan() + ".csv");
        if (f == null) {
            return;
        }
        try {
            controller.xuatSaoKe(saoKeHienTai, f);
            UI.thongBao(this, "Đã xuất sao kê ra file:\n" + f.getAbsolutePath());
        } catch (IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
