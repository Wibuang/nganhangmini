package module.baocao.dao;

import core.database.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import module.baocao.model.NhatKy;

/** SQL của bảng NhatKy. Chỉ thêm và đọc, không bao giờ sửa hay xóa. */
public class NhatKyDAO {

    public boolean them(Integer maND, String tenDangNhap, String hanhDong, String doiTuong, String chiTiet) {
        String sql = "INSERT INTO NhatKy(MaND, TenDangNhap, HanhDong, DoiTuong, ChiTiet) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (maND == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, maND);
            }
            ps.setString(2, tenDangNhap);
            ps.setString(3, hanhDong);
            ps.setString(4, doiTuong);
            ps.setString(5, chiTiet);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** tuKhoa khớp tên đăng nhập, hành động, đối tượng hoặc chi tiết; mọi điều kiện để null là không lọc. */
    public ArrayList<NhatKy> tim(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, int toiDa) {
        StringBuilder sql = new StringBuilder("SELECT TOP (" + toiDa
                + ") MaNK, ThoiGian, TenDangNhap, HanhDong, DoiTuong, ChiTiet FROM NhatKy WHERE 1 = 1 ");
        ArrayList<Object> thamSo = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (TenDangNhap LIKE ? OR HanhDong LIKE ? OR DoiTuong LIKE ? OR ChiTiet LIKE ?) ");
            String mau = "%" + tuKhoa.trim() + "%";
            for (int i = 0; i < 4; i++) {
                thamSo.add(mau);
            }
        }
        if (tuNgay != null) {
            sql.append("AND ThoiGian >= ? ");
            thamSo.add(Timestamp.valueOf(tuNgay.atStartOfDay()));
        }
        if (denNgay != null) {
            sql.append("AND ThoiGian < ? ");
            thamSo.add(Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay()));
        }
        sql.append("ORDER BY MaNK DESC");

        ArrayList<NhatKy> ds = new ArrayList<>();
        try (Connection conn = MyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new NhatKy(rs.getInt("MaNK"), rs.getTimestamp("ThoiGian").toLocalDateTime(),
                            rs.getString("TenDangNhap"), rs.getString("HanhDong"),
                            rs.getString("DoiTuong"), rs.getString("ChiTiet")));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ds;
    }
}
