package module.chuyenkhoan.model;

import java.time.LocalDate;

/** Tần suất của lệnh chuyển tiền định kỳ. */
public final class TanSuat {
    public static final String HANG_NGAY  = "HANG_NGAY";
    public static final String HANG_TUAN  = "HANG_TUAN";
    public static final String HANG_THANG = "HANG_THANG";

    public static final String[] MA  = { HANG_NGAY, HANG_TUAN, HANG_THANG };
    public static final String[] TEN = { "Hằng ngày", "Hằng tuần", "Hằng tháng" };

    private TanSuat() {}

    public static String ten(String ma) {
        for (int i = 0; i < MA.length; i++) {
            if (MA[i].equals(ma)) {
                return TEN[i];
            }
        }
        return ma == null ? "" : ma;
    }

    public static boolean hopLe(String ma) {
        for (String m : MA) {
            if (m.equals(ma)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ngày chạy đầu tiên SAU mốc moc, tính từ ngày bắt đầu theo đúng chu kỳ.
     * Tính từ ngày bắt đầu (không cộng dồn) nên lệnh "ngày 31 hằng tháng" vẫn giữ ngày 31 khi tháng có đủ ngày.
     */
    public static LocalDate ngayChaySau(LocalDate batDau, String tanSuat, LocalDate moc) {
        LocalDate d = batDau;
        long k = 0;
        while (!d.isAfter(moc)) {
            k++;
            if (HANG_NGAY.equals(tanSuat)) {
                d = batDau.plusDays(k);
            } else if (HANG_TUAN.equals(tanSuat)) {
                d = batDau.plusWeeks(k);
            } else {
                d = batDau.plusMonths(k);
            }
        }
        return d;
    }
}
