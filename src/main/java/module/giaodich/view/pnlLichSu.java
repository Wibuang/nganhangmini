package module.giaodich.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.giaodich.controller.GiaoDichController;
import module.giaodich.model.GiaoDich;
import module.giaodich.model.KenhGD;
import module.giaodich.model.LoaiGD;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.view.cboTaiKhoan;

/**
 * Lịch sử giao dịch có bộ lọc và xuất CSV.
 * Khách hàng: chọn một tài khoản của mình. Nhân viên: tìm theo số tài khoản hoặc mã giao dịch
 * trên toàn hệ thống; quản trị viên có thêm nút "Đảo giao dịch".
 */
public class pnlLichSu extends JPanel implements CoTheLamMoi {
    private final PhienDangNhap phien;
    private final GiaoDichController controller;
    private final TaiKhoanController taiKhoanController;
    private final boolean laKhach;

    private final cboTaiKhoan cboTK = new cboTaiKhoan();
    private final JTextField txtTuKhoa = new JTextField(14);
    private final JComboBox<String> cboLoai = new JComboBox<>(LoaiGD.TEN);
    private final JTextField txtTu = new JTextField(8);
    private final JTextField txtDen = new JTextField(8);
    private final DefaultTableModel tableModel;
    private final JTable tbl;
    private final JLabel lblTong = new JLabel(" ");
    private ArrayList<GiaoDich> dsHienTai = new ArrayList<>();

    public pnlLichSu(PhienDangNhap phien) {
        this.phien = phien;
        this.laKhach = phien.laKhach();
        this.controller = new GiaoDichController(phien);
        this.taiKhoanController = new TaiKhoanController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tableModel = laKhach
                ? UI.moHinhBang("Mã GD", "Thời gian", "Loại", "Số tiền (VND)", "Số dư sau",
                                "Kênh", "TK đối tác", "Nội dung")
                : UI.moHinhBang("Mã GD", "Thời gian", "Số TK", "Loại", "Số tiền (VND)", "Số dư sau",
                                "Kênh", "TK đối tác", "Nội dung");
        tbl = new JTable(tableModel);
        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int cotTien = laKhach ? 3 : 4;
        if (laKhach) {
            UI.rongCot(tbl, 90, 150, 90, 110, 110, 80, 100, 260);
        } else {
            UI.rongCot(tbl, 90, 150, 100, 90, 110, 110, 80, 100, 240);
        }
        UI.mauTien(tbl, cotTien);
        UI.canPhai(tbl, cotTien + 1);

        // ----- hàng bộ lọc -----
        txtTu.setToolTipText("dd/MM/yyyy, để trống = không giới hạn");
        txtDen.setToolTipText("dd/MM/yyyy, để trống = không giới hạn");
        JButton btnLoc = new JButton("Lọc");
        JButton btnBoLoc = new JButton("Bỏ lọc");
        JButton btnXuat = new JButton("Xuất CSV");
        btnLoc.addActionListener(e -> taiDuLieu());
        btnBoLoc.addActionListener(e -> {
            cboLoai.setSelectedIndex(0);
            txtTuKhoa.setText("");
            txtTu.setText("");
            txtDen.setText("");
            taiDuLieu();
        });
        btnXuat.addActionListener(e -> xuat());

        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        if (laKhach) {
            pnlLoc.add(new JLabel("Tài khoản:"));
            pnlLoc.add(cboTK);
        } else {
            pnlLoc.add(new JLabel("Số TK / mã GD:"));
            pnlLoc.add(txtTuKhoa);
        }
        pnlLoc.add(new JLabel("Loại:"));
        pnlLoc.add(cboLoai);
        pnlLoc.add(new JLabel("Từ ngày:"));
        pnlLoc.add(txtTu);
        pnlLoc.add(new JLabel("Đến ngày:"));
        pnlLoc.add(txtDen);
        pnlLoc.add(btnLoc);
        pnlLoc.add(btnBoLoc);
        pnlLoc.add(btnXuat);

        // ----- hàng dưới -----
        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.add(lblTong, BorderLayout.WEST);
        if (phien.laAdmin()) {
            JButton btnDao = new JButton("Đảo giao dịch đã chọn");
            btnDao.addActionListener(e -> dao());
            JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlNut.add(btnDao);
            pnlDuoi.add(pnlNut, BorderLayout.EAST);
        }

        add(pnlLoc, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlDuoi, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        if (laKhach) {
            cboTK.napLai(taiKhoanController.taiKhoanCuaToi(false));
        }
        taiDuLieu();
    }

    /** Đọc các ô lọc, lấy dữ liệu từ Controller và đổ lên bảng. */
    private void taiDuLieu() {
        String loai = LoaiGD.MA[cboLoai.getSelectedIndex()];
        try {
            if (laKhach) {
                TaiKhoan tk = cboTK.getChon();
                dsHienTai = tk == null ? new ArrayList<>()
                        : controller.lichSuTaiKhoan(tk.getMaTK(), loai, txtTu.getText(), txtDen.getText());
            } else {
                dsHienTai = controller.timGiaoDich(txtTuKhoa.getText(), loai, txtTu.getText(), txtDen.getText());
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
            return;
        }
        tableModel.setRowCount(0);
        for (GiaoDich g : dsHienTai) {
            String dau = LoaiGD.laTienVao(g.getLoaiGD()) ? "+" : "-";
            ArrayList<Object> dong = new ArrayList<>();
            dong.add(g.getMaThamChieu());
            dong.add(DinhDang.ngayGio(g.getNgayGD()));
            if (!laKhach) {
                dong.add(g.getSoTaiKhoan());
            }
            dong.add(LoaiGD.tenHienThi(g.getLoaiGD()));
            dong.add(dau + DinhDang.tien(g.getSoTien()));
            dong.add(DinhDang.tien(g.getSoDuSau()));
            dong.add(KenhGD.ten(g.getKenh()));
            dong.add(g.getSoTKDoiTac() == null ? "" : g.getSoTKDoiTac());
            dong.add(g.getGhiChu() == null ? "" : g.getGhiChu());
            tableModel.addRow(dong.toArray());
        }
        lblTong.setText("Có " + dsHienTai.size() + " giao dịch"
                + (dsHienTai.size() >= 500 ? " (hiển thị tối đa 500 giao dịch mới nhất)" : ""));
    }

    private void xuat() {
        if (dsHienTai.isEmpty()) {
            UI.thongBao(this, "Không có dữ liệu để xuất.");
            return;
        }
        File f = UI.chonFileCsv(this, laKhach ? "lichsu.csv" : "giaodich.csv");
        if (f == null) {
            return;
        }
        try {
            controller.xuatLichSu(dsHienTai, f);
            UI.thongBao(this, "Đã xuất " + dsHienTai.size() + " giao dịch ra file:\n" + f.getAbsolutePath());
        } catch (IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }

    // Chỉ quản trị viên thấy nút này; Controller kiểm tra quyền thêm một lần nữa
    private void dao() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn giao dịch cần đảo trong bảng.");
            return;
        }
        GiaoDich g = dsHienTai.get(dong);
        String lyDo = UI.hoi(this, "Lý do đảo giao dịch " + g.getMaThamChieu() + " ("
                + LoaiGD.tenHienThi(g.getLoaiGD()) + " " + DinhDang.tien(g.getSoTien()) + " VND):");
        if (lyDo == null) {
            return;
        }
        try {
            GiaoDich moi = controller.daoGiaoDich(g.getMaGD(), lyDo);
            lamMoi();
            BienLai.hienThi(this, "Đã đảo giao dịch " + g.getMaThamChieu() + ".", moi);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
