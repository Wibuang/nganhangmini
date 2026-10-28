package module.khachhang.view;

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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.khachhang.controller.KhachHangController;
import module.khachhang.model.KhachHang;
import module.khachhang.model.TrangThaiKYC;
import module.xacthuc.controller.NguoiDungController;

/**
 * Màn hình của nhân viên: tìm kiếm, tạo và sửa hồ sơ khách hàng, cấp tài khoản đăng nhập.
 * Duyệt/từ chối KYC và ngừng/kích hoạt hồ sơ chỉ dành cho quản trị viên.
 */
public class pnlQuanLyKhachHang extends JPanel implements CoTheLamMoi {
    private final PhienDangNhap phien;
    private final KhachHangController controller;
    private ArrayList<KhachHang> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel = UI.moHinhBang(
            "Mã KH", "Họ tên", "Ngày sinh", "CCCD", "SĐT", "Email", "KYC", "Hoạt động", "Ghi chú KYC");
    private final JTable tbl = new JTable(tableModel);
    private final JTextField txtTim = new JTextField(20);
    private final JComboBox<String> cboKYC = new JComboBox<>(TrangThaiKYC.TEN);
    private final JLabel lblTong = new JLabel(" ");

    public pnlQuanLyKhachHang(PhienDangNhap phien) {
        this.phien = phien;
        this.controller = new KhachHangController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ----- tìm kiếm -----
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnLamMoi = new JButton("Làm mới");
        btnTim.addActionListener(e -> lamMoi());
        txtTim.addActionListener(e -> lamMoi());
        btnLamMoi.addActionListener(e -> {
            txtTim.setText("");
            cboKYC.setSelectedIndex(0);
            lamMoi();
        });
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTim.add(new JLabel("Tìm theo tên / CCCD / SĐT / email:"));
        pnlTim.add(txtTim);
        pnlTim.add(new JLabel("KYC:"));
        pnlTim.add(cboKYC);
        pnlTim.add(btnTim);
        pnlTim.add(btnLamMoi);

        // ----- bảng -----
        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 55, 150, 85, 110, 90, 150, 90, 80, 150);

        // ----- nút -----
        JButton btnThem = new JButton("Tạo hồ sơ");
        JButton btnSua = new JButton("Sửa hồ sơ");
        JButton btnDuyet = new JButton("Duyệt KYC");
        JButton btnTuChoi = new JButton("Từ chối KYC");
        JButton btnHoatDong = new JButton("Ngừng / Kích hoạt");
        JButton btnDangNhap = new JButton("Cấp tài khoản đăng nhập");
        btnThem.addActionListener(e -> taoHoSo());
        btnSua.addActionListener(e -> suaHoSo());
        btnDuyet.addActionListener(e -> duyet());
        btnTuChoi.addActionListener(e -> tuChoi());
        btnHoatDong.addActionListener(e -> doiHoatDong());
        btnDangNhap.addActionListener(e -> capDangNhap());
        // Chỉ quản trị viên mới duyệt KYC và ngừng hồ sơ; giao dịch viên chỉ thấy nút bị mờ
        btnDuyet.setEnabled(phien.laAdmin());
        btnTuChoi.setEnabled(phien.laAdmin());
        btnHoatDong.setEnabled(phien.laAdmin());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        pnlNut.add(btnThem);
        pnlNut.add(btnSua);
        pnlNut.add(btnDuyet);
        pnlNut.add(btnTuChoi);
        pnlNut.add(btnHoatDong);
        pnlNut.add(btnDangNhap);
        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.add(lblTong, BorderLayout.NORTH);
        pnlDuoi.add(pnlNut, BorderLayout.CENTER);

        add(pnlTim, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlDuoi, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.timKiem(txtTim.getText(), TrangThaiKYC.MA[cboKYC.getSelectedIndex()]);
        tableModel.setRowCount(0);
        for (KhachHang kh : dsHienTai) {
            tableModel.addRow(new Object[]{
                kh.getMaKH(), kh.getHoTen(), DinhDang.ngay(kh.getNgaySinh()), kh.getCccd(),
                kh.getSoDienThoai(), kh.getEmail(), TrangThaiKYC.ten(kh.getTrangThaiKYC()),
                kh.isHoatDong() ? "Có" : "Ngừng",
                kh.getGhiChuKYC() == null ? "" : kh.getGhiChuKYC()});
        }
        lblTong.setText("Tổng: " + dsHienTai.size() + " khách hàng");
    }

