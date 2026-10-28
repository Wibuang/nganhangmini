package module.xacthuc.view;

import core.phien.PhienDangNhap;
import core.phien.VaiTro;
import core.ui.CoTheLamMoi;
import core.ui.UI;
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
import module.xacthuc.controller.NguoiDungController;
import module.xacthuc.model.NguoiDung;

/** Quản trị viên quản lý người dùng: xem, tạo nhân viên, khóa/mở khóa, đặt lại mật khẩu. */
public class pnlNguoiDung extends JPanel implements CoTheLamMoi {
    private final NguoiDungController controller;
    private ArrayList<NguoiDung> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel = UI.moHinhBang(
            "Mã", "Tên đăng nhập", "Họ tên (nhân viên)", "Vai trò", "Trạng thái", "Sai MK", "Mã KH");
    private final JTable tbl = new JTable(tableModel);
    private final JTextField txtTim = new JTextField(22);
    private final JLabel lblTong = new JLabel(" ");

    public pnlNguoiDung(PhienDangNhap phien) {
        this.controller = new NguoiDungController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnLamMoi = new JButton("Làm mới");
        btnTim.addActionListener(e -> lamMoi());
        txtTim.addActionListener(e -> lamMoi());
        btnLamMoi.addActionListener(e -> {
            txtTim.setText("");
            lamMoi();
        });
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTim.add(new JLabel("Tìm theo tên đăng nhập / họ tên:"));
        pnlTim.add(txtTim);
        pnlTim.add(btnTim);
        pnlTim.add(btnLamMoi);

        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 50, 130, 200, 130, 90, 60, 60);

        JButton btnThem = new JButton("Thêm nhân viên");
        JButton btnKhoa = new JButton("Khóa / Mở khóa");
        JButton btnDatLai = new JButton("Đặt lại mật khẩu");
        btnThem.addActionListener(e -> themNhanVien());
        btnKhoa.addActionListener(e -> doiTrangThai());
        btnDatLai.addActionListener(e -> datLaiMatKhau());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnThem);
        pnlNut.add(btnKhoa);
        pnlNut.add(btnDatLai);
        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.add(lblTong, BorderLayout.WEST);
        pnlDuoi.add(pnlNut, BorderLayout.CENTER);

        add(pnlTim, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlDuoi, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.timKiem(txtTim.getText());
        tableModel.setRowCount(0);
        for (NguoiDung nd : dsHienTai) {
            tableModel.addRow(new Object[]{
                nd.getMaND(), nd.getTenDangNhap(), nd.getHoTen() == null ? "" : nd.getHoTen(),
                VaiTro.ten(nd.getVaiTro()), nd.isTrangThai() ? "Hoạt động" : "Đã khóa",
                nd.getSoLanSai(), nd.getMaKH() == null ? "" : nd.getMaKH()});
        }
        lblTong.setText("Tổng: " + dsHienTai.size() + " người dùng");
    }

    private NguoiDung dangChon() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một dòng trong bảng.");
            return null;
        }
        return dsHienTai.get(dong);
    }

    private void themNhanVien() {
        JTextField txtHoTen = new JTextField(20);
        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cboVaiTro = new JComboBox<>(new String[]{"Giao dịch viên", "Quản trị viên"});
        JPanel form = UI.bieuMau(
                new String[]{"Họ tên:", "Tên đăng nhập:", "Mật khẩu ban đầu:", "Vai trò:"},
                new JComponent[]{txtHoTen, txtUser, txtPass, cboVaiTro});
        while (true) {
            int chon = JOptionPane.showConfirmDialog(this, form, "Thêm nhân viên",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (chon != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                String vaiTro = cboVaiTro.getSelectedIndex() == 0 ? VaiTro.GIAODICHVIEN : VaiTro.ADMIN;
                controller.taoNhanVien(txtHoTen.getText(), txtUser.getText(),
                        new String(txtPass.getPassword()), vaiTro);
                lamMoi();
                UI.thongBao(this, "Đã tạo tài khoản nhân viên.");
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                UI.loi(this, ex.getMessage());
            }
        }
    }

    private void doiTrangThai() {
        NguoiDung nd = dangChon();
        if (nd == null) {
            return;
        }
        String hanhDong = nd.isTrangThai() ? "KHÓA" : "MỞ KHÓA";
        if (!UI.xacNhan(this, "Bạn có chắc muốn " + hanhDong + " tài khoản đăng nhập \""
                + nd.getTenDangNhap() + "\"?")) {
            return;
        }
        try {
            controller.doiTrangThai(nd);
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }

    private void datLaiMatKhau() {
        NguoiDung nd = dangChon();
        if (nd == null) {
            return;
        }
        if (!UI.xacNhan(this, "Đặt lại mật khẩu cho \"" + nd.getTenDangNhap()
                + "\"?\nMật khẩu cũ sẽ không dùng được nữa.")) {
            return;
        }
        try {
            String tam = controller.datLaiMatKhau(nd);
            lamMoi();
            JTextField o = new JTextField(tam);              // ô chỉ đọc để dễ sao chép
            o.setEditable(false);
            JOptionPane.showMessageDialog(this,
                    new Object[]{"Mật khẩu tạm của " + nd.getTenDangNhap() + " là:", o,
                                 "Hãy báo cho người dùng và yêu cầu đổi mật khẩu sau khi đăng nhập."},
                    "Đặt lại mật khẩu", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
