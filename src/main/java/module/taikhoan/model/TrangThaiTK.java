package module.taikhoan.model;

/** Trạng thái của tài khoản ngân hàng. */
public final class TrangThaiTK {
    public static final String HOAT_DONG = "HOAT_DONG";
    public static final String DONG_BANG = "DONG_BANG";
    public static final String DA_DONG   = "DA_DONG";

    /** Dùng cho ô lọc: vị trí 0 = "Tất cả" (mã null). Hai mảng cùng thứ tự. */
    public static final String[] MA  = { null, HOAT_DONG, DONG_BANG, DA_DONG };
    public static final String[] TEN = { "Tất cả", "Hoạt động", "Đóng băng", "Đã đóng" };

    private TrangThaiTK() {}

    public static String ten(String ma) {
        for (int i = 1; i < MA.length; i++) {
            if (MA[i].equals(ma)) {
                return TEN[i];
            }
        }
        return ma == null ? "" : ma;
    }
}
