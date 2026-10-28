package module.xacthuc.view;

import core.phien.PhienDangNhap;
import core.ui.UI;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import module.xacthuc.controller.NguoiDungController;

/** Đổi mật khẩu của chính người đang đăng nhập (mọi vai trò). */
public class pnlDoiMatKhau extends JPanel {
    private final NguoiDungController controller;
    private final JPasswordField txtCu = new JPasswordField(16);
    private final JPasswordField txtMoi = new JPasswordField(16);
    private final JPasswordField txtXacNhan = new JPasswordField(16);

    public pnlDoiMatKhau(PhienDangNhap phien) {
        this.controller = new NguoiDungController(phien);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton btnDoi = new JButton("Đổi mật khẩu");
        btnDoi.addActionListener(e -> doi());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlNut.add(btnDoi);

        add(UI.bieuMau(
                new String[]{"Mật khẩu hiện tại:", "Mật khẩu mới:", "Nhập lại mật khẩu mới:"},
                new JComponent[]{txtCu, txtMoi, txtXacNhan}), BorderLayout.NORTH);
        add(pnlNut, BorderLayout.CENTER);
    }

    private void doi() {
        try {
            controller.doiMatKhau(new String(txtCu.getPassword()),
                    new String(txtMoi.getPassword()), new String(txtXacNhan.getPassword()));
            txtCu.setText("");
            txtMoi.setText("");
            txtXacNhan.setText("");
            UI.thongBao(this, "Đổi mật khẩu thành công!");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
