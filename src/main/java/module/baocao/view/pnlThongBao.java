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
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import module.baocao.controller.ThongBaoController;
import module.baocao.model.ThongBao;

/** Thông báo của khách hàng: biến động số dư, kết quả KYC, khóa/mở khóa... Chọn một dòng để đọc đầy đủ. */
public class pnlThongBao extends JPanel implements CoTheLamMoi {
    private final ThongBaoController controller;
    private ArrayList<ThongBao> dsHienTai = new ArrayList<>();

    private final DefaultTableModel tableModel = UI.moHinhBang("Thời gian", "Tiêu đề", "Trạng thái");
    private final JTable tbl = new JTable(tableModel);
    private final JTextArea txtNoiDung = new JTextArea(4, 40);
    private final JLabel lblSoMoi = new JLabel(" ");

    public pnlThongBao(PhienDangNhap phien) {
        this.controller = new ThongBaoController(phien);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tbl.setRowHeight(24);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UI.rongCot(tbl, 150, 400, 90);
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienNoiDung();
            }
        });

        txtNoiDung.setEditable(false);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        JScrollPane pnlChiTiet = new JScrollPane(txtNoiDung);
        pnlChiTiet.setBorder(BorderFactory.createTitledBorder("Nội dung"));

        JButton btnDaDoc = new JButton("Đánh dấu tất cả đã đọc");
        JButton btnLamMoi = new JButton("Làm mới");
        btnDaDoc.addActionListener(e -> daDoc());
        btnLamMoi.addActionListener(e -> lamMoi());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pnlNut.add(btnDaDoc);
        pnlNut.add(btnLamMoi);

        lblSoMoi.setFont(UI.dam(14));
        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.add(pnlChiTiet, BorderLayout.CENTER);
        pnlDuoi.add(pnlNut, BorderLayout.SOUTH);

        add(lblSoMoi, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(pnlDuoi, BorderLayout.SOUTH);
    }

    @Override
    public void lamMoi() {
        dsHienTai = controller.thongBaoCuaToi();
        tableModel.setRowCount(0);
        int moi = 0;
        for (ThongBao t : dsHienTai) {
            tableModel.addRow(new Object[]{DinhDang.ngayGio(t.getThoiGian()), t.getTieuDe(),
                t.isDaDoc() ? "Đã đọc" : "MỚI"});
            if (!t.isDaDoc()) {
                moi++;
            }
        }
        lblSoMoi.setText(moi == 0 ? "Bạn không có thông báo mới." : "Bạn có " + moi + " thông báo mới.");
        lblSoMoi.setForeground(moi == 0 ? java.awt.Color.DARK_GRAY : UI.DO);
        txtNoiDung.setText("");
    }

    private void hienNoiDung() {
        int dong = tbl.getSelectedRow();
        txtNoiDung.setText(dong < 0 || dong >= dsHienTai.size() ? "" : dsHienTai.get(dong).getNoiDung());
    }

    private void daDoc() {
        try {
            controller.danhDauTatCaDaDoc();
            lamMoi();
        } catch (IllegalStateException ex) {
            UI.loi(this, ex.getMessage());
        }
    }
}
