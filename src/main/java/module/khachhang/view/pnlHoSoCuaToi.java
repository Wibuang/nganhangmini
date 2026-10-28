package module.khachhang.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
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
import module.khachhang.controller.KhachHangController;
import module.khachhang.model.KhachHang;
import module.khachhang.model.TrangThaiKYC;

/** Hồ sơ của chính khách hàng: xem toàn bộ, tự sửa số điện thoại / email / địa chỉ. */
public class pnlHoSoCuaToi extends JPanel implements CoTheLamMoi {
    private final KhachHangController controller;

    private final JLabel lblHoTen = new JLabel();
    private final JLabel lblNgaySinh = new JLabel();
    private final JLabel lblCCCD = new JLabel();
    private final JLabel lblKYC = new JLabel();
    private final JTextField txtSDT = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtDiaChi = new JTextField(20);

    public pnlHoSoCuaToi(PhienDangNhap phien) {
        this.controller = new KhachHangController(phien);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton btnLuu = new JButton("Lưu thay đổi");
        btnLuu.addActionListener(e -> luu());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlNut.add(btnLuu);

        add(UI.bieuMau(
                new String[]{"Họ tên:", "Ngày sinh:", "CCCD:", "Xác minh danh tính (KYC):",
                             "Số điện thoại:", "Email:", "Địa chỉ:"},
                new JComponent[]{lblHoTen, lblNgaySinh, lblCCCD, lblKYC, txtSDT, txtEmail, txtDiaChi}),
                BorderLayout.NORTH);
        add(pnlNut, BorderLayout.CENTER);
        add(new JLabel("Họ tên, ngày sinh và CCCD chỉ thay đổi được tại quầy giao dịch."),
                BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        KhachHang kh = controller.hoSoCuaToi();
        if (kh == null) {
            return;
        }
        lblHoTen.setText(kh.getHoTen());
        lblNgaySinh.setText(DinhDang.ngay(kh.getNgaySinh()));
        lblCCCD.setText(kh.getCccd());
        lblKYC.setText(TrangThaiKYC.ten(kh.getTrangThaiKYC()));
        txtSDT.setText(kh.getSoDienThoai() == null ? "" : kh.getSoDienThoai());
        txtEmail.setText(kh.getEmail() == null ? "" : kh.getEmail());
        txtDiaChi.setText(kh.getDiaChi() == null ? "" : kh.getDiaChi());
    }

    private void luu() {
        try {
            controller.capNhatLienHe(txtSDT.getText(), txtEmail.getText(), txtDiaChi.getText());
            UI.thongBao(this, "Đã lưu thông tin liên hệ.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
