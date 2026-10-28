package module.khachhang.model;

/** Trạng thái xác minh danh tính (KYC) của khách hàng. */
public final class TrangThaiKYC {
    public static final String CHO_DUYET   = "CHO_DUYET";
    public static final String DA_XAC_MINH = "DA_XAC_MINH";
    public static final String TU_CHOI     = "TU_CHOI";

    /** Dùng cho ô lọc: vị trí 0 = "Tất cả" (mã null). Hai mảng cùng thứ tự. */
    public static final String[] MA  = { null, CHO_DUYET, DA_XAC_MINH, TU_CHOI };
    public static final String[] TEN = { "Tất cả", "Chờ duyệt", "Đã xác minh", "Từ chối" };

    private TrangThaiKYC() {}

    public static String ten(String ma) {
        for (int i = 1; i < MA.length; i++) {
            if (MA[i].equals(ma)) {
                return TEN[i];
            }
        }
        return ma == null ? "" : ma;
    }
}
