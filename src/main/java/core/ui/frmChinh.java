package core.ui;

import core.phien.PhienDangNhap;
import core.phien.VaiTro;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Cửa sổ chính: thanh tiêu đề (tên + vai trò + Đăng xuất) và mỗi module một tab.
 * Chỉ hiện các module mà vai trò của người đăng nhập được phép dùng.
 */
public class frmChinh extends JFrame {

    public frmChinh(PhienDangNhap phien, List<IModule> dsModule, Runnable khiDangXuat) {
        setTitle("Ngân hàng mini - " + phien.getTenHienThi());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        for (IModule m : dsModule) {
            if (m.choPhep(phien)) {
                tabs.addTab(m.ten(phien), m.taoManHinh(phien));
            }
        }
        tabs.addChangeListener(e -> {          // chuyển module thì tải lại dữ liệu của module đó
            java.awt.Component c = tabs.getSelectedComponent();
            if (c instanceof CoTheLamMoi) {
                ((CoTheLamMoi) c).lamMoi();
            }
        });

        JLabel lblChao = new JLabel("Xin chào, " + phien.getTenHienThi()
                + "   (" + VaiTro.ten(phien.getVaiTro()) + ")");
        lblChao.setFont(UI.dam(16));
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.addActionListener(e -> {
            dispose();
            khiDangXuat.run();
        });
        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        pnlTren.add(lblChao, BorderLayout.CENTER);
        pnlTren.add(btnDangXuat, BorderLayout.EAST);

        JPanel pnlChinh = new JPanel(new BorderLayout());
        pnlChinh.add(pnlTren, BorderLayout.NORTH);
        pnlChinh.add(tabs, BorderLayout.CENTER);
        setContentPane(pnlChinh);

        setSize(1050, 640);
        setLocationRelativeTo(null);
    }
}
