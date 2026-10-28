/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.util;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

/** Băm mật khẩu (SHA-256) và sinh mật khẩu tạm. */
public class MatKhauUtil {
    private static final String KY_TU =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    /**
     * Băm mật khẩu. Ghép thêm tên đăng nhập vào trước khi băm để hai người
     * dùng cùng mật khẩu vẫn có mã băm khác nhau.
     * (Hệ thống thật nên dùng BCrypt/Argon2 với muối ngẫu nhiên.)
     */
    public static String bam(String tenDangNhap, String matKhau) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String goc = tenDangNhap.trim().toLowerCase(Locale.ROOT) + "|" + matKhau;
            byte[] kq = md.digest(goc.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : kq) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Máy không hỗ trợ SHA-256", e);
        }
    }

    /** Mật khẩu tạm 8 ký tự ngẫu nhiên (bỏ các ký tự dễ nhầm như 0/O, 1/l). */
    public static String taoMatKhauTam() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(KY_TU.charAt(r.nextInt(KY_TU.length())));
        }
        return sb.toString();
    }
}
