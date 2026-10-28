package main;

import core.phien.PhienDangNhap;
import core.ui.IModule;
import core.ui.frmChinh;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import module.giaodich.GiaoDichModule;
import module.khachhang.KhachHangModule;
import module.taikhoan.TaiKhoanModule;
import module.xacthuc.XacThucModule;
import module.xacthuc.view.frmDangNhap;

/**
 * Điểm chạy của chương trình và là nơi DUY NHẤT biết tất cả các module.
 * Thêm module mới: viết lớp cài IModule rồi thêm vào danh sách bên dưới.
 */
public class Main {

    /** Thứ tự ở đây là thứ tự các tab trên cửa sổ chính. */
    private static final List<IModule> CAC_MODULE = Arrays.asList(
            new KhachHangModule(),
            new TaiKhoanModule(),
            new GiaoDichModule(),
            new XacThucModule());

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {           // giao diện Nimbus cho đẹp hơn
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // không có Nimbus thì dùng giao diện mặc định
        }
        SwingUtilities.invokeLater(Main::moDangNhap);
    }

    private static void moDangNhap() {
        new frmDangNhap(Main::moCuaSoChinh).setVisible(true);
    }

    private static void moCuaSoChinh(PhienDangNhap phien) {
        new frmChinh(phien, CAC_MODULE, Main::moDangNhap).setVisible(true);
    }
}
