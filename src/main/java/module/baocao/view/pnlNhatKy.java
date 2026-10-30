package module.baocao.view;

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
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import module.baocao.controller.NhatKyController;
import module.baocao.model.NhatKy;

/** Quản trị viên tra cứu nhật ký kiểm toán: ai làm gì, lúc nào. */
public class pnlNhatKy extends JPanel implements CoTheLamMoi {
    private final NhatKyController controller;

    private final JTextField txtTuKhoa = new JTextField(16);
    private final JTextField txtTu = new JTextField(8);
    private final JTextField txtDen = new JTextField(8);
    private final DefaultTableModel tableModel = UI.moHinhBang("Thời gian", "Người thực hiện",
            "Hành động", "Đối tượng", "Chi tiết");
    private final JLabel lblTong = new JLabel(" ");

    public pnlNhatKy(PhienDangNhap phien) {
        this.controller = new NhatKyController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton btnTim = new JButton("Tìm");
        JButton btnBoLoc = new JButton("Bỏ lọc");
        btnTim.addActionListener(e -> lamMoi());
        txtTuKhoa.addActionListener(e -> lamMoi());
        btnBoLoc.addActionListener(e -> {
            txtTuKhoa.setText("");
            txtTu.setText("");
            txtDen.setText("");
            lamMoi();
        });
        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlLoc.add(new JLabel("Từ khóa (người dùng / hành động / nội dung):"));
        pnlLoc.add(txtTuKhoa);
        pnlLoc.add(new JLabel("Từ ngày:"));
        pnlLoc.add(txtTu);
        pnlLoc.add(new JLabel("Đến ngày:"));
        pnlLoc.add(txtDen);
        pnlLoc.add(btnTim);
        pnlLoc.add(btnBoLoc);

        JTable bang = new JTable(tableModel);
        bang.setRowHeight(24);
        UI.rongCot(bang, 150, 110, 160, 190, 340);

        add(pnlLoc, BorderLayout.NORTH);
        add(new JScrollPane(bang), BorderLayout.CENTER);
        add(lblTong, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        ArrayList<NhatKy> ds;
        try {
            ds = controller.tim(txtTuKhoa.getText(), txtTu.getText(), txtDen.getText());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
            return;
        }
        tableModel.setRowCount(0);
        for (NhatKy n : ds) {
            tableModel.addRow(new Object[]{DinhDang.ngayGio(n.getThoiGian()),
                n.getTenDangNhap() == null ? "" : n.getTenDangNhap(), n.getHanhDong(),
                n.getDoiTuong() == null ? "" : n.getDoiTuong(),
                n.getChiTiet() == null ? "" : n.getChiTiet()});
        }
        lblTong.setText("Có " + ds.size() + " dòng nhật ký"
                + (ds.size() >= NhatKyController.TOI_DA_HIEN_THI ? " (hiển thị tối đa "
                + NhatKyController.TOI_DA_HIEN_THI + " dòng mới nhất)" : ""));
    }
}
