package module.chuyenkhoan.view;

import core.phien.PhienDangNhap;
import core.ui.CoTheLamMoi;
import core.ui.UI;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.chuyenkhoan.controller.ChuyenKhoanController;
import module.chuyenkhoan.model.NguoiThuHuong;

/** Danh bạ người nhận của khách hàng: thêm bằng số tài khoản, xóa. */
public class pnlDanhBa extends JPanel implements CoTheLamMoi {
    private final ChuyenKhoanController controller;
    private ArrayList<NguoiThuHuong> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel =
            UI.moHinhBang("Tên gợi nhớ", "Số tài khoản", "Chủ tài khoản");
    private final JTable tbl = new JTable(tableModel);

    public pnlDanhBa(PhienDangNhap phien) {
        this.controller = new ChuyenKhoanController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tbl.setRowHeight(26);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 220, 160, 280);

        JButton btnThem = new JButton("Thêm người nhận");
        JButton btnXoa = new JButton("Xóa người nhận đã chọn");
        btnThem.addActionListener(e -> them());
        btnXoa.addActionListener(e -> xoa());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnThem);
        pnlNut.add(btnXoa);

        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlNut, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.danhBa();
        tableModel.setRowCount(0);
        for (NguoiThuHuong n : dsHienTai) {
            tableModel.addRow(new Object[]{n.getTenGoiNho(), n.getSoTaiKhoan(), n.getTenNguoiNhan()});
        }
    }

    private void them() {
        JTextField txtSo = new JTextField(14);
        JTextField txtTen = new JTextField();
        JPanel form = UI.bieuMau(new String[]{"Số tài khoản (10 số):", "Tên gợi nhớ (có thể để trống):"},
                new JComponent[]{txtSo, txtTen});
        while (true) {
            int chon = JOptionPane.showConfirmDialog(this, form, "Thêm người nhận",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (chon != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                controller.luuDanhBa(txtSo.getText(), txtTen.getText());
                lamMoi();
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                UI.loi(this, ex.getMessage());
            }
        }
    }

    private void xoa() {
        int dong = tbl.getSelectedRow();
        if (dong < 0) {
            UI.thongBao(this, "Vui lòng chọn một người nhận trong bảng.");
            return;
        }
        NguoiThuHuong n = dsHienTai.get(dong);
        if (!UI.xacNhan(this, "Xóa \"" + n.getTenGoiNho() + "\" khỏi danh bạ?")) {
            return;
        }
        try {
            controller.xoaDanhBa(n);
            lamMoi();
        } catch (IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
