package module.baocao.model;

import java.time.LocalDateTime;

/** Một thông báo gửi cho khách hàng (biến động số dư, kết quả KYC, khóa tài khoản...). */
public class ThongBao {
    private final int maTB;
    private final String tieuDe;
    private final String noiDung;
    private final LocalDateTime thoiGian;
    private final boolean daDoc;

    public ThongBao(int maTB, String tieuDe, String noiDung, LocalDateTime thoiGian, boolean daDoc) {
        this.maTB = maTB;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.thoiGian = thoiGian;
        this.daDoc = daDoc;
    }

    public int getMaTB() { return maTB; }
    public String getTieuDe() { return tieuDe; }
    public String getNoiDung() { return noiDung; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public boolean isDaDoc() { return daDoc; }
}
