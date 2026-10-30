package module.baocao.model;

import java.time.LocalDateTime;

/** Một dòng nhật ký kiểm toán: ai làm gì, lúc nào. */
public class NhatKy {
    private final int maNK;
    private final LocalDateTime thoiGian;
    private final String tenDangNhap;
    private final String hanhDong;
    private final String doiTuong;
    private final String chiTiet;

    public NhatKy(int maNK, LocalDateTime thoiGian, String tenDangNhap, String hanhDong,
                  String doiTuong, String chiTiet) {
        this.maNK = maNK;
        this.thoiGian = thoiGian;
        this.tenDangNhap = tenDangNhap;
        this.hanhDong = hanhDong;
        this.doiTuong = doiTuong;
        this.chiTiet = chiTiet;
    }

    public int getMaNK() { return maNK; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getHanhDong() { return hanhDong; }
    public String getDoiTuong() { return doiTuong; }
    public String getChiTiet() { return chiTiet; }
}
