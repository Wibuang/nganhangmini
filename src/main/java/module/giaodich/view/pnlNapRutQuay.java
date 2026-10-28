package module.giaodich.view;

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
import javax.swing.JTextField;
import module.giaodich.controller.GiaoDichController;
import module.giaodich.model.GiaoDich;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.model.TrangThaiTK;

/** Nhân viên nạp / rút tiền cho khách tại quầy: tra cứu số tài khoản → nhập số tiền → thực hiện. */
public class pnlNapRutQuay extends JPanel {
    private final TaiKhoanController taiKhoanController;
    private final GiaoDichController controller;

    private final JTextField txtSoTK = new JTextField(14);
    private final JLabel lblChu = new JLabel(" ");
    private final JLabel lblSoDu = new JLabel(" ");
    private final JLabel lblTrangThai = new JLabel(" ");
    private final JTextField txtSoTien = new JTextField(16);
    private final JTextField txtGhiChu = new JTextField(24);

    public pnlNapRutQuay(PhienDangNhap phien) {
        this.taiKhoanController = new TaiKhoanController(phien);
        this.controller = new GiaoDichController(phien);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton btnTra = new JButton("Tra cứu");
        btnTra.addActionListener(e -> traCuu());
        txtSoTK.addActionListener(e -> traCuu());
        JPanel pnlSoTK = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlSoTK.add(txtSoTK);
        pnlSoTK.add(btnTra);

        lblSoDu.setFont(UI.dam(15));
        lblSoDu.setForeground(UI.XANH);

        JPanel form = UI.bieuMau(
                new String[]{"Số tài khoản:", "Chủ tài khoản:", "Số dư hiện tại:", "Trạng thái:",
                             "Số tiền (VND):", "Ghi chú (không bắt buộc):"},
                new JComponent[]{pnlSoTK, lblChu, lblSoDu, lblTrangThai, txtSoTien, txtGhiChu});

        JButton btnNap = new JButton("Nạp tiền");
        JButton btnRut = new JButton("Rút tiền");
        btnNap.addActionListener(e -> thucHien(true));
        btnRut.addActionListener(e -> thucHien(false));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlNut.add(btnNap);
        pnlNut.add(btnRut);

        add(form, BorderLayout.NORTH);
        add(pnlNut, BorderLayout.CENTER);
        add(new JLabel("Giao dịch tại quầy tối đa 500.000.000 VND mỗi lần. Luôn tra cứu và đối chiếu tên chủ tài khoản trước khi thực hiện."),
                BorderLayout.SOUTH);
    }

    private void traCuu() {
        try {
            TaiKhoan tk = taiKhoanController.layTheoSo(txtSoTK.getText());
            lblChu.setText(tk.getTenChuTaiKhoan());
            lblSoDu.setText(DinhDang.tien(tk.getSoDu()) + " VND");
            lblTrangThai.setText(TrangThaiTK.ten(tk.getTrangThai()));
            lblTrangThai.setForeground(tk.dangHoatDong() ? UI.XANH : UI.DO);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            xoaThongTin();
            UI.loi(this, ex.getMessage());
        }
    }

    private void xoaThongTin() {
        lblChu.setText(" ");
        lblSoDu.setText(" ");
        lblTrangThai.setText(" ");
    }

    private void thucHien(boolean laNap) {
        try {
            GiaoDich g = laNap
                    ? controller.napTienQuay(txtSoTK.getText(), txtSoTien.getText(), txtGhiChu.getText())
                    : controller.rutTienQuay(txtSoTK.getText(), txtSoTien.getText(), txtGhiChu.getText());
            txtSoTien.setText("");
            txtGhiChu.setText("");
            traCuu();                                          // cập nhật số dư mới lên màn hình
            BienLai.hienThi(this, laNap ? "Nạp tiền thành công!" : "Rút tiền thành công!", g);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
