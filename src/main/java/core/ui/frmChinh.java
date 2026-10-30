package core.ui;

import core.phien.PhienDangNhap;
import core.phien.VaiTro;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

/**
 * Cửa sổ chính: thanh tiêu đề (tên + vai trò + Đăng xuất) và mỗi module một tab.
 * Chỉ hiện các module mà vai trò của người đăng nhập được phép dùng.
 * Tự đăng xuất khi không thao tác quá phutHetPhien phút (0 = không tự đăng xuất).
 */
public class frmChinh extends JFrame {
    private static final int CHU_KY_KIEM_TRA_MS = 5000;

    private final AWTEventListener langNgheThaoTac;      // null nếu không dùng hết phiên
    private final Timer dongHoKiemTra;
    private volatile long lanThaoTacCuoi = System.currentTimeMillis();

    public frmChinh(PhienDangNhap phien, List<IModule> dsModule, Runnable khiDangXuat, int phutHetPhien) {
        setTitle("Ngân hàng mini - " + phien.getTenHienThi());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        for (IModule m : dsModule) {
            if (m.choPhep(phien)) {
                tabs.addTab(m.ten(phien), m.taoManHinh(phien));
            }
        }
        tabs.addChangeListener(e -> {          // chuyển module thì tải lại dữ liệu của module đó
            Component c = tabs.getSelectedComponent();
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

        setSize(1150, 660);
        setLocationRelativeTo(null);

        // ----- tự đăng xuất khi không thao tác -----
        if (phutHetPhien > 0) {
            langNgheThaoTac = ev -> lanThaoTacCuoi = System.currentTimeMillis();
            Toolkit.getDefaultToolkit().addAWTEventListener(langNgheThaoTac,
                    AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
            dongHoKiemTra = new Timer(CHU_KY_KIEM_TRA_MS, e -> {
                if (System.currentTimeMillis() - lanThaoTacCuoi > phutHetPhien * 60_000L) {
                    hetPhien(khiDangXuat, phutHetPhien);
                }
            });
            dongHoKiemTra.start();
        } else {
            langNgheThaoTac = null;
            dongHoKiemTra = null;
        }
    }

    private void hetPhien(Runnable khiDangXuat, int phut) {
        dispose();                                  // đóng cửa sổ chính (và mọi hộp thoại đang mở)
        khiDangXuat.run();                          // quay về màn hình đăng nhập
        UI.thongBao(null, "Phiên làm việc đã kết thúc vì bạn không thao tác trong " + phut
                + " phút. Vui lòng đăng nhập lại.");
    }

    /** Dừng bộ đếm hết phiên khi cửa sổ đóng, tránh chạy ngầm sau khi đã đăng xuất. */
    @Override
    public void dispose() {
        if (dongHoKiemTra != null) {
            dongHoKiemTra.stop();
        }
        if (langNgheThaoTac != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(langNgheThaoTac);
        }
        super.dispose();
    }
}
