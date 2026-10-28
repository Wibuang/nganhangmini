package module.taikhoan.view;

import core.util.DinhDang;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import module.taikhoan.model.TaiKhoan;

/**
 * Ô chọn tài khoản dùng chung: module Giao dịch dùng lại để khách hàng chọn tài khoản của mình.
 * Mỗi mục hiện "số TK - tên gợi nhớ (số dư)".
 */
public class cboTaiKhoan extends JComboBox<TaiKhoan> {

    public cboTaiKhoan() {
        setPreferredSize(new Dimension(420, 28));          // đủ rộng để đọc "số TK - tên (số dư)"
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof TaiKhoan) {
                    TaiKhoan tk = (TaiKhoan) value;
                    setText(tk.tenHienThi() + "   (" + DinhDang.tien(tk.getSoDu()) + " VND)");
                }
                return this;
            }
        });
    }

    /** Nạp lại danh sách và giữ nguyên tài khoản đang chọn nếu vẫn còn trong danh sách. */
    public void napLai(ArrayList<TaiKhoan> ds) {
        TaiKhoan dangChon = getChon();
        removeAllItems();
        int viTriCu = -1;
        for (int i = 0; i < ds.size(); i++) {
            addItem(ds.get(i));
            if (dangChon != null && ds.get(i).getMaTK() == dangChon.getMaTK()) {
                viTriCu = i;
            }
        }
        if (viTriCu >= 0) {
            setSelectedIndex(viTriCu);
        }
    }

    /** Tài khoản đang chọn (null nếu danh sách trống). */
    public TaiKhoan getChon() {
        return (TaiKhoan) getSelectedItem();
    }
}
