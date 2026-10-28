package module.taikhoan.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import core.util.DinhDang;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.taikhoan.controller.TaiKhoanController;
import module.taikhoan.model.TaiKhoan;
import module.taikhoan.model.TrangThaiTK;

/** Danh sách các tài khoản của khách hàng đang đăng nhập; đổi tên gợi nhớ. */
public class pnlTaiKhoanCuaToi extends JPanel implements CoTheLamMoi {
    private final TaiKhoanController controller;
    private ArrayList<TaiKhoan> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel = UI.moHinhBang(
            "Số tài khoản", "Tên gợi nhớ", "Số dư (VND)", "Trạng thái", "Ngày mở");
    private final JTable tbl = new JTable(tableModel);
    private final JLabel lblTong = new JLabel(" ");

    public pnlTaiKhoanCuaToi(PhienDangNhap phien) {
        this.controller = new TaiKhoanController(phien);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tbl.setRowHeight(26);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 130, 220, 140, 110, 130);
        UI.canPhai(tbl, 2);

        lblTong.setFont(UI.dam(16));
        lblTong.setForeground(UI.XANH);

        JButton btnDoiTen = new JButton("Đổi tên gợi nhớ");
        JButton btnLamMoi = new JButton("Làm mới");
        btnDoiTen.addActionListener(e -> doiTen());
        btnLamMoi.addActionListener(e -> lamMoi());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnDoiTen);
        pnlNut.add(btnLamMoi);

        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.add(lblTong, BorderLayout.NORTH);
        pnlDuoi.add(pnlNut, BorderLayout.CENTER);

        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlDuoi, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.taiKhoanCuaToi(false);
        tableModel.setRowCount(0);
        long tong = 0;
        for (TaiKhoan tk : dsHienTai) {
            tableModel.addRow(new Object[]{
                tk.getSoTaiKhoan(), tk.getTenGoiNho(), DinhDang.tien(tk.getSoDu()),
                TrangThaiTK.ten(tk.getTrangThai()), DinhDang.ngay(tk.getNgayMo())});
            if (tk.dangHoatDong()) {
                tong += tk.getSoDu();
            }
        }
        lblTong.setText("Tổng số dư các tài khoản đang hoạt động: " + DinhDang.tien(tong) + " VND");
    }

    private void doiTen() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một tài khoản trong bảng.");
            return;
        }
        TaiKhoan tk = dsHienTai.get(dong);
        String ten = UI.hoi(this, "Tên gợi nhớ mới cho tài khoản " + tk.getSoTaiKhoan() + ":");
        if (ten == null) {
            return;
        }
        try {
            controller.doiTenGoiNho(tk.getMaTK(), ten);
            lamMoi();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
