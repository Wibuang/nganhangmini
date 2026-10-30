package module.chuyenkhoan.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import module.chuyenkhoan.controller.ChuyenKhoanController;
import module.chuyenkhoan.model.KetQuaChuyenKhoan;
import module.chuyenkhoan.model.NguoiThuHuong;
import module.chuyenkhoan.model.XemTruocChuyenKhoan;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.view.cboTaiKhoan;

/**
 * Chuyển khoản nội bộ: chọn tài khoản nguồn → chọn người nhận (nhập số hoặc lấy từ danh bạ) →
 * xem trước (phí, tổng tiền) → xác nhận → nhập OTP nếu số tiền lớn → biên lai.
 */
public class pnlChuyenKhoan extends JPanel implements CoTheLamMoi {
    private static final String CHON_DANH_BA = "-- Chọn từ danh bạ --";

    private final ChuyenKhoanController controller;
    private final TaiKhoanController taiKhoanController;

    private final cboTaiKhoan cboNguon = new cboTaiKhoan();
    private final JComboBox<Object> cboDanhBa = new JComboBox<>();
    private final JTextField txtSoTKNhan = new JTextField(14);
    private final JLabel lblNguoiNhan = new JLabel(" ");
    private final JTextField txtSoTien = new JTextField(16);
    private final JTextField txtNoiDung = new JTextField(30);
    private final JCheckBox chkLuu = new JCheckBox("Lưu người nhận vào danh bạ");
    private boolean dangNapDanhBa = false;

