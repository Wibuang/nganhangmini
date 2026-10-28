package module.khachhang.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import module.khachhang.model.KhachHang;

/** Mọi câu SQL của bảng KhachHang. */
public class KhachHangDAO {
    public static final int TRUNG_DU_LIEU = 0;
    public static final int LOI = -1;

    private static final String COT = "MaKH, HoTen, NgaySinh, CCCD, SoDienThoai, Email, DiaChi, "
            + "TrangThaiKYC, GhiChuKYC, HoatDong, NgayTao ";

    // ---------------------------------------------------------------- tìm
    private KhachHang timMot(String dieuKien, Object thamSo) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT " + COT + "FROM KhachHang " + dieuKien)) {
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

    public KhachHang timTheoMa(int maKH) {
        return timMot("WHERE MaKH = ?", maKH);
    }

    public KhachHang timTheoCCCD(String cccd) {
        return timMot("WHERE CCCD = ?", cccd);
    }

    /** tuKhoa tìm trong họ tên, CCCD, SĐT, email; kyc = null là lấy mọi trạng thái. Tối đa 500 dòng. */
    public ArrayList<KhachHang> tim(String tuKhoa, String kyc) {
        boolean coTuKhoa = tuKhoa != null && !tuKhoa.trim().isEmpty();
        StringBuilder sql = new StringBuilder("SELECT TOP 500 " + COT + "FROM KhachHang WHERE 1 = 1 ");
        if (coTuKhoa) {
            sql.append("AND (HoTen LIKE ? OR CCCD LIKE ? OR SoDienThoai LIKE ? OR Email LIKE ?) ");
        }
        if (kyc != null) {
            sql.append("AND TrangThaiKYC = ? ");
        }
        sql.append("ORDER BY MaKH DESC");

        ArrayList<KhachHang> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (coTuKhoa) {
                String mau = "%" + tuKhoa.trim() + "%";
                for (int k = 0; k < 4; k++) {
                    ps.setString(i++, mau);
                }
            }
            if (kyc != null) {
                ps.setString(i, kyc);
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

    // ---------------------------------------------------------------- thêm / sửa
    /** Thêm hồ sơ mới (KYC = CHO_DUYET). Trả về MaKH (> 0), TRUNG_DU_LIEU (trùng CCCD) hoặc LOI. */
    public int them(KhachHang kh) {
        String sql = "INSERT INTO KhachHang(HoTen, NgaySinh, CCCD, SoDienThoai, Email, DiaChi) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kh.getHoTen());
            ps.setDate(2, Date.valueOf(kh.getNgaySinh()));
            ps.setString(3, kh.getCccd());
            ps.setString(4, kh.getSoDienThoai());
            ps.setString(5, kh.getEmail());
            ps.setString(6, kh.getDiaChi());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
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

    /** Nhân viên sửa họ tên, ngày sinh, liên hệ (không sửa CCCD). */
    public boolean capNhat(KhachHang kh) {
        return chay("UPDATE KhachHang SET HoTen = ?, NgaySinh = ?, SoDienThoai = ?, Email = ?, DiaChi = ? "
                  + "WHERE MaKH = ?", kh.getHoTen(), Date.valueOf(kh.getNgaySinh()),
                kh.getSoDienThoai(), kh.getEmail(), kh.getDiaChi(), kh.getMaKH()) > 0;
    }

    /** Khách hàng tự sửa thông tin liên hệ của mình. */
    public boolean capNhatLienHe(int maKH, String sdt, String email, String diaChi) {
        return chay("UPDATE KhachHang SET SoDienThoai = ?, Email = ?, DiaChi = ? WHERE MaKH = ?",
                sdt, email, diaChi, maKH) > 0;
    }

    public boolean capNhatKYC(int maKH, String trangThai, String ghiChu) {
        return chay("UPDATE KhachHang SET TrangThaiKYC = ?, GhiChuKYC = ? WHERE MaKH = ?",
                trangThai, ghiChu, maKH) > 0;
    }

    public boolean doiHoatDong(int maKH, boolean hoatDong) {
        return chay("UPDATE KhachHang SET HoatDong = ? WHERE MaKH = ?", hoatDong, maKH) > 0;
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

    private KhachHang tao(ResultSet rs) throws SQLException {
        Date ns = rs.getDate("NgaySinh");
        Timestamp nt = rs.getTimestamp("NgayTao");
        LocalDate ngaySinh = ns == null ? null : ns.toLocalDate();
        return new KhachHang(
                rs.getInt("MaKH"),
                rs.getString("HoTen"),
                ngaySinh,
                rs.getString("CCCD"),
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getString("DiaChi"),
                rs.getString("TrangThaiKYC"),
                rs.getString("GhiChuKYC"),
                rs.getBoolean("HoatDong"),
                nt == null ? null : nt.toLocalDateTime());
    }
}
