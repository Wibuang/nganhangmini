/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package core.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Các hàm dựng giao diện dùng chung để code các màn hình ngắn gọn. */
public class UI {
    public static final Color XANH = new Color(0, 130, 60);
    public static final Color DO = new Color(192, 0, 0);

    public static void thongBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void loi(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean xacNhan(Component cha, String noiDung) {
        return JOptionPane.showConfirmDialog(cha, noiDung, "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Hỏi một chuỗi; trả về null nếu người dùng bấm Cancel. */
    public static String hoi(Component cha, String cauHoi) {
        return JOptionPane.showInputDialog(cha, cauHoi, "Nhập thông tin", JOptionPane.QUESTION_MESSAGE);
    }

    /** Hộp thoại chọn nơi lưu file CSV; trả về null nếu người dùng bấm Hủy. */
    public static File chonFileCsv(Component cha, String tenMacDinh) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(tenMacDinh));
        if (fc.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            f = new File(f.getPath() + ".csv");
        }
        return f;
    }

    public static Font dam(int coChu) {
        return new Font("Segoe UI", Font.BOLD, coChu);
    }

    /** Biểu mẫu 2 cột: nhãn bên trái, ô nhập/giá trị bên phải (co giãn theo chiều ngang). */
    public static JPanel bieuMau(String[] nhan, JComponent[] o) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        for (int i = 0; i < nhan.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.NONE;
            p.add(new JLabel(nhan[i]), c);

            c.gridx = 1;
            c.weightx = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            p.add(o[i], c);
        }
        return p;
    }

    public static JPanel coTieuDe(JPanel p, String tieuDe) {
        p.setBorder(BorderFactory.createTitledBorder(tieuDe));
        return p;
    }

    // ---------------------------------------------------------------- bảng dữ liệu
    /** Mô hình bảng không cho sửa trực tiếp trên ô. */
    public static DefaultTableModel moHinhBang(String... cot) {
        return new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void rongCot(JTable bang, int... rong) {
        for (int i = 0; i < rong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }
    }

    public static void canPhai(JTable bang, int... cot) {
        DefaultTableCellRenderer phai = new DefaultTableCellRenderer();
        phai.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : cot) {
            bang.getColumnModel().getColumn(c).setCellRenderer(phai);
        }
    }

    /** Cột số tiền có dấu: bắt đầu bằng "+" màu xanh, còn lại màu đỏ; căn phải. */
    public static void mauTien(JTable bang, int cot) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean chon,
                                                           boolean focus, int dong, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, chon, focus, dong, c);
                if (!chon) {
                    comp.setForeground(String.valueOf(v).startsWith("+") ? XANH : DO);
                }
                return comp;
            }
        };
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        bang.getColumnModel().getColumn(cot).setCellRenderer(r);
    }
}