    private KhachHang dangChon() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một dòng trong bảng.");
            return null;
        }
        return dsHienTai.get(dong);
    }

    // ------------------------------------------------------------ tạo / sửa hồ sơ
    private void taoHoSo() {
        JTextField txtHoTen = new JTextField(22);
        JTextField txtNgaySinh = new JTextField();
        JTextField txtCCCD = new JTextField();
        JTextField txtSDT = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtDiaChi = new JTextField();
        JPanel form = UI.bieuMau(
                new String[]{"Họ tên:", "Ngày sinh (dd/MM/yyyy):", "CCCD (12 số):",
                             "Số điện thoại:", "Email:", "Địa chỉ:"},
                new JComponent[]{txtHoTen, txtNgaySinh, txtCCCD, txtSDT, txtEmail, txtDiaChi});
        while (true) {
            int chon = JOptionPane.showConfirmDialog(this, form, "Tạo hồ sơ khách hàng",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (chon != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                KhachHang kh = controller.taoHoSo(txtHoTen.getText(), txtNgaySinh.getText(),
                        txtCCCD.getText(), txtSDT.getText(), txtEmail.getText(), txtDiaChi.getText());
                lamMoi();
                UI.thongBao(this, "Đã tạo hồ sơ cho " + kh.getHoTen() + " (mã " + kh.getMaKH()
                        + ").\nHồ sơ đang chờ quản trị viên duyệt KYC.");
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                UI.loi(this, ex.getMessage());
            }
        }
    }

    private void suaHoSo() {
        KhachHang kh = dangChon();
        if (kh == null) {
            return;
        }
        JTextField txtHoTen = new JTextField(kh.getHoTen(), 22);
        JTextField txtNgaySinh = new JTextField(DinhDang.ngay(kh.getNgaySinh()));
        JTextField txtCCCD = new JTextField(kh.getCccd());
        txtCCCD.setEditable(false);                          // CCCD không được sửa
        JTextField txtSDT = new JTextField(kh.getSoDienThoai() == null ? "" : kh.getSoDienThoai());
        JTextField txtEmail = new JTextField(kh.getEmail() == null ? "" : kh.getEmail());
        JTextField txtDiaChi = new JTextField(kh.getDiaChi() == null ? "" : kh.getDiaChi());
        JPanel form = UI.bieuMau(
                new String[]{"Họ tên:", "Ngày sinh (dd/MM/yyyy):", "CCCD (không sửa được):",
                             "Số điện thoại:", "Email:", "Địa chỉ:"},
                new JComponent[]{txtHoTen, txtNgaySinh, txtCCCD, txtSDT, txtEmail, txtDiaChi});
        while (true) {
            int chon = JOptionPane.showConfirmDialog(this, form, "Sửa hồ sơ - mã " + kh.getMaKH(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (chon != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                controller.capNhat(kh.getMaKH(), txtHoTen.getText(), txtNgaySinh.getText(),
                        txtSDT.getText(), txtEmail.getText(), txtDiaChi.getText());
                lamMoi();
                UI.thongBao(this, "Cập nhật thành công!");
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                UI.loi(this, ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ KYC, hoạt động
    private void duyet() {
        KhachHang kh = dangChon();
        if (kh == null) {
            return;
        }
        if (!UI.xacNhan(this, "Xác nhận đã kiểm tra giấy tờ và DUYỆT KYC cho " + kh.getHoTen() + "?")) {
            return;
        }
        thucHien(() -> controller.duyetKYC(kh));
    }

    private void tuChoi() {
        KhachHang kh = dangChon();
        if (kh == null) {
            return;
        }
        String lyDo = UI.hoi(this, "Lý do từ chối KYC của " + kh.getHoTen() + ":");
        if (lyDo == null) {
            return;
        }
        thucHien(() -> controller.tuChoiKYC(kh, lyDo));
    }

    private void doiHoatDong() {
        KhachHang kh = dangChon();
        if (kh == null) {
            return;
        }
        String hanhDong = kh.isHoatDong() ? "NGỪNG HOẠT ĐỘNG" : "KÍCH HOẠT LẠI";
        if (!UI.xacNhan(this, "Bạn có chắc muốn " + hanhDong + " hồ sơ của " + kh.getHoTen() + "?")) {
            return;
        }
        thucHien(() -> controller.doiHoatDong(kh));
    }

    private void thucHien(Runnable viec) {
        try {
            viec.run();
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }

    // ------------------------------------------------------------ cấp đăng nhập (gọi module Xác thực)
    private void capDangNhap() {
        KhachHang kh = dangChon();
        if (kh == null) {
            return;
        }
        JTextField txtUser = new JTextField(16);
        JPasswordField txtPass = new JPasswordField();
        JPanel form = UI.bieuMau(new String[]{"Tên đăng nhập:", "Mật khẩu ban đầu:"},
                new JComponent[]{txtUser, txtPass});
        NguoiDungController nguoiDung = new NguoiDungController(phien);
        while (true) {
            int chon = JOptionPane.showConfirmDialog(this, form,
                    "Cấp tài khoản đăng nhập cho " + kh.getHoTen(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (chon != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                nguoiDung.capDangNhapChoKhach(kh.getMaKH(), txtUser.getText(),
                        new String(txtPass.getPassword()));
                UI.thongBao(this, "Đã cấp tài khoản đăng nhập \"" + txtUser.getText().trim()
                        + "\" cho " + kh.getHoTen() + ".");
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                UI.loi(this, ex.getMessage());
            }
        }
    }
}
