package module.chuyenkhoan.model;

/** Trạng thái của lệnh chuyển tiền định kỳ. */
public final class TrangThaiLenh {
    public static final String HOAT_DONG = "HOAT_DONG";
    public static final String TAM_DUNG  = "TAM_DUNG";
    public static final String HET_HAN   = "HET_HAN";
    public static final String DA_HUY    = "DA_HUY";

    /** Dùng cho ô lọc: vị trí 0 = "Tất cả" (mã null). Hai mảng cùng thứ tự. */
    public static final String[] MA  = { null, HOAT_DONG, TAM_DUNG, HET_HAN, DA_HUY };
    public static final String[] TEN = { "Tất cả", "Đang chạy", "Tạm dừng", "Hết hạn", "Đã hủy" };

    private TrangThaiLenh() {}

    public static String ten(String ma) {
        for (int i = 1; i < MA.length; i++) {
            if (MA[i].equals(ma)) {
                return TEN[i];
            }
        }
        return ma == null ? "" : ma;
    }
}
