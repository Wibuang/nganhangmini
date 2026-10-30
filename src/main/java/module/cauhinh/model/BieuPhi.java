package module.cauhinh.model;

/** Một bậc phí: số tiền trong khoảng [tuSoTien, denSoTien] thì thu phi. */
public class BieuPhi {
    public static final String CHUYEN_KHOAN = "CHUYEN_KHOAN";

    private final int maPhi;
    private final String loaiPhi;
    private final long tuSoTien;
    private final Long denSoTien;       // null = không giới hạn
    private final long phi;

    public BieuPhi(int maPhi, String loaiPhi, long tuSoTien, Long denSoTien, long phi) {
        this.maPhi = maPhi;
        this.loaiPhi = loaiPhi;
        this.tuSoTien = tuSoTien;
        this.denSoTien = denSoTien;
        this.phi = phi;
    }

    public int getMaPhi() { return maPhi; }
    public String getLoaiPhi() { return loaiPhi; }
    public long getTuSoTien() { return tuSoTien; }
    public Long getDenSoTien() { return denSoTien; }
    public long getPhi() { return phi; }
}
