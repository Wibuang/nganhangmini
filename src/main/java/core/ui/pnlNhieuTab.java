/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Khung nhiều tab; tự gọi lamMoi() của tab vừa được chọn nên dữ liệu luôn mới. */
public class pnlNhieuTab extends JPanel implements CoTheLamMoi {
    private final JTabbedPane tabs = new JTabbedPane();

    public pnlNhieuTab() {
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        tabs.addChangeListener(e -> lamMoiTabDangChon());
    }

    public pnlNhieuTab them(String ten, JComponent manHinh) {
        tabs.addTab(ten, manHinh);
        return this;
    }

    private void lamMoiTabDangChon() {
        Component c = tabs.getSelectedComponent();
        if (c instanceof CoTheLamMoi) {
            ((CoTheLamMoi) c).lamMoi();
        }
    }

    @Override
    public void lamMoi() {
        lamMoiTabDangChon();
    }
}
