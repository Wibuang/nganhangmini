package module.chuyenkhoan.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.chuyenkhoan.controller.ChuyenKhoanController;
import module.chuyenkhoan.model.LenhDinhKy;
import module.chuyenkhoan.model.TanSuat;
import module.chuyenkhoan.model.TrangThaiLenh;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.view.cboTaiKhoan;

/** Lệnh chuyển tiền định kỳ của khách hàng: tạo, tạm dừng / tiếp tục, hủy. */
public class pnlLenhDinhKy extends JPanel implements CoTheLamMoi {
    private final ChuyenKhoanController controller;
    private final TaiKhoanController taiKhoanController;
    private ArrayList<LenhDinhKy> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel = UI.moHinhBang("Mã", "Từ TK", "Đến TK", "Người nhận",
            "Số tiền (VND)", "Tần suất", "Chạy tiếp theo", "Kết thúc", "Trạng thái", "Số lần", "Kết quả lần cuối");
    private final JTable tbl = new JTable(tableModel);

    public pnlLenhDinhKy(PhienDangNhap phien) {
        this.controller = new ChuyenKhoanController(phien);
        this.taiKhoanController = new TaiKhoanController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 40, 90, 90, 120, 100, 80, 110, 90, 90, 60, 150);
        UI.canPhai(tbl, 4);

        JButton btnTao = new JButton("Tạo lệnh mới");
        JButton btnTamDung = new JButton("Tạm dừng / Tiếp tục");
        JButton btnHuy = new JButton("Hủy lệnh");
        btnTao.addActionListener(e -> tao());
        btnTamDung.addActionListener(e -> tamDungTiepTuc());
        btnHuy.addActionListener(e -> huy());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnTao);
        pnlNut.add(btnTamDung);
        pnlNut.add(btnHuy);

        add(new JLabel("Lệnh định kỳ tự chuyển một khoản cố định mỗi chu kỳ. Thiếu tiền thì bỏ qua kỳ đó và báo cho bạn."),
                BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.lenhCuaToi();
        tableModel.setRowCount(0);
        for (LenhDinhKy l : dsHienTai) {
            tableModel.addRow(new Object[]{l.getMaLenh(), l.getSoTKNguon(), l.getSoTKNhan(),
                l.getTenNguoiNhan(), DinhDang.tien(l.getSoTien()), TanSuat.ten(l.getTanSuat()),
                DinhDang.ngay(l.getNgayChayKeTiep()),
                l.getNgayKetThuc() == null ? "Không" : DinhDang.ngay(l.getNgayKetThuc()),
                TrangThaiLenh.ten(l.getTrangThai()), l.getSoLanChay(),
                l.getKetQuaLanCuoi() == null ? "" : l.getKetQuaLanCuoi()});
        }
    }

    private LenhDinhKy dangChon() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một lệnh trong bảng.");
            return null;
        }
        return dsHienTai.get(dong);
    }

    private void tao() {
        cboTaiKhoan cboNguon = new cboTaiKhoan();
        cboNguon.napLai(taiKhoanController.taiKhoanCuaToi(true));
        JTextField txtSoTKNhan = new JTextField(14);
        JTextField txtSoTien = new JTextField();
        JTextField txtNoiDung = new JTextField();
        JComboBox<String> cboTanSuat = new JComboBox<>(TanSuat.TEN);
        JTextField txtBatDau = new JTextField(DinhDang.ngay(LocalDate.now().plusDays(1)));
        JTextField txtKetThuc = new JTextField();
        JPanel form = UI.bieuMau(
                new String[]{"Từ tài khoản:", "Số tài khoản nhận:", "Số tiền (VND):", "Nội dung:",
                             "Tần suất:", "Ngày bắt đầu (dd/MM/yyyy):", "Ngày kết thúc (để trống = không):"},
                new JComponent[]{cboNguon, txtSoTKNhan, txtSoTien, txtNoiDung, cboTanSuat, txtBatDau, txtKetThuc});
        while (true) {
            int chon = JOptionPane.showConfirmDialog(this, form, "Tạo lệnh chuyển tiền định kỳ",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (chon != JOptionPane.OK_OPTION) {
                return;
            }
            TaiKhoan nguon = cboNguon.getChon();
            if (nguon == null) {
                UI.loi(this, "Bạn chưa có tài khoản nào đang hoạt động.");
                return;
            }
            try {
                controller.taoLenh(nguon.getMaTK(), txtSoTKNhan.getText(), txtSoTien.getText(),
                        txtNoiDung.getText(), TanSuat.MA[cboTanSuat.getSelectedIndex()],
                        txtBatDau.getText(), txtKetThuc.getText());
                lamMoi();
                UI.thongBao(this, "Đã tạo lệnh chuyển tiền định kỳ.");
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                UI.loi(this, ex.getMessage());
            }
        }
    }

    private void tamDungTiepTuc() {
        LenhDinhKy l = dangChon();
        if (l == null) {
            return;
        }
        String moi = TrangThaiLenh.HOAT_DONG.equals(l.getTrangThai())
                ? TrangThaiLenh.TAM_DUNG : TrangThaiLenh.HOAT_DONG;
        thucHien(l, moi);
    }

    private void huy() {
        LenhDinhKy l = dangChon();
        if (l == null) {
            return;
        }
        if (!UI.xacNhan(this, "Hủy lệnh " + l.getMaLenh() + " (" + l.getSoTKNguon() + " → "
                + l.getSoTKNhan() + ")? Lệnh đã hủy không khôi phục được.")) {
            return;
        }
        thucHien(l, TrangThaiLenh.DA_HUY);
    }

    private void thucHien(LenhDinhKy l, String trangThaiMoi) {
        try {
            controller.doiTrangThaiLenh(l, trangThaiMoi);
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
