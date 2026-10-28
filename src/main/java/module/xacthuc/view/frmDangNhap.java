package module.xacthuc.view;

import core.phien.PhienDangNhap;
import core.ui.UI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import module.xacthuc.controller.DangNhapController;

/**
 * Màn hình đăng nhập. Đăng nhập đúng thì gọi khiThanhCong (do Main cung cấp) để mở cửa sổ chính;
 * module này không cần biết các module khác.
 */
public class frmDangNhap extends JFrame {

    private final DangNhapController controller = new DangNhapController();
    private final Consumer<PhienDangNhap> khiThanhCong;

    private final JTextField txtTenDangNhap = new JTextField(16);
    private final JPasswordField txtMatKhau = new JPasswordField(16);
    private final JLabel lblThongBao = new JLabel(" ", SwingConstants.CENTER);

    public frmDangNhap(Consumer<PhienDangNhap> khiThanhCong) {
        this.khiThanhCong = khiThanhCong;
        setTitle("Ngân hàng mini - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblTieuDe = new JLabel("NGÂN HÀNG MINI", SwingConstants.CENTER);
        lblTieuDe.setFont(UI.dam(22));
        JLabel lblPhu = new JLabel("Đăng nhập hệ thống", SwingConstants.CENTER);
        JPanel pnlTren = new JPanel(new GridLayout(2, 1));
        pnlTren.add(lblTieuDe);
        pnlTren.add(lblPhu);

        lblThongBao.setForeground(UI.DO);
        lblThongBao.setPreferredSize(new Dimension(320, 44));
        JButton btnDangNhap = new JButton("Đăng nhập");
        btnDangNhap.addActionListener(e -> xuLyDangNhap());
        getRootPane().setDefaultButton(btnDangNhap);            // Enter = bấm nút
        JPanel pnlDuoi = new JPanel(new BorderLayout(0, 6));
        pnlDuoi.add(lblThongBao, BorderLayout.CENTER);
        pnlDuoi.add(btnDangNhap, BorderLayout.SOUTH);

        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        pnl.add(pnlTren, BorderLayout.NORTH);
        pnl.add(UI.bieuMau(new String[]{"Tên đăng nhập:", "Mật khẩu:"},
                new JComponent[]{txtTenDangNhap, txtMatKhau}), BorderLayout.CENTER);
        pnl.add(pnlDuoi, BorderLayout.SOUTH);
        setContentPane(pnl);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void xuLyDangNhap() {
        try {
            PhienDangNhap phien = controller.dangNhap(txtTenDangNhap.getText(),
                    new String(txtMatKhau.getPassword()));
            dispose();
            khiThanhCong.accept(phien);
        } catch (IllegalArgumentException ex) {
            lblThongBao.setText("<html><div style='text-align:center'>" + ex.getMessage()
                    + "</div></html>");
            txtMatKhau.setText("");
        }
    }
}
