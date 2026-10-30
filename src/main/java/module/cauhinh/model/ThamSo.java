package module.cauhinh.model;

/** Một tham số hệ thống đọc từ CSDL. */
public class ThamSo {
    private final String ma;
    private final long giaTri;
    private final String donVi;
    private final String moTa;

    public ThamSo(String ma, long giaTri, String donVi, String moTa) {
        this.ma = ma;
        this.giaTri = giaTri;
        this.donVi = donVi;
        this.moTa = moTa;
    }

    public String getMa() { return ma; }
    public long getGiaTri() { return giaTri; }
    public String getDonVi() { return donVi; }
    public String getMoTa() { return moTa; }
}
