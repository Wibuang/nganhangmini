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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import module.chuyenkhoan.controller.ChuyenKhoanController;
import module.chuyenkhoan.model.KetQuaChayLenh;
import module.chuyenkhoan.model.LenhDinhKy;
import module.chuyenkhoan.model.TanSuat;
import module.chuyenkhoan.model.TrangThaiLenh;

/** Màn hình nhân viên: xem mọi lệnh định kỳ và chạy các lệnh đã đến hạn (mô phỏng tác vụ chạy tự động mỗi ngày). */
public class pnlLenhDinhKyQuanTri extends JPanel implements CoTheLamMoi {
    private final ChuyenKhoanController controller;

    private final JComboBox<String> cboTrangThai = new JComboBox<>(TrangThaiLenh.TEN);
    private final DefaultTableModel tableModel = UI.moHinhBang("Mã", "Mã KH", "Từ TK", "Đến TK",
            "Số tiền (VND)", "Tần suất", "Chạy tiếp theo", "Trạng thái", "Số lần", "Kết quả lần cuối");
    private final JLabel lblTong = new JLabel(" ");

    public pnlLenhDinhKyQuanTri(PhienDangNhap phien) {
        this.controller = new ChuyenKhoanController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton btnLoc = new JButton("Lọc");
        btnLoc.addActionListener(e -> lamMoi());
        JButton btnChay = new JButton("Chạy các lệnh đến hạn hôm nay");
        btnChay.addActionListener(e -> chay());
        JPanel pnlTren = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTren.add(new JLabel("Trạng thái:"));
        pnlTren.add(cboTrangThai);
        pnlTren.add(btnLoc);
        pnlTren.add(btnChay);

        JTable bang = new JTable(tableModel);
        bang.setRowHeight(24);
        UI.rongCot(bang, 40, 60, 90, 90, 100, 80, 110, 90, 60, 220);
        UI.canPhai(bang, 4);

        add(pnlTren, BorderLayout.NORTH);
        add(new JScrollPane(bang), BorderLayout.CENTER);
        add(lblTong, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        ArrayList<LenhDinhKy> ds = controller.tatCaLenh(TrangThaiLenh.MA[cboTrangThai.getSelectedIndex()]);
        tableModel.setRowCount(0);
        for (LenhDinhKy l : ds) {
            tableModel.addRow(new Object[]{l.getMaLenh(), l.getMaKH(), l.getSoTKNguon(), l.getSoTKNhan(),
                DinhDang.tien(l.getSoTien()), TanSuat.ten(l.getTanSuat()),
                DinhDang.ngay(l.getNgayChayKeTiep()), TrangThaiLenh.ten(l.getTrangThai()),
                l.getSoLanChay(), l.getKetQuaLanCuoi() == null ? "" : l.getKetQuaLanCuoi()});
        }
        lblTong.setText("Có " + ds.size() + " lệnh");
    }

    private void chay() {
        if (!UI.xacNhan(this, "Chạy tất cả lệnh định kỳ đã đến hạn tính đến hôm nay?")) {
            return;
        }
        try {
            KetQuaChayLenh kq = controller.chayLenhDenHan();
            lamMoi();
            StringBuilder sb = new StringBuilder("Đã xử lý " + kq.getSoLenh() + " lệnh: "
                    + kq.getThanhCong() + " thành công, " + kq.getThatBai() + " thất bại.\n\n");
            for (String d : kq.getChiTiet()) {
                sb.append(d).append('\n');
            }
            UI.thongBao(this, sb.toString());
        } catch (IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
