package module.taikhoan.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import module.taikhoan.model.TaiKhoan;

/**
 * Mọi câu SQL về vòng đời tài khoản (mở, đóng băng, đóng, đổi tên).
 * Cột SoDu KHÔNG được sửa ở đây: chỉ module Giao dịch (sổ cái) được thay đổi số dư.
 */
public class TaiKhoanDAO {
    public static final int LOI = -1;

    private static final String SELECT =
            "SELECT MaTK, SoTaiKhoan, MaKH, TenGoiNho, SoDu, TrangThai, NgayMo, NgayDong FROM TaiKhoan ";

    // ---------------------------------------------------------------- tìm
    private TaiKhoan timMot(String dieuKien, Object thamSo) {
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

    public TaiKhoan timTheoMa(int maTK) {
        return timMot("WHERE MaTK = ?", maTK);
    }

    public TaiKhoan timTheoSo(String soTaiKhoan) {
        return timMot("WHERE SoTaiKhoan = ?", soTaiKhoan);
    }

    /** Các tài khoản của một khách hàng (không tính tài khoản đã đóng nếu boQuaDaDong = true). */
    public ArrayList<TaiKhoan> theoKhachHang(int maKH, boolean boQuaDaDong) {
        String sql = SELECT + "WHERE MaKH = ? " + (boQuaDaDong ? "AND TrangThai <> 'DA_DONG' " : "")
                   + "ORDER BY MaTK";
        ArrayList<TaiKhoan> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKH);
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

    /**
     * Tìm cho nhân viên: tuKhoa khớp số tài khoản, hoặc tài khoản thuộc một trong các khách hàng
     * dsMaKH (do controller tìm từ module Khách hàng). trangThai = null là lấy tất cả.
     * Tối đa 500 dòng.
     */
    public ArrayList<TaiKhoan> tim(String tuKhoa, ArrayList<Integer> dsMaKH, String trangThai) {
        boolean coTuKhoa = tuKhoa != null && !tuKhoa.trim().isEmpty();
        StringBuilder sql = new StringBuilder(
                "SELECT TOP 500 MaTK, SoTaiKhoan, MaKH, TenGoiNho, SoDu, TrangThai, NgayMo, NgayDong "
              + "FROM TaiKhoan WHERE 1 = 1 ");
        if (coTuKhoa) {
            sql.append("AND (SoTaiKhoan LIKE ? ");
            if (!dsMaKH.isEmpty()) {
                sql.append("OR MaKH IN (");
                for (int i = 0; i < dsMaKH.size(); i++) {
                    sql.append(i == 0 ? "?" : ", ?");
                }
                sql.append(") ");
            }
            sql.append(") ");
        }
        if (trangThai != null) {
            sql.append("AND TrangThai = ? ");
        }
        sql.append("ORDER BY MaTK DESC");

        ArrayList<TaiKhoan> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (coTuKhoa) {
                ps.setString(i++, "%" + tuKhoa.trim() + "%");
                for (Integer ma : dsMaKH) {
                    ps.setInt(i++, ma);
                }
            }
            if (trangThai != null) {
                ps.setString(i, trangThai);
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

    // ---------------------------------------------------------------- thay đổi
    /**
     * Mở tài khoản mới với số dư 0; số tài khoản = số lớn nhất hiện có + 1 (10 chữ số).
     * Khóa HOLDLOCK giữ cho hai nhân viên mở cùng lúc không bị trùng số.
     * Trả về MaTK vừa tạo hoặc LOI.
     */
    public int moTaiKhoan(int maKH, String tenGoiNho) {
        String sql = "INSERT INTO TaiKhoan(SoTaiKhoan, MaKH, TenGoiNho) "
                   + "SELECT CAST(ISNULL(MAX(CAST(SoTaiKhoan AS BIGINT)), 1000000000) + 1 AS VARCHAR(20)), ?, ? "
                   + "FROM TaiKhoan WITH (UPDLOCK, HOLDLOCK)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maKH);
            ps.setString(2, tenGoiNho);
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

    /** Đóng băng / mở băng. Không áp dụng cho tài khoản đã đóng. */
    public boolean doiTrangThai(int maTK, String trangThaiMoi) {
        return chay("UPDATE TaiKhoan SET TrangThai = ? WHERE MaTK = ? AND TrangThai <> 'DA_DONG'",
                trangThaiMoi, maTK) > 0;
    }

    /** Đóng tài khoản; chỉ thành công khi số dư bằng 0 và tài khoản chưa đóng. */
    public boolean dongTaiKhoan(int maTK) {
        return chay("UPDATE TaiKhoan SET TrangThai = 'DA_DONG', NgayDong = GETDATE() "
                  + "WHERE MaTK = ? AND SoDu = 0 AND TrangThai <> 'DA_DONG'", maTK) > 0;
    }

    public boolean doiTenGoiNho(int maTK, String tenGoiNho) {
        return chay("UPDATE TaiKhoan SET TenGoiNho = ? WHERE MaTK = ?", tenGoiNho, maTK) > 0;
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

    private TaiKhoan tao(ResultSet rs) throws SQLException {
        Timestamp mo = rs.getTimestamp("NgayMo");
        Timestamp dong = rs.getTimestamp("NgayDong");
        return new TaiKhoan(
                rs.getInt("MaTK"),
                rs.getString("SoTaiKhoan"),
                rs.getInt("MaKH"),
                rs.getString("TenGoiNho"),
                rs.getLong("SoDu"),
                rs.getString("TrangThai"),
                mo == null ? null : mo.toLocalDateTime(),
                dong == null ? null : dong.toLocalDateTime());
    }
}
