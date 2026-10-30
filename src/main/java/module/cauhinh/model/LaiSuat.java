package module.cauhinh.model;

/** Lãi suất năm (%) theo loại và kỳ hạn; kỳ hạn 0 = không kỳ hạn. */
public class LaiSuat {
    public static final String TIET_KIEM = "TIET_KIEM";
    public static final String VAY = "VAY";

    private final int maLS;
    private final String loaiLai;
    private final int kyHanThang;
    private final double laiSuatNam;

    public LaiSuat(int maLS, String loaiLai, int kyHanThang, double laiSuatNam) {
        this.maLS = maLS;
        this.loaiLai = loaiLai;
        this.kyHanThang = kyHanThang;
        this.laiSuatNam = laiSuatNam;
    }

    public int getMaLS() { return maLS; }
    public String getLoaiLai() { return loaiLai; }
    public int getKyHanThang() { return kyHanThang; }
    public double getLaiSuatNam() { return laiSuatNam; }
}
