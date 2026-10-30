package module.chuyenkhoan.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import module.chuyenkhoan.model.NguoiThuHuong;

/** SQL của bảng NguoiThuHuong (danh bạ người nhận). */
public class NguoiThuHuongDAO {
    public static final int THANH_CONG    = 1;
    public static final int TRUNG_DU_LIEU = 0;
    public static final int LOI           = -1;

    public ArrayList<NguoiThuHuong> theoKhachHang(int maKH) {
        ArrayList<NguoiThuHuong> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaNTH, SoTaiKhoan, TenGoiNho, TenNguoiNhan FROM NguoiThuHuong "
                   + "WHERE MaKH = ? ORDER BY TenGoiNho")) {
            ps.setInt(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new NguoiThuHuong(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    public int them(int maKH, String soTaiKhoan, String tenGoiNho, String tenNguoiNhan) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO NguoiThuHuong(MaKH, SoTaiKhoan, TenGoiNho, TenNguoiNhan) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, maKH);
            ps.setString(2, soTaiKhoan);
            ps.setString(3, tenGoiNho);
            ps.setString(4, tenNguoiNhan);
            ps.executeUpdate();
            return THANH_CONG;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {   // đã có trong danh bạ
                return TRUNG_DU_LIEU;
            }
            e.printStackTrace();
            return LOI;
        } catch (Exception e) {
            e.printStackTrace();
            return LOI;
        }
    }

    /** Xóa một người nhận; điều kiện MaKH bảo đảm khách chỉ xóa được danh bạ của mình. */
    public boolean xoa(int maNTH, int maKH) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM NguoiThuHuong WHERE MaNTH = ? AND MaKH = ?")) {
            ps.setInt(1, maNTH);
            ps.setInt(2, maKH);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
