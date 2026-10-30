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
 * hoặc hoàn tác tất cả.
 */
public class GiaoDichDAO {
    public static final int KHONG_DU_SO_DU = -2;
    public static final int LOI            = -1;

    private static final String COT = "MaGD, MaThamChieu, MaTK, SoTaiKhoan, LoaiGD, SoTien, SoDuSau, "
            + "NgayGD, Kenh, GhiChu, SoTKDoiTac, MaGDGoc, MaNhom, MaND ";

    // ================================================================ NẠP / RÚT / ĐẢO
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

            if (tienVao) {
                congTien(conn, maTK, soTien);                // ném lỗi nếu tài khoản không hoạt động
            } else if (!truTien(conn, maTK, soTien)) {       // 1 câu SQL vừa kiểm tra đủ tiền vừa trừ
                conn.rollback();
                return KHONG_DU_SO_DU;
            }
            long soDuSau = docSoDu(conn, maTK);
            long maGD = ghiDong(conn, maTK, soTaiKhoan, loaiGD, soTien, soDuSau, kenh, ghiChu,
                    null, maGDGoc, null, maND);

            conn.commit();                                   // mọi bước OK → lưu thật
            return maGD;
        } catch (SQLException e) {
            e.printStackTrace();
            huy(conn);
            return LOI;
        } finally {
            dong(conn);
        }
    }

    // ================================================================ CHUYỂN KHOẢN
    /**
     * Chuyển khoản trong MỘT giao dịch: trừ (soTien + phi) của người gửi, cộng soTien cho người nhận,
     * ghi sổ 3 dòng: CHUYEN_DI + PHI (bên gửi) và CHUYEN_DEN (bên nhận).
     * Luôn cập nhật tài khoản có MaTK NHỎ HƠN trước để hai người chuyển cho nhau cùng lúc
     * không bị khóa chéo (deadlock).
     * Trả về MaGD của dòng CHUYEN_DI (> 0), KHONG_DU_SO_DU hoặc LOI.
     */
    public long chuyenKhoan(int maTKGui, String soTKGui, int maTKNhan, String soTKNhan, long soTien,
                            long phi, String noiDung, String kenh, Integer maND) {
        Connection conn = MyConnection.getConnection();
        if (conn == null) {
            return LOI;
        }
        try {
            conn.setAutoCommit(false);
            long tongTru = soTien + phi;

            if (maTKGui < maTKNhan) {
                if (!truTien(conn, maTKGui, tongTru)) {
                    conn.rollback();
                    return KHONG_DU_SO_DU;
                }
                congTien(conn, maTKNhan, soTien);
            } else {
                congTien(conn, maTKNhan, soTien);
                if (!truTien(conn, maTKGui, tongTru)) {
                    conn.rollback();                         // hoàn tác cả khoản đã cộng cho người nhận
                    return KHONG_DU_SO_DU;
                }
            }

            long duGui = docSoDu(conn, maTKGui);             // sau khi đã trừ cả phí
            long duNhan = docSoDu(conn, maTKNhan);
            // Sổ cái ghi theo đúng thứ tự: chuyển đi (chưa trừ phí) → phí → bên nhận
            long maDi = ghiDong(conn, maTKGui, soTKGui, LoaiGD.CHUYEN_DI, soTien, duGui + phi,
                    kenh, noiDung, soTKNhan, null, null, maND);
            if (phi > 0) {
                ghiDong(conn, maTKGui, soTKGui, LoaiGD.PHI, phi, duGui, kenh,
                        "Phí chuyển khoản", null, null, (int) maDi, maND);
            }
            ghiDong(conn, maTKNhan, soTKNhan, LoaiGD.CHUYEN_DEN, soTien, duNhan, kenh,
                    noiDung, soTKGui, null, (int) maDi, maND);

            conn.commit();
            return maDi;
        } catch (SQLException e) {
            e.printStackTrace();
            huy(conn);
            return LOI;
        } finally {
            dong(conn);
        }
    }

    /** Tổng số tiền đã chuyển đi (không tính phí) của tài khoản trong một ngày, để kiểm tra hạn mức ngày. */
    public long tongChuyenDiTrongNgay(int maTK, LocalDate ngay) {
        String sql = "SELECT ISNULL(SUM(SoTien), 0) FROM GiaoDich WHERE MaTK = ? AND LoaiGD = 'CHUYEN_DI' "
                   + "AND NgayGD >= ? AND NgayGD < ?";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setTimestamp(2, Timestamp.valueOf(ngay.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(ngay.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
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

    // ================================================================ hàm dùng chung trong 1 giao dịch
    /** Trừ tiền nếu đủ số dư và tài khoản đang hoạt động. false = không đủ tiền (hoặc tài khoản không hoạt động). */
    private boolean truTien(Connection conn, int maTK, long soTien) throws SQLException {
        String sql = "UPDATE TaiKhoan SET SoDu = SoDu - ? "
                   + "WHERE MaTK = ? AND TrangThai = 'HOAT_DONG' AND SoDu >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, soTien);
            ps.setInt(2, maTK);
            ps.setLong(3, soTien);
            return ps.executeUpdate() > 0;
        }
    }

    /** Cộng tiền; ném lỗi nếu tài khoản không tồn tại hoặc không hoạt động (để hoàn tác cả giao dịch). */
    private void congTien(Connection conn, int maTK, long soTien) throws SQLException {
        String sql = "UPDATE TaiKhoan SET SoDu = SoDu + ? WHERE MaTK = ? AND TrangThai = 'HOAT_DONG'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, soTien);
            ps.setInt(2, maTK);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Tài khoản " + maTK + " không tồn tại hoặc không hoạt động");
            }
        }
    }

    private long docSoDu(Connection conn, int maTK) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT SoDu FROM TaiKhoan WHERE MaTK = ?")) {
            ps.setInt(1, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("SoDu");
            }
        }
    }

    /** Ghi một dòng vào sổ cái, trả về MaGD vừa tạo. */
    private long ghiDong(Connection conn, int maTK, String soTaiKhoan, String loaiGD, long soTien,
                         long soDuSau, String kenh, String ghiChu, String soTKDoiTac, Integer maGDGoc,
                         Integer maNhom, Integer maND) throws SQLException {
        String sql = "INSERT INTO GiaoDich(MaTK, SoTaiKhoan, LoaiGD, SoTien, SoDuSau, Kenh, GhiChu, "
                   + "SoTKDoiTac, MaGDGoc, MaNhom, MaND) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maTK);
            ps.setString(2, soTaiKhoan);
            ps.setString(3, loaiGD);
            ps.setLong(4, soTien);
            ps.setLong(5, soDuSau);
            ps.setString(6, kenh);
            ps.setString(7, ghiChu);
            ps.setString(8, soTKDoiTac);
            datInteger(ps, 9, maGDGoc);
            datInteger(ps, 10, maNhom);
            datInteger(ps, 11, maND);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void datInteger(PreparedStatement ps, int viTri, Integer giaTri) throws SQLException {
        if (giaTri == null) {
            ps.setNull(viTri, Types.INTEGER);
        } else {
            ps.setInt(viTri, giaTri);
        }
    }

    private Integer docInteger(ResultSet rs, String cot) throws SQLException {
        int v = rs.getInt(cot);
        return rs.wasNull() ? null : v;
    }

    private void huy(Connection conn) {
        try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void dong(Connection conn) {
        try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
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
                docInteger(rs, "MaNhom"),
                docInteger(rs, "MaND"));
    }
}
