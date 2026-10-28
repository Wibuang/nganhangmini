package module.giaodich.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
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
import module.taikhoan.view.cboTaiKhoan;

/** Khách hàng nạp / rút tiền trên một trong các tài khoản đang hoạt động của mình. */
public class pnlNapRutKhach extends JPanel implements CoTheLamMoi {
    private final TaiKhoanController taiKhoanController;
    private final GiaoDichController controller;

    private final cboTaiKhoan cboTK = new cboTaiKhoan();
    private final JTextField txtSoTien = new JTextField(16);

    public pnlNapRutKhach(PhienDangNhap phien) {
        this.taiKhoanController = new TaiKhoanController(phien);
        this.controller = new GiaoDichController(phien);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton btnNap = new JButton("Nạp tiền");
        JButton btnRut = new JButton("Rút tiền");
        btnNap.addActionListener(e -> thucHien(true));
        btnRut.addActionListener(e -> thucHien(false));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlNut.add(btnNap);
        pnlNut.add(btnRut);

        add(UI.bieuMau(new String[]{"Tài khoản:", "Số tiền (VND):"},
                new JComponent[]{cboTK, txtSoTien}), BorderLayout.NORTH);
        add(pnlNut, BorderLayout.CENTER);
        add(new JLabel("Nhập số nguyên, ví dụ 1000000 hoặc 1.000.000. Chỉ giao dịch được trên tài khoản đang hoạt động."),
                BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        cboTK.napLai(taiKhoanController.taiKhoanCuaToi(true));
    }

    private void thucHien(boolean laNap) {
        TaiKhoan tk = cboTK.getChon();
        if (tk == null) {
            UI.loi(this, "Bạn chưa có tài khoản nào đang hoạt động.");
            return;
        }
        try {
            GiaoDich g = laNap ? controller.napTienUngDung(tk.getMaTK(), txtSoTien.getText())
                               : controller.rutTienUngDung(tk.getMaTK(), txtSoTien.getText());
            txtSoTien.setText("");
            lamMoi();                                          // cập nhật số dư trong danh sách chọn
            BienLai.hienThi(this, laNap ? "Nạp tiền thành công!" : "Rút tiền thành công!", g);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
