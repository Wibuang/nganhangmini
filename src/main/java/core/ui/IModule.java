/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.ui;

import core.phien.PhienDangNhap;
import javax.swing.JComponent;

/**
 * Khuôn của mọi module. frmChinh chỉ cần danh sách IModule: tự lọc theo vai trò và tạo tab.
 * Thêm module mới = viết 1 lớp cài IModule rồi thêm vào danh sách trong main.Main.
 */
public interface IModule {
    /** Tên hiện trên tab (có thể khác nhau theo vai trò). */
    String ten(PhienDangNhap phien);

    /** Vai trò này có được thấy module không? */
    boolean choPhep(PhienDangNhap phien);

    /** Màn hình chính của module cho người đang đăng nhập. */
    JComponent taoManHinh(PhienDangNhap phien);
}
