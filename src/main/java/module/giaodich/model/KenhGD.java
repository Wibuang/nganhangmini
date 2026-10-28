package module.giaodich.model;

/** Kênh thực hiện giao dịch. */
public final class KenhGD {
    public static final String QUAY     = "QUAY";        // nhân viên làm tại quầy
    public static final String UNG_DUNG = "UNG_DUNG";    // khách hàng tự làm trên ứng dụng
    public static final String HE_THONG = "HE_THONG";    // hệ thống tự ghi (đảo giao dịch...)

    private KenhGD() {}

    public static String ten(String ma) {
        if (QUAY.equals(ma)) return "Tại quầy";
        if (UNG_DUNG.equals(ma)) return "Ứng dụng";
        if (HE_THONG.equals(ma)) return "Hệ thống";
        return ma == null ? "" : ma;
    }
}
