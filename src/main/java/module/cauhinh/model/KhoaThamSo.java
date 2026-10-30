package module.cauhinh.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Danh sách các tham số hệ thống: mã, giá trị mặc định và khoảng cho phép.
 * Mã tham số luôn lấy từ đây (không gõ chuỗi tay ở nơi khác) để tránh viết sai.
 * Giá trị mặc định được dùng khi CSDL chưa có tham số đó.
 */
public final class KhoaThamSo {
    public static final String SO_LAN_SAI_TOI_DA         = "SO_LAN_SAI_TOI_DA";
    public static final String TUOI_TOI_THIEU            = "TUOI_TOI_THIEU";
    public static final String SO_TAI_KHOAN_TOI_DA       = "SO_TAI_KHOAN_TOI_DA";
    public static final String DO_DAI_MAT_KHAU_TOI_THIEU = "DO_DAI_MAT_KHAU_TOI_THIEU";
    public static final String THOI_GIAN_HET_PHIEN_PHUT  = "THOI_GIAN_HET_PHIEN_PHUT";
    public static final String HAN_MUC_NAP_RUT_QUAY      = "HAN_MUC_NAP_RUT_QUAY";
    public static final String HAN_MUC_NAP_RUT_UNG_DUNG  = "HAN_MUC_NAP_RUT_UNG_DUNG";
    public static final String CHUYEN_TOI_THIEU          = "CHUYEN_TOI_THIEU";
    public static final String HAN_MUC_CHUYEN_MOI_LAN    = "HAN_MUC_CHUYEN_MOI_LAN";
    public static final String HAN_MUC_CHUYEN_MOI_NGAY   = "HAN_MUC_CHUYEN_MOI_NGAY";
    public static final String NGUONG_OTP                = "NGUONG_OTP";

    private static final long TOI_DA_TIEN = 1_000_000_000_000L;    // 1.000 tỷ

    /** mã → { mặc định, nhỏ nhất, lớn nhất } */
    private static final Map<String, long[]> GIOI_HAN = new LinkedHashMap<>();
    static {
        GIOI_HAN.put(SO_LAN_SAI_TOI_DA,         new long[]{5, 3, 10});
        GIOI_HAN.put(TUOI_TOI_THIEU,            new long[]{18, 16, 25});
        GIOI_HAN.put(SO_TAI_KHOAN_TOI_DA,       new long[]{5, 1, 20});
        GIOI_HAN.put(DO_DAI_MAT_KHAU_TOI_THIEU, new long[]{6, 6, 32});
        GIOI_HAN.put(THOI_GIAN_HET_PHIEN_PHUT,  new long[]{10, 0, 120});
        GIOI_HAN.put(HAN_MUC_NAP_RUT_QUAY,      new long[]{500_000_000L, 1_000, TOI_DA_TIEN});
        GIOI_HAN.put(HAN_MUC_NAP_RUT_UNG_DUNG,  new long[]{50_000_000L, 1_000, TOI_DA_TIEN});
        GIOI_HAN.put(CHUYEN_TOI_THIEU,          new long[]{10_000L, 1_000, TOI_DA_TIEN});
        GIOI_HAN.put(HAN_MUC_CHUYEN_MOI_LAN,    new long[]{200_000_000L, 1_000, TOI_DA_TIEN});
        GIOI_HAN.put(HAN_MUC_CHUYEN_MOI_NGAY,   new long[]{500_000_000L, 1_000, TOI_DA_TIEN});
        GIOI_HAN.put(NGUONG_OTP,                new long[]{10_000_000L, 1_000, TOI_DA_TIEN});
    }

    private KhoaThamSo() {}

    public static boolean tonTai(String ma) {
        return GIOI_HAN.containsKey(ma);
    }

    public static long macDinh(String ma) {
        return gioiHan(ma)[0];
    }

    public static long nhoNhat(String ma) {
        return gioiHan(ma)[1];
    }

    public static long lonNhat(String ma) {
        return gioiHan(ma)[2];
    }

    private static long[] gioiHan(String ma) {
        long[] g = GIOI_HAN.get(ma);
        if (g == null) {
            throw new IllegalArgumentException("Tham số không tồn tại: " + ma);
        }
        return g;
    }
}
