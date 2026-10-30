package module.xacthuc.controller;

import core.phien.PhienDangNhap;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mã OTP một lần cho các giao dịch lớn. Bản học tập: mã được tạo và hiển thị ngay trên màn hình
 * (hệ thống thật sẽ gửi qua SMS / ứng dụng xác thực). Mã gắn với người dùng, hết hạn sau 2 phút,
 * dùng được một lần và nhập sai 3 lần thì bị hủy.
 */
public class OtpController {
    public static final int HIEU_LUC_GIAY = 120;
    public static final int TOI_DA_SAI = 3;

    /** Mỗi người dùng chỉ có tối đa một mã đang hiệu lực. */
    private static final Map<Integer, MaOtp> DANH_SACH = new ConcurrentHashMap<>();
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    private final PhienDangNhap phien;

    public OtpController(PhienDangNhap phien) {
        this.phien = phien;
    }

    /** Tạo mã OTP 6 chữ số mới (mã cũ của người này, nếu có, mất hiệu lực). */
    public String taoOtp() {
        if (phien.laHeThong()) {
            throw new IllegalStateException("Bạn chưa đăng nhập.");
        }
        String ma = String.format("%06d", NGAU_NHIEN.nextInt(1_000_000));
        DANH_SACH.put(phien.getMaND(), new MaOtp(ma, System.currentTimeMillis() + HIEU_LUC_GIAY * 1000L));
        return ma;
    }

    /** Kiểm tra mã người dùng nhập. Đúng thì mã bị hủy (dùng một lần); sai thì ném lỗi. */
    public void kiemTra(String nhap) {
        MaOtp m = DANH_SACH.get(phien.getMaND());
        if (m == null) {
            throw new IllegalArgumentException("Chưa có mã OTP, vui lòng yêu cầu mã mới.");
        }
        if (System.currentTimeMillis() > m.hetHanLuc) {
            DANH_SACH.remove(phien.getMaND());
            throw new IllegalArgumentException("Mã OTP đã hết hạn, vui lòng yêu cầu mã mới.");
        }
        if (!m.ma.equals(nhap == null ? "" : nhap.trim())) {
            m.soLanSai++;
            if (m.soLanSai >= TOI_DA_SAI) {
                DANH_SACH.remove(phien.getMaND());
                throw new IllegalArgumentException(
                        "Bạn đã nhập sai mã OTP " + TOI_DA_SAI + " lần, vui lòng yêu cầu mã mới.");
            }
            throw new IllegalArgumentException("Mã OTP không đúng. Bạn còn "
                    + (TOI_DA_SAI - m.soLanSai) + " lần thử.");
        }
        DANH_SACH.remove(phien.getMaND());
    }

    private static class MaOtp {
        final String ma;
        final long hetHanLuc;
        int soLanSai = 0;

        MaOtp(String ma, long hetHanLuc) {
            this.ma = ma;
            this.hetHanLuc = hetHanLuc;
        }
    }
}
