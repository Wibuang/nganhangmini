package module.baocao.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import module.baocao.model.ThongBao;

/** SQL của bảng ThongBao. */
public class ThongBaoDAO {

    public boolean them(int maKH, String tieuDe, String noiDung) {
        String sql = "INSERT INTO ThongBao(MaKH, TieuDe, NoiDung) VALUES (?, ?, ?)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKH);
            ps.setString(2, tieuDe);
            ps.setString(3, noiDung);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Thông báo của một khách hàng, mới nhất trước. */
    public ArrayList<ThongBao> theoKhachHang(int maKH, int toiDa) {
        String sql = "SELECT TOP (" + toiDa + ") MaTB, TieuDe, NoiDung, ThoiGian, DaDoc "
                   + "FROM ThongBao WHERE MaKH = ? ORDER BY MaTB DESC";
        ArrayList<ThongBao> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ThongBao(rs.getInt("MaTB"), rs.getString("TieuDe"),
                            rs.getString("NoiDung"), rs.getTimestamp("ThoiGian").toLocalDateTime(),
                            rs.getBoolean("DaDoc")));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    public int demChuaDoc(int maKH) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM ThongBao WHERE MaKH = ? AND DaDoc = 0")) {
            ps.setInt(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public boolean danhDauTatCaDaDoc(int maKH) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE ThongBao SET DaDoc = 1 WHERE MaKH = ? AND DaDoc = 0")) {
            ps.setInt(1, maKH);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
