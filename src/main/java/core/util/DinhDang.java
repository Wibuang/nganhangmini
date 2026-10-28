package core.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Pattern;

/** Định dạng và đọc tiền, ngày giờ; kiểm tra email, số điện thoại. */
public class DinhDang {
    private static final DecimalFormat TIEN = new DecimalFormat("#,###",
            DecimalFormatSymbols.getInstance(new Locale("vi", "VN")));       // 5.000.000
    private static final DateTimeFormatter NGAY_GIO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter NGAY_HIEN_THI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public static String tien(long soTien) {
        return TIEN.format(soTien);
    }

    public static String ngayGio(LocalDateTime t) {
        return t == null ? "" : t.format(NGAY_GIO);
    }

    public static String ngay(LocalDate d) {
        return d == null ? "" : d.format(NGAY_HIEN_THI);
    }

    public static String ngay(LocalDateTime t) {
        return t == null ? "" : t.format(NGAY_HIEN_THI);
    }

    /** "" -> null; "15/10/2026" -> LocalDate; sai dạng -> báo lỗi. */
    public static LocalDate docNgay(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), NGAY);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Ngày \"" + text.trim() + "\" không hợp lệ, nhập theo dạng dd/MM/yyyy.");
        }
    }

    /** Như docNgay nhưng bắt buộc phải nhập; tenOGoi dùng trong thông báo lỗi. */
    public static LocalDate docNgayBatBuoc(String tenO, String text) {
        LocalDate d = docNgay(text);
        if (d == null) {
            throw new IllegalArgumentException("Vui lòng nhập " + tenO + " (dd/MM/yyyy).");
        }
        return d;
    }

    public static void kiemTraKhoangNgay(LocalDate tu, LocalDate den) {
        if (tu != null && den != null && tu.isAfter(den)) {
            throw new IllegalArgumentException("\"Từ ngày\" không được sau \"Đến ngày\".");
        }
    }

    /** Đọc số tiền người dùng gõ; chấp nhận 1000000, 1.000.000 hoặc 1,000,000. */
    public static long docTien(String text) {
        try {
            String s = text.trim().replace(".", "").replace(",", "").replace(" ", "");
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Số tiền không hợp lệ, chỉ nhập số nguyên.");
        }
    }

    public static boolean laEmailHopLe(String email) {
        return EMAIL.matcher(email.trim()).matches();
    }

    public static boolean laSoDienThoaiHopLe(String sdt) {
        return sdt.trim().matches("\\d{9,11}");
    }
}
