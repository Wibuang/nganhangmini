package module.baocao.model;

/** Một dòng báo cáo giao dịch theo ngày hoặc theo tháng. */
public class DongBaoCao {
    private final String ky;          // "2026-10-15" (theo ngày) hoặc "2026-10" (theo tháng)
    private final int soGiaoDich;     // số giao dịch nghiệp vụ (không đếm dòng "chuyển đến" và "phí")
    private final long tongNap;
    private final long tongRut;
    private final long tongChuyen;
    private final long tongPhi;
    private final long tongHoan;

    public DongBaoCao(String ky, int soGiaoDich, long tongNap, long tongRut,
                      long tongChuyen, long tongPhi, long tongHoan) {
        this.ky = ky;
        this.soGiaoDich = soGiaoDich;
        this.tongNap = tongNap;
        this.tongRut = tongRut;
        this.tongChuyen = tongChuyen;
        this.tongPhi = tongPhi;
        this.tongHoan = tongHoan;
    }

    public String getKy() { return ky; }
    public int getSoGiaoDich() { return soGiaoDich; }
    public long getTongNap() { return tongNap; }
    public long getTongRut() { return tongRut; }
    public long getTongChuyen() { return tongChuyen; }
    public long getTongPhi() { return tongPhi; }
    public long getTongHoan() { return tongHoan; }
}
