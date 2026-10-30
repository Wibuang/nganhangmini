package module.giaodich.model;

/** Các loại giao dịch trong sổ cái và tên hiển thị tiếng Việt. */
public final class LoaiGD {
    public static final String NAP        = "NAP";
    public static final String RUT        = "RUT";
    public static final String CHUYEN_DI  = "CHUYEN_DI";     // chuyển khoản: bên gửi
    public static final String CHUYEN_DEN = "CHUYEN_DEN";    // chuyển khoản: bên nhận
    public static final String PHI        = "PHI";           // phí chuyển khoản (trừ tiền bên gửi)
    public static final String HOAN_NAP   = "HOAN_NAP";      // đảo một giao dịch nạp (trừ tiền)
    public static final String HOAN_RUT   = "HOAN_RUT";      // đảo một giao dịch rút (cộng tiền)

    /** Dùng cho ô lọc: vị trí 0 = "Tất cả" (mã null). Hai mảng cùng thứ tự. */
    public static final String[] MA = { null, NAP, RUT, CHUYEN_DI, CHUYEN_DEN, PHI, HOAN_NAP, HOAN_RUT };
    public static final String[] TEN = { "Tất cả", "Nạp tiền", "Rút tiền", "Chuyển đi",
                                         "Chuyển đến", "Phí", "Hoàn nạp", "Hoàn rút" };

    private LoaiGD() {}

    public static String tenHienThi(String ma) {
        for (int i = 1; i < MA.length; i++) {
            if (MA[i].equals(ma)) {
                return TEN[i];
            }
        }
        return ma == null ? "" : ma;
    }

    /** Giao dịch làm tăng số dư (hiện dấu +) hay giảm (dấu -). */
    public static boolean laTienVao(String ma) {
        return NAP.equals(ma) || CHUYEN_DEN.equals(ma) || HOAN_RUT.equals(ma);
    }
}
