package module.chuyenkhoan.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import module.chuyenkhoan.model.LenhDinhKy;

/** SQL của bảng LenhDinhKy (lệnh chuyển tiền định kỳ). */
public class LenhDinhKyDAO {
    public static final int LOI = -1;

    private static final String COT = "MaLenh, MaKH, MaTKNguon, SoTKNguon, SoTKNhan, TenNguoiNhan, SoTien, "
            + "NoiDung, TanSuat, NgayBatDau, NgayKetThuc, NgayChayKeTiep, TrangThai, SoLanChay, KetQuaLanCuoi ";

    /** Thêm lệnh mới (trạng thái HOAT_DONG, chạy lần đầu vào ngày bắt đầu). Trả về MaLenh hoặc LOI. */
    public int them(int maKH, int maTKNguon, String soTKNguon, String soTKNhan, String tenNguoiNhan,
                    long soTien, String noiDung, String tanSuat, LocalDate batDau, LocalDate ketThuc) {
        String sql = "INSERT INTO LenhDinhKy(MaKH, MaTKNguon, SoTKNguon, SoTKNhan, TenNguoiNhan, SoTien, "
                   + "NoiDung, TanSuat, NgayBatDau, NgayKetThuc, NgayChayKeTiep) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maKH);
            ps.setInt(2, maTKNguon);
            ps.setString(3, soTKNguon);
            ps.setString(4, soTKNhan);
            ps.setString(5, tenNguoiNhan);
            ps.setLong(6, soTien);
            ps.setString(7, noiDung);
            ps.setString(8, tanSuat);
            ps.setDate(9, Date.valueOf(batDau));
            if (ketThuc == null) {
                ps.setNull(10, Types.DATE);
            } else {
                ps.setDate(10, Date.valueOf(ketThuc));
            }
            ps.setDate(11, Date.valueOf(batDau));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return LOI;
        }
    }

    public ArrayList<LenhDinhKy> theoKhachHang(int maKH) {
        return truyVan("WHERE MaKH = ? ORDER BY MaLenh DESC", maKH);
    }

    /** Mọi lệnh (tối đa 500), trangThai = null là lấy tất cả. */
    public ArrayList<LenhDinhKy> tatCa(String trangThai) {
        if (trangThai == null) {
            return truyVan("ORDER BY MaLenh DESC");
        }
        return truyVan("WHERE TrangThai = ? ORDER BY MaLenh DESC", trangThai);
    }

    /** Các lệnh đang chạy mà ngày chạy kế tiếp đã đến hoặc đã qua. */
    public ArrayList<LenhDinhKy> denHan(LocalDate ngay) {
        return truyVan("WHERE TrangThai = 'HOAT_DONG' AND NgayChayKeTiep <= ? ORDER BY MaLenh",
                Date.valueOf(ngay));
    }

    /** Đổi trạng thái; điều kiện MaKH bảo đảm khách chỉ đổi được lệnh của mình. */
    public boolean doiTrangThai(int maLenh, int maKH, String trangThai) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE LenhDinhKy SET TrangThai = ? WHERE MaLenh = ? AND MaKH = ? "
                   + "AND TrangThai IN ('HOAT_DONG', 'TAM_DUNG')")) {
            ps.setString(1, trangThai);
            ps.setInt(2, maLenh);
            ps.setInt(3, maKH);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Đặt lại ngày chạy kế tiếp (dùng khi tiếp tục một lệnh đã tạm dừng lâu). */
    public boolean datNgayChayKeTiep(int maLenh, int maKH, LocalDate ngay) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE LenhDinhKy SET NgayChayKeTiep = ? WHERE MaLenh = ? AND MaKH = ?")) {
            ps.setDate(1, Date.valueOf(ngay));
            ps.setInt(2, maLenh);
            ps.setInt(3, maKH);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Ghi kết quả sau một lần chạy: ngày chạy kế tiếp, trạng thái mới, kết quả; thành công thì tăng số lần chạy. */
    public boolean capNhatSauChay(int maLenh, LocalDate ngayKeTiep, String trangThai,
                                  String ketQua, boolean thanhCong) {
        String sql = "UPDATE LenhDinhKy SET NgayChayKeTiep = ?, TrangThai = ?, KetQuaLanCuoi = ?, "
                   + "SoLanChay = SoLanChay + ? WHERE MaLenh = ?";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngayKeTiep));
            ps.setString(2, trangThai);
            ps.setString(3, ketQua);
            ps.setInt(4, thanhCong ? 1 : 0);
            ps.setInt(5, maLenh);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private ArrayList<LenhDinhKy> truyVan(String phanSau, Object... thamSo) {
        ArrayList<LenhDinhKy> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT TOP 500 " + COT + "FROM LenhDinhKy " + phanSau)) {
            for (int i = 0; i < thamSo.length; i++) {
                ps.setObject(i + 1, thamSo[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(tao(rs));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    private LenhDinhKy tao(ResultSet rs) throws SQLException {
        Date kt = rs.getDate("NgayKetThuc");
        return new LenhDinhKy(
                rs.getInt("MaLenh"), rs.getInt("MaKH"), rs.getInt("MaTKNguon"), rs.getString("SoTKNguon"),
                rs.getString("SoTKNhan"), rs.getString("TenNguoiNhan"), rs.getLong("SoTien"),
                rs.getString("NoiDung"), rs.getString("TanSuat"),
                rs.getDate("NgayBatDau").toLocalDate(), kt == null ? null : kt.toLocalDate(),
                rs.getDate("NgayChayKeTiep").toLocalDate(), rs.getString("TrangThai"),
                rs.getInt("SoLanChay"), rs.getString("KetQuaLanCuoi"));
    }
}
