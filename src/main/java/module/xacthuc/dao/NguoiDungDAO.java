package module.xacthuc.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import module.xacthuc.model.NguoiDung;

/** Mọi câu SQL của bảng NguoiDung. */
public class NguoiDungDAO {
    public static final int THANH_CONG    = 1;
    public static final int TRUNG_DU_LIEU = 0;
    public static final int LOI           = -1;

    private static final String SELECT =
            "SELECT MaND, TenDangNhap, HoTen, VaiTro, TrangThai, SoLanSai, MaKH, NgayTao FROM NguoiDung ";

    // ---------------------------------------------------------------- tìm
    private NguoiDung timMot(String dieuKien, Object thamSo) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + dieuKien)) {
            ps.setObject(1, thamSo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return tao(rs);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public NguoiDung timTheoTenDangNhap(String tenDangNhap) {
        return timMot("WHERE TenDangNhap = ?", tenDangNhap);
    }

    public NguoiDung timTheoMa(int maND) {
        return timMot("WHERE MaND = ?", maND);
    }

    public NguoiDung timTheoMaKH(int maKH) {
        return timMot("WHERE MaKH = ?", maKH);
    }

    public ArrayList<NguoiDung> getAll(String tuKhoa) {
        boolean coTuKhoa = tuKhoa != null && !tuKhoa.trim().isEmpty();
        String sql = SELECT + (coTuKhoa ? "WHERE TenDangNhap LIKE ? OR HoTen LIKE ? " : "")
                   + "ORDER BY MaND";
        ArrayList<NguoiDung> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (coTuKhoa) {
                String mau = "%" + tuKhoa.trim() + "%";
                ps.setString(1, mau);
                ps.setString(2, mau);
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

    // ---------------------------------------------------------------- mật khẩu, khóa
    /** Mật khẩu (đã băm) có khớp với người dùng không? */
    public boolean kiemTraMatKhau(int maND, String matKhauDaBam) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM NguoiDung WHERE MaND = ? AND MatKhau = ?")) {
            ps.setInt(1, maND);
            ps.setString(2, matKhauDaBam);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Đổi mật khẩu (đồng thời đặt lại bộ đếm nhập sai). */
    public boolean doiMatKhau(int maND, String matKhauMoiDaBam) {
        return chay("UPDATE NguoiDung SET MatKhau = ?, SoLanSai = 0 WHERE MaND = ?",
                matKhauMoiDaBam, maND) > 0;
    }

    /** Tăng bộ đếm nhập sai và trả về số lần sai hiện tại. */
    public int tangSoLanSai(int maND) {
        chay("UPDATE NguoiDung SET SoLanSai = SoLanSai + 1 WHERE MaND = ?", maND);
        NguoiDung nd = timTheoMa(maND);
        return nd == null ? 0 : nd.getSoLanSai();
    }

    public void resetSoLanSai(int maND) {
        chay("UPDATE NguoiDung SET SoLanSai = 0 WHERE MaND = ?", maND);
    }

    /** Khóa (false) hoặc mở khóa (true); mở khóa thì xóa bộ đếm sai. */
    public boolean doiTrangThai(int maND, boolean hoatDong) {
        return chay("UPDATE NguoiDung SET TrangThai = ?, SoLanSai = 0 WHERE MaND = ?",
                hoatDong, maND) > 0;
    }

    // ---------------------------------------------------------------- thêm
    /** Thêm người dùng. hoTen dùng cho nhân viên; maKH dùng cho khách hàng (cái còn lại để null). */
    public int them(String tenDangNhap, String matKhauDaBam, String hoTen, String vaiTro, Integer maKH) {
        String sql = "INSERT INTO NguoiDung(TenDangNhap, MatKhau, HoTen, VaiTro, MaKH) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhauDaBam);
            if (hoTen == null) {
                ps.setNull(3, Types.NVARCHAR);
            } else {
                ps.setString(3, hoTen);
            }
            ps.setString(4, vaiTro);
            if (maKH == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, maKH);
            }
            ps.executeUpdate();
            return THANH_CONG;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {   // vi phạm UNIQUE
                return TRUNG_DU_LIEU;
            }
            e.printStackTrace();
            return LOI;
        } catch (Exception e) {
            e.printStackTrace();
            return LOI;
        }
    }

    // ---------------------------------------------------------------- nội bộ
    private int chay(String sql, Object... thamSo) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < thamSo.length; i++) {
                ps.setObject(i + 1, thamSo[i]);
            }
            return ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    private NguoiDung tao(ResultSet rs) throws SQLException {
        int mk = rs.getInt("MaKH");
        Integer maKH = rs.wasNull() ? null : mk;
        Timestamp nt = rs.getTimestamp("NgayTao");
        return new NguoiDung(
                rs.getInt("MaND"),
                rs.getString("TenDangNhap"),
                rs.getString("HoTen"),
                rs.getString("VaiTro"),
                rs.getBoolean("TrangThai"),
                rs.getInt("SoLanSai"),
                maKH,
                nt == null ? null : nt.toLocalDateTime());
    }
}
