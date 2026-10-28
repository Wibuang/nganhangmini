package module.giaodich.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import module.giaodich.model.GiaoDich;
import module.giaodich.model.LoaiGD;

/**
 * SỔ CÁI. Đây là nơi DUY NHẤT trong chương trình được thay đổi số dư (cột TaiKhoan.SoDu).
 * Mỗi giao dịch chạy trong MỘT giao dịch JDBC: cập nhật số dư + ghi sổ, hoặc cùng thành công,
 * hoặc hoàn tác cả hai.
 */
public class GiaoDichDAO {
    public static final int KHONG_DU_SO_DU = -2;
    public static final int LOI            = -1;

    private static final String COT = "MaGD, MaThamChieu, MaTK, SoTaiKhoan, LoaiGD, SoTien, SoDuSau, "
            + "NgayGD, Kenh, GhiChu, SoTKDoiTac, MaGDGoc, MaND ";

    /**
     * Ghi một giao dịch tiền vào (NAP, HOAN_RUT) hoặc tiền ra (RUT, HOAN_NAP).
     * Trả về MaGD (> 0) nếu thành công, KHONG_DU_SO_DU nếu rút quá số dư, LOI nếu thất bại.
     * Tài khoản phải đang HOAT_DONG.
     */
    public long ghiSo(int maTK, String soTaiKhoan, String loaiGD, long soTien, String kenh,
                      String ghiChu, Integer maND, Integer maGDGoc) {
        boolean tienVao = LoaiGD.laTienVao(loaiGD);
        Connection conn = MyConnection.getConnection();
        if (conn == null) {
            return LOI;
        }
        try {
            conn.setAutoCommit(false);                       // bắt đầu giao dịch

            // Bước 1: cập nhật số dư. Tiền ra chỉ trừ khi đủ số dư (1 câu SQL vừa kiểm tra vừa trừ)
            String sqlSoDu = tienVao
                    ? "UPDATE TaiKhoan SET SoDu = SoDu + ? WHERE MaTK = ? AND TrangThai = 'HOAT_DONG'"
                    : "UPDATE TaiKhoan SET SoDu = SoDu - ? WHERE MaTK = ? AND TrangThai = 'HOAT_DONG' "
                    + "AND SoDu >= ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlSoDu)) {
                ps.setLong(1, soTien);
                ps.setInt(2, maTK);
                if (!tienVao) {
                    ps.setLong(3, soTien);
                }
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return tienVao ? LOI : KHONG_DU_SO_DU;
                }
            }

            // Bước 2: đọc số dư mới
            long soDuSau;
            try (PreparedStatement ps = conn.prepareStatement("SELECT SoDu FROM TaiKhoan WHERE MaTK = ?")) {
                ps.setInt(1, maTK);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    soDuSau = rs.getLong("SoDu");
                }
            }

            // Bước 3: ghi sổ
            long maGD;
            String sqlGhi = "INSERT INTO GiaoDich(MaTK, SoTaiKhoan, LoaiGD, SoTien, SoDuSau, Kenh, "
                          + "GhiChu, MaGDGoc, MaND) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlGhi, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, maTK);
                ps.setString(2, soTaiKhoan);
                ps.setString(3, loaiGD);
                ps.setLong(4, soTien);
                ps.setLong(5, soDuSau);
                ps.setString(6, kenh);
                ps.setString(7, ghiChu);
                datInteger(ps, 8, maGDGoc);
                datInteger(ps, 9, maND);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    maGD = rs.getLong(1);
                }
            }

            conn.commit();                                   // cả 3 bước OK → lưu thật
            return maGD;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return LOI;
        } finally {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ================================================================ tra cứu
    public GiaoDich timTheoMa(int maGD) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT " + COT + "FROM GiaoDich WHERE MaGD = ?")) {
            ps.setInt(1, maGD);
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

    /** Giao dịch này đã bị đảo chưa? */
    public boolean daDaoNguoc(int maGDGoc) {
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM GiaoDich WHERE MaGDGoc = ?")) {
            ps.setInt(1, maGDGoc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Tìm giao dịch; mỗi điều kiện để null/rỗng nghĩa là "không lọc".
     * maTK: của một tài khoản; tuKhoa: khớp số tài khoản hoặc mã giao dịch (GD...); loai: mã LoaiGD;
     * tuNgay/denNgay: bao gồm cả hai đầu. tangDan = true thì cũ trước mới sau (dùng cho sao kê).
     */
    public ArrayList<GiaoDich> truyVan(Integer maTK, String tuKhoa, String loai, LocalDate tuNgay,
                                       LocalDate denNgay, boolean tangDan, int toiDa) {
        StringBuilder sql = new StringBuilder("SELECT TOP (" + toiDa + ") " + COT + "FROM GiaoDich WHERE 1 = 1 ");
        ArrayList<Object> thamSo = new ArrayList<>();
        if (maTK != null) {
            sql.append("AND MaTK = ? ");
            thamSo.add(maTK);
        }
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (SoTaiKhoan LIKE ? OR MaThamChieu LIKE ?) ");
            String mau = "%" + tuKhoa.trim() + "%";
            thamSo.add(mau);
            thamSo.add(mau);
        }
        if (loai != null) {
            sql.append("AND LoaiGD = ? ");
            thamSo.add(loai);
        }
        if (tuNgay != null) {
            sql.append("AND NgayGD >= ? ");
            thamSo.add(Timestamp.valueOf(tuNgay.atStartOfDay()));
        }
        if (denNgay != null) {
            sql.append("AND NgayGD < ? ");                          // hết ngày "đến ngày"
            thamSo.add(Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay()));
        }
        sql.append(tangDan ? "ORDER BY MaGD ASC" : "ORDER BY MaGD DESC");

        ArrayList<GiaoDich> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
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

    /** Số dư của tài khoản ngay trước 0h ngày tuNgay (= SoDuSau của giao dịch cuối trước đó; 0 nếu chưa có). */
    public long soDuTruocNgay(int maTK, LocalDate tuNgay) {
        String sql = "SELECT TOP 1 SoDuSau FROM GiaoDich WHERE MaTK = ? AND NgayGD < ? ORDER BY MaGD DESC";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setTimestamp(2, Timestamp.valueOf(tuNgay.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    // ================================================================ nội bộ
    private void datInteger(PreparedStatement ps, int vitri, Integer giaTri) throws SQLException {
        if (giaTri == null) {
            ps.setNull(vitri, Types.INTEGER);
        } else {
            ps.setInt(vitri, giaTri);
        }
    }

    private Integer docInteger(ResultSet rs, String cot) throws SQLException {
        int v = rs.getInt(cot);
        return rs.wasNull() ? null : v;
    }

    private GiaoDich tao(ResultSet rs) throws SQLException {
        return new GiaoDich(
                rs.getInt("MaGD"),
                rs.getString("MaThamChieu"),
                rs.getInt("MaTK"),
                rs.getString("SoTaiKhoan"),
                rs.getString("LoaiGD"),
                rs.getLong("SoTien"),
                rs.getLong("SoDuSau"),
                rs.getTimestamp("NgayGD").toLocalDateTime(),
                rs.getString("Kenh"),
                rs.getString("GhiChu"),
                rs.getString("SoTKDoiTac"),
                docInteger(rs, "MaGDGoc"),
                docInteger(rs, "MaND"));
    }
}
