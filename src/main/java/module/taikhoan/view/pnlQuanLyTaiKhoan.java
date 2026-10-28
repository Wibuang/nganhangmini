package module.taikhoan.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.khachhang.model.KhachHang;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.model.TrangThaiTK;

/** Màn hình của nhân viên: tìm tài khoản, mở tài khoản mới, đóng băng / mở băng, đóng tài khoản. */
public class pnlQuanLyTaiKhoan extends JPanel implements CoTheLamMoi {
    private final TaiKhoanController controller;
    private final PhienDangNhap phien;
    private ArrayList<TaiKhoan> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel = UI.moHinhBang(
            "Mã TK", "Số tài khoản", "Chủ tài khoản", "Tên gợi nhớ", "Số dư (VND)", "Trạng thái", "Ngày mở");
    private final JTable tbl = new JTable(tableModel);
    private final JTextField txtTim = new JTextField(22);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(TrangThaiTK.TEN);
    private final JLabel lblTong = new JLabel(" ");

    public pnlQuanLyTaiKhoan(PhienDangNhap phien) {
        this.phien = phien;
        this.controller = new TaiKhoanController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnLamMoi = new JButton("Làm mới");
        btnTim.addActionListener(e -> lamMoi());
        txtTim.addActionListener(e -> lamMoi());
        btnLamMoi.addActionListener(e -> {
            txtTim.setText("");
            cboTrangThai.setSelectedIndex(0);
            lamMoi();
        });
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTim.add(new JLabel("Tìm theo số TK / tên / CCCD / SĐT:"));
        pnlTim.add(txtTim);
        pnlTim.add(new JLabel("Trạng thái:"));
        pnlTim.add(cboTrangThai);
        pnlTim.add(btnTim);
        pnlTim.add(btnLamMoi);

        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 50, 110, 160, 160, 120, 90, 90);
        UI.canPhai(tbl, 4);

        JButton btnMo = new JButton("Mở tài khoản mới");
        JButton btnBang = new JButton("Đóng băng / Mở băng");
        JButton btnDong = new JButton("Đóng tài khoản");
        btnMo.addActionListener(e -> moTaiKhoan());
        btnBang.addActionListener(e -> doiDongBang());
        btnDong.addActionListener(e -> dongTaiKhoan());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnMo);
        pnlNut.add(btnBang);
        pnlNut.add(btnDong);
        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.add(lblTong, BorderLayout.NORTH);
        pnlDuoi.add(pnlNut, BorderLayout.CENTER);

        add(pnlTim, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlDuoi, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.timKiem(txtTim.getText(), TrangThaiTK.MA[cboTrangThai.getSelectedIndex()]);
        tableModel.setRowCount(0);
        for (TaiKhoan tk : dsHienTai) {
            tableModel.addRow(new Object[]{
                tk.getMaTK(), tk.getSoTaiKhoan(), tk.getTenChuTaiKhoan(), tk.getTenGoiNho(),
                DinhDang.tien(tk.getSoDu()), TrangThaiTK.ten(tk.getTrangThai()),
                DinhDang.ngay(tk.getNgayMo())});
        }
        lblTong.setText("Tổng: " + dsHienTai.size() + " tài khoản");
    }

    private TaiKhoan dangChon() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một dòng trong bảng.");
            return null;
        }
        return dsHienTai.get(dong);
    }

    private void moTaiKhoan() {
        JTextField txtCCCD = new JTextField(16);
        JTextField txtTen = new JTextField();
        JPanel form = UI.bieuMau(
                new String[]{"CCCD của khách hàng:", "Tên gợi nhớ (có thể để trống):"},
                new JComponent[]{txtCCCD, txtTen});
        while (true) {
            int chon = JOptionPane.showConfirmDialog(this, form, "Mở tài khoản mới",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (chon != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                TaiKhoan tk = controller.moTaiKhoan(txtCCCD.getText(), txtTen.getText());
                lamMoi();
                UI.thongBao(this, "Đã mở tài khoản " + tk.getSoTaiKhoan() + " cho "
                        + tk.getTenChuTaiKhoan() + ".\nSố dư ban đầu 0 VND; hãy nạp tiền ở tab Giao dịch.");
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                UI.loi(this, ex.getMessage());
            }
        }
    }

    private void doiDongBang() {
        TaiKhoan tk = dangChon();
        if (tk == null) {
            return;
        }
        String hanhDong = tk.dangHoatDong() ? "ĐÓNG BĂNG" : "MỞ BĂNG";
        if (!UI.xacNhan(this, "Bạn có chắc muốn " + hanhDong + " tài khoản " + tk.getSoTaiKhoan()
                + " của " + tk.getTenChuTaiKhoan() + "?")) {
            return;
        }
        thucHien(() -> controller.doiDongBang(tk));
    }

    private void dongTaiKhoan() {
        TaiKhoan tk = dangChon();
        if (tk == null) {
            return;
        }
        if (!UI.xacNhan(this, "ĐÓNG VĨNH VIỄN tài khoản " + tk.getSoTaiKhoan() + " của "
                + tk.getTenChuTaiKhoan() + "?\nThao tác này không hoàn tác được.")) {
            return;
        }
        thucHien(() -> controller.dongTaiKhoan(tk));
    }

    private void thucHien(Runnable viec) {
        try {
            viec.run();
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
