package module.cauhinh.dao;

import core.database.MyConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import module.cauhinh.model.BieuPhi;
import module.cauhinh.model.LaiSuat;
import module.cauhinh.model.ThamSo;

/** SQL của ba bảng cấu hình: ThamSo, BieuPhi, LaiSuat. */
public class CauHinhDAO {

    // ---------------------------------------------------------------- tham số
    /** Giá trị một tham số; null nếu chưa có trong CSDL (hoặc lỗi kết nối). */
    public Long layGiaTri(String ma) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT GiaTri FROM ThamSo WHERE MaThamSo = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ArrayList<ThamSo> layTatCaThamSo() {
        ArrayList<ThamSo> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaThamSo, GiaTri, DonVi, MoTa FROM ThamSo ORDER BY MaThamSo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new ThamSo(rs.getString(1), rs.getLong(2), rs.getString(3), rs.getString(4)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    /** Cập nhật; nếu tham số chưa có trong CSDL thì thêm mới. */
    public boolean datThamSo(String ma, long giaTri) {
        String sql = "UPDATE ThamSo SET GiaTri = ? WHERE MaThamSo = ?; "
                   + "IF @@ROWCOUNT = 0 INSERT INTO ThamSo(MaThamSo, GiaTri) VALUES (?, ?)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, giaTri);
            ps.setString(2, ma);
            ps.setString(3, ma);
            ps.setLong(4, giaTri);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------------- biểu phí
    public ArrayList<BieuPhi> layBieuPhi(String loaiPhi) {
        ArrayList<BieuPhi> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaPhi, LoaiPhi, TuSoTien, DenSoTien, Phi FROM BieuPhi "
                   + "WHERE LoaiPhi = ? ORDER BY TuSoTien")) {
            ps.setString(1, loaiPhi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long den = rs.getLong("DenSoTien");
                    Long denSo = rs.wasNull() ? null : den;
                    ds.add(new BieuPhi(rs.getInt("MaPhi"), rs.getString("LoaiPhi"),
                            rs.getLong("TuSoTien"), denSo, rs.getLong("Phi")));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    /** Phí của bậc chứa soTien; null nếu không có bậc nào (hoặc lỗi kết nối). */
    public Long phiTheoSoTien(String loaiPhi, long soTien) {
        String sql = "SELECT TOP 1 Phi FROM BieuPhi WHERE LoaiPhi = ? AND ? >= TuSoTien "
                   + "AND (DenSoTien IS NULL OR ? <= DenSoTien) ORDER BY TuSoTien DESC";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loaiPhi);
            ps.setLong(2, soTien);
            ps.setLong(3, soTien);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean capNhatPhi(int maPhi, long phi) {
        return chay("UPDATE BieuPhi SET Phi = ? WHERE MaPhi = ?", phi, maPhi);
    }

    // ---------------------------------------------------------------- lãi suất
    public ArrayList<LaiSuat> layLaiSuat() {
        ArrayList<LaiSuat> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaLS, LoaiLai, KyHanThang, LaiSuatNam FROM LaiSuat ORDER BY LoaiLai DESC, KyHanThang");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new LaiSuat(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    public boolean capNhatLaiSuat(int maLS, BigDecimal laiSuat) {
        return chay("UPDATE LaiSuat SET LaiSuatNam = ? WHERE MaLS = ?", laiSuat, maLS);
    }

    private boolean chay(String sql, Object... thamSo) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < thamSo.length; i++) {
                ps.setObject(i + 1, thamSo[i]);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