    public pnlChuyenKhoan(PhienDangNhap phien) {
        this.controller = new ChuyenKhoanController(phien);
        this.taiKhoanController = new TaiKhoanController(phien);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        cboDanhBa.addActionListener(e -> {
            Object chon = cboDanhBa.getSelectedItem();
            if (!dangNapDanhBa && chon instanceof NguoiThuHuong) {
                txtSoTKNhan.setText(((NguoiThuHuong) chon).getSoTaiKhoan());
                kiemTraNguoiNhan();
            }
        });

        JButton btnKiemTra = new JButton("Kiểm tra");
        btnKiemTra.addActionListener(e -> kiemTraNguoiNhan());
        JPanel pnlSoTK = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlSoTK.add(txtSoTKNhan);
        pnlSoTK.add(btnKiemTra);

        JPanel form = UI.bieuMau(
                new String[]{"Từ tài khoản:", "Người nhận trong danh bạ:", "Số tài khoản nhận:",
                             "Chủ tài khoản nhận:", "Số tiền (VND):", "Nội dung:", ""},
                new JComponent[]{cboNguon, cboDanhBa, pnlSoTK, lblNguoiNhan, txtSoTien, txtNoiDung, chkLuu});

        JButton btnChuyen = new JButton("Chuyển khoản");
        btnChuyen.addActionListener(e -> chuyen());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlNut.add(btnChuyen);

        add(form, BorderLayout.NORTH);
        add(pnlNut, BorderLayout.CENTER);
        add(new JLabel("<html>Hạn mức, phí và ngưỡng xác nhận OTP do quản trị viên cấu hình. "
                + "Phí được hiển thị trước khi bạn xác nhận.</html>"), BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        cboNguon.napLai(taiKhoanController.taiKhoanCuaToi(true));
        napDanhBa();
    }

    private void napDanhBa() {
        dangNapDanhBa = true;
        cboDanhBa.removeAllItems();
        cboDanhBa.addItem(CHON_DANH_BA);
        ArrayList<NguoiThuHuong> ds = controller.danhBa();
        for (NguoiThuHuong n : ds) {
            cboDanhBa.addItem(n);
        }
        cboDanhBa.setSelectedIndex(0);
        dangNapDanhBa = false;
    }

    private void kiemTraNguoiNhan() {
        TaiKhoan nguon = cboNguon.getChon();
        if (nguon == null) {
            lblNguoiNhan.setForeground(UI.DO);
            lblNguoiNhan.setText("Bạn chưa có tài khoản nào đang hoạt động.");
            return;
        }
        try {
            TaiKhoan nhan = controller.traCuuNguoiNhan(nguon.getMaTK(), txtSoTKNhan.getText());
            lblNguoiNhan.setForeground(UI.XANH);
            lblNguoiNhan.setText(nhan.getTenChuTaiKhoan());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblNguoiNhan.setForeground(UI.DO);
            lblNguoiNhan.setText(ex.getMessage());
        }
    }

    private void chuyen() {
        TaiKhoan nguon = cboNguon.getChon();
        if (nguon == null) {
            UI.loi(this, "Bạn chưa có tài khoản nào đang hoạt động.");
            return;
        }
        try {
            // Bước 1: xem trước (kiểm tra hạn mức, số dư, người nhận, tính phí)
            XemTruocChuyenKhoan xt = controller.xemTruoc(nguon.getMaTK(), txtSoTKNhan.getText(),
                    txtSoTien.getText());
            boolean dongY = UI.xacNhan(this, "XÁC NHẬN CHUYỂN KHOẢN\n\n"
                    + "Từ tài khoản: " + nguon.getSoTaiKhoan() + "\n"
                    + "Đến: " + xt.getNhan().getTenChuTaiKhoan() + " (" + xt.getNhan().getSoTaiKhoan() + ")\n"
                    + "Số tiền: " + DinhDang.tien(xt.getSoTien()) + " VND\n"
                    + "Phí chuyển khoản: " + DinhDang.tien(xt.getPhi()) + " VND\n"
                    + "Tổng bị trừ: " + DinhDang.tien(xt.getTongTru()) + " VND\n\n"
                    + (xt.isCanOtp() ? "Số tiền lớn nên cần xác nhận bằng mã OTP.\n\n" : "")
                    + "Bạn có chắc chắn muốn chuyển?");
            if (!dongY) {
                return;
            }
            // Bước 2: OTP nếu số tiền lớn
            String otpNhap = "";
            if (xt.isCanOtp()) {
                String ma = controller.taoOtp();
                otpNhap = UI.hoi(this, "Mã OTP của bạn là: " + ma + "\n"
                        + "(Bản học tập hiển thị mã tại đây; hệ thống thật gửi qua SMS. Mã có hiệu lực 2 phút.)\n\n"
                        + "Nhập mã OTP để xác nhận:");
                if (otpNhap == null) {
                    return;
                }
            }
            // Bước 3: chuyển
            KetQuaChuyenKhoan kq = controller.chuyenKhoan(nguon.getMaTK(), txtSoTKNhan.getText(),
                    txtSoTien.getText(), txtNoiDung.getText(), otpNhap);
            if (chkLuu.isSelected()) {
                try {
                    controller.luuDanhBa(kq.getSoTKNhan(), "");
                } catch (IllegalArgumentException ex) {
                    // đã có trong danh bạ: bỏ qua
                }
            }
            xoaForm();
            lamMoi();
            UI.thongBao(this, "CHUYỂN KHOẢN THÀNH CÔNG\n\n"
                    + "Mã giao dịch: " + kq.getGiaoDich().getMaThamChieu() + "\n"
                    + "Người nhận: " + kq.getTenNguoiNhan() + " (" + kq.getSoTKNhan() + ")\n"
                    + "Số tiền: " + DinhDang.tien(kq.getGiaoDich().getSoTien()) + " VND\n"
                    + "Phí: " + DinhDang.tien(kq.getPhi()) + " VND\n"
                    + "Số dư sau giao dịch: " + DinhDang.tien(kq.getSoDuSau()) + " VND\n"
                    + "Thời gian: " + DinhDang.ngayGio(kq.getGiaoDich().getNgayGD()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }

    private void xoaForm() {
        txtSoTKNhan.setText("");
        txtSoTien.setText("");
        txtNoiDung.setText("");
        lblNguoiNhan.setText(" ");
        chkLuu.setSelected(false);
    }
}
