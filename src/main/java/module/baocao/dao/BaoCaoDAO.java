package module.baocao.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import module.baocao.model.ChenhLech;
import module.baocao.model.DoiSoat;
import module.baocao.model.DongBaoCao;
import module.baocao.model.DongTopSoDu;
import module.baocao.model.ThongKe;

/**
 * Các truy vấn BÁO CÁO. Module này CHỈ ĐỌC dữ liệu (SELECT) của các module khác để tổng hợp,
 * không bao giờ INSERT / UPDATE / DELETE vào bảng của module khác.
 */
public class BaoCaoDAO {
    // Giao dịch làm tăng số dư / giảm số dư (khớp với model LoaiGD của module Giao dịch)
    private static final String TIEN_VAO = "('NAP', 'CHUYEN_DEN', 'HOAN_RUT')";
    private static final String HOM_NAY = "CAST(NgayGD AS DATE) = CAST(GETDATE() AS DATE)";

    public ThongKe layThongKe() {
        String sql = "SELECT "
            + "(SELECT COUNT(*) FROM KhachHang) AS TongKH, "
            + "(SELECT COUNT(*) FROM TaiKhoan) AS TongTK, "
            + "(SELECT COUNT(*) FROM TaiKhoan WHERE TrangThai = 'HOAT_DONG') AS TKHoatDong, "
            + "(SELECT ISNULL(SUM(SoDu), 0) FROM TaiKhoan) AS TongSoDu, "
            + "(SELECT COUNT(*) FROM GiaoDich WHERE LoaiGD NOT IN ('CHUYEN_DEN', 'PHI') AND " + HOM_NAY + ") AS GDHomNay, "
            + "(SELECT ISNULL(SUM(SoTien), 0) FROM GiaoDich WHERE LoaiGD = 'NAP' AND " + HOM_NAY + ") AS NapHomNay, "
            + "(SELECT ISNULL(SUM(SoTien), 0) FROM GiaoDich WHERE LoaiGD = 'RUT' AND " + HOM_NAY + ") AS RutHomNay, "
            + "(SELECT ISNULL(SUM(SoTien), 0) FROM GiaoDich WHERE LoaiGD = 'CHUYEN_DI' AND " + HOM_NAY + ") AS ChuyenHomNay, "
            + "(SELECT ISNULL(SUM(SoTien), 0) FROM GiaoDich WHERE LoaiGD = 'PHI' AND " + HOM_NAY + ") AS PhiHomNay";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new ThongKe(rs.getInt("TongKH"), rs.getInt("TongTK"), rs.getInt("TKHoatDong"),
                        rs.getLong("TongSoDu"), rs.getInt("GDHomNay"), rs.getLong("NapHomNay"),
                        rs.getLong("RutHomNay"), rs.getLong("ChuyenHomNay"), rs.getLong("PhiHomNay"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ThongKe(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public ArrayList<DongTopSoDu> topSoDu(int soLuong) {
        String sql = "SELECT TOP (" + soLuong + ") t.SoTaiKhoan, k.HoTen, t.SoDu "
                   + "FROM TaiKhoan t JOIN KhachHang k ON t.MaKH = k.MaKH "
                   + "WHERE t.TrangThai <> 'DA_DONG' ORDER BY t.SoDu DESC";
        ArrayList<DongTopSoDu> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new DongTopSoDu(rs.getString(1), rs.getString(2), rs.getLong(3)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    /** Báo cáo giao dịch theo ngày hoặc theo tháng trong khoảng [tuNgay, denNgay]. */
    public ArrayList<DongBaoCao> baoCaoTheoKy(LocalDate tuNgay, LocalDate denNgay, boolean theoThang) {
        // 'yyyy-MM-dd' (kiểu 23) hoặc 'yyyy-MM' (7 ký tự đầu)
        String ky = theoThang ? "LEFT(CONVERT(VARCHAR(10), NgayGD, 23), 7)" : "CONVERT(VARCHAR(10), NgayGD, 23)";
        String sql = "SELECT " + ky + " AS Ky, "
            + "SUM(CASE WHEN LoaiGD NOT IN ('CHUYEN_DEN', 'PHI') THEN 1 ELSE 0 END) AS SoGD, "
            + "SUM(CASE WHEN LoaiGD = 'NAP' THEN SoTien ELSE 0 END) AS Nap, "
            + "SUM(CASE WHEN LoaiGD = 'RUT' THEN SoTien ELSE 0 END) AS Rut, "
            + "SUM(CASE WHEN LoaiGD = 'CHUYEN_DI' THEN SoTien ELSE 0 END) AS Chuyen, "
            + "SUM(CASE WHEN LoaiGD = 'PHI' THEN SoTien ELSE 0 END) AS Phi, "
            + "SUM(CASE WHEN LoaiGD IN ('HOAN_NAP', 'HOAN_RUT') THEN SoTien ELSE 0 END) AS Hoan "
            + "FROM GiaoDich WHERE NgayGD >= ? AND NgayGD < ? GROUP BY " + ky + " ORDER BY " + ky;
        ArrayList<DongBaoCao> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(tuNgay.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new DongBaoCao(rs.getString("Ky"), rs.getInt("SoGD"), rs.getLong("Nap"),
                            rs.getLong("Rut"), rs.getLong("Chuyen"), rs.getLong("Phi"), rs.getLong("Hoan")));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }

    /** Đối soát: số dư các tài khoản phải khớp với sổ cái. */
    public DoiSoat doiSoat() {
        long tongSoDu = 0;
        long tongSoCai = 0;
        int soTK = 0;
        ArrayList<ChenhLech> lech = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*), ISNULL(SUM(SoDu), 0) FROM TaiKhoan");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                soTK = rs.getInt(1);
                tongSoDu = rs.getLong(2);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ISNULL(SUM(CASE WHEN LoaiGD IN " + TIEN_VAO + " THEN SoTien ELSE -SoTien END), 0) "
                  + "FROM GiaoDich");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                tongSoCai = rs.getLong(1);
            }
            String sqlLech = "SELECT t.SoTaiKhoan, t.SoDu, ISNULL(g.SoDuSau, 0) AS SoDuSoCai FROM TaiKhoan t "
                + "OUTER APPLY (SELECT TOP 1 SoDuSau FROM GiaoDich WHERE MaTK = t.MaTK ORDER BY MaGD DESC) g "
                + "WHERE t.SoDu <> ISNULL(g.SoDuSau, 0) ORDER BY t.MaTK";
            try (PreparedStatement ps = conn.prepareStatement(sqlLech);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lech.add(new ChenhLech(rs.getString(1), rs.getLong(2), rs.getLong(3)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new DoiSoat(soTK, tongSoDu, tongSoCai, lech);
    }
}
