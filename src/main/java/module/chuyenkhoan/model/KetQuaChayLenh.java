package module.chuyenkhoan.model;

import java.util.ArrayList;

/** Tổng kết một lần chạy các lệnh định kỳ đến hạn. */
public class KetQuaChayLenh {
    private int soLenh;
    private int thanhCong;
    private int thatBai;
    private final ArrayList<String> chiTiet = new ArrayList<>();

    public void them(boolean ok, String dong) {
        soLenh++;
        if (ok) {
            thanhCong++;
        } else {
            thatBai++;
        }
        chiTiet.add(dong);
    }

    public int getSoLenh() { return soLenh; }
    public int getThanhCong() { return thanhCong; }
    public int getThatBai() { return thatBai; }
    public ArrayList<String> getChiTiet() { return chiTiet; }
}
