/* ==========================================================
   CSDL QLNganHang - phiên bản 3 (đợt 1 + đợt 2: module 1-5, 9, 10)
   1 Xác thực | 2 Khách hàng | 3 Tài khoản | 4 Giao dịch & Sổ cái
   5 Chuyển khoản | 9 Cấu hình | 10 Báo cáo, Nhật ký & Thông báo
   CHÚ Ý: script này XÓA và TẠO LẠI CSDL QLNganHang.
   Chạy toàn bộ trong SQL Server Management Studio (SSMS).
   ========================================================== */
USE master;
GO
IF DB_ID(N'QLNganHang') IS NOT NULL
BEGIN
    ALTER DATABASE QLNganHang SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QLNganHang;
END
GO
CREATE DATABASE QLNganHang;
GO
USE QLNganHang;
GO

/* ---------- MODULE 2: KhachHang (hồ sơ khách hàng + xác minh KYC) ----------
   TrangThaiKYC: CHO_DUYET -> DA_XAC_MINH (hoặc TU_CHOI).
   Chỉ khách hàng DA_XAC_MINH mới được mở tài khoản và cấp đăng nhập.
   Hồ sơ không bị xóa, chỉ chuyển HoatDong = 0.                        */
CREATE TABLE KhachHang (
    MaKH         INT IDENTITY(1,1) PRIMARY KEY,
    HoTen        NVARCHAR(100) NOT NULL,
    NgaySinh     DATE          NOT NULL,
    CCCD         VARCHAR(12)   NOT NULL UNIQUE,
    SoDienThoai  VARCHAR(15)   NULL,
    Email        VARCHAR(100)  NULL,
    DiaChi       NVARCHAR(200) NULL,
    TrangThaiKYC VARCHAR(12)   NOT NULL DEFAULT 'CHO_DUYET',
    GhiChuKYC    NVARCHAR(200) NULL,
    HoatDong     BIT           NOT NULL DEFAULT 1,
    NgayTao      DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_KhachHang_KYC CHECK (TrangThaiKYC IN ('CHO_DUYET', 'DA_XAC_MINH', 'TU_CHOI'))
);
GO

/* ---------- MODULE 1: NguoiDung (đăng nhập + phân quyền) ----------
   Tách riêng khỏi tài khoản ngân hàng: 1 người dùng đăng nhập 1 lần,
   khách hàng có thể có nhiều tài khoản ngân hàng.
   VaiTro: KHACH (gắn với 1 khách hàng), GIAODICHVIEN, ADMIN (nhân viên).
   MatKhau: mã băm SHA-256, KHÔNG lưu mật khẩu gốc.                    */
CREATE TABLE NguoiDung (
    MaND        INT IDENTITY(1,1) PRIMARY KEY,
    TenDangNhap VARCHAR(50)   NOT NULL UNIQUE,
    MatKhau     VARCHAR(100)  NOT NULL,
    HoTen       NVARCHAR(100) NULL,                 -- chỉ nhân viên; khách hàng lấy tên từ KhachHang
    VaiTro      VARCHAR(15)   NOT NULL,
    TrangThai   BIT           NOT NULL DEFAULT 1,   -- 1: hoạt động, 0: khóa
    SoLanSai    INT           NOT NULL DEFAULT 0,
    MaKH        INT           NULL,
    NgayTao     DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_NguoiDung_VaiTro CHECK (VaiTro IN ('KHACH', 'GIAODICHVIEN', 'ADMIN')),
    CONSTRAINT CK_NguoiDung_SoLanSai CHECK (SoLanSai >= 0),
    CONSTRAINT CK_NguoiDung_Loai CHECK (
        (VaiTro = 'KHACH' AND MaKH IS NOT NULL)
        OR (VaiTro <> 'KHACH' AND MaKH IS NULL AND HoTen IS NOT NULL)),
    CONSTRAINT FK_NguoiDung_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
);
GO
-- Mỗi khách hàng chỉ có tối đa 1 tài khoản đăng nhập
CREATE UNIQUE INDEX UX_NguoiDung_MaKH ON NguoiDung(MaKH) WHERE MaKH IS NOT NULL;
GO

/* ---------- MODULE 3: TaiKhoan (tài khoản ngân hàng) ----------
   1 khách hàng có nhiều tài khoản.
   TrangThai: HOAT_DONG | DONG_BANG | DA_DONG (đóng chỉ khi SoDu = 0).
   SoDu chỉ được thay đổi bởi module Giao dịch (sổ cái).               */
CREATE TABLE TaiKhoan (
    MaTK       INT IDENTITY(1,1) PRIMARY KEY,
    SoTaiKhoan VARCHAR(20)   NOT NULL UNIQUE,
    MaKH       INT           NOT NULL,
    TenGoiNho  NVARCHAR(50)  NULL,
    SoDu       DECIMAL(18,0) NOT NULL DEFAULT 0,
    TrangThai  VARCHAR(10)   NOT NULL DEFAULT 'HOAT_DONG',
    NgayMo     DATETIME      NOT NULL DEFAULT GETDATE(),
    NgayDong   DATETIME      NULL,
    CONSTRAINT CK_TaiKhoan_SoDu CHECK (SoDu >= 0),
    CONSTRAINT CK_TaiKhoan_TrangThai CHECK (TrangThai IN ('HOAT_DONG', 'DONG_BANG', 'DA_DONG')),
    CONSTRAINT FK_TaiKhoan_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
);
GO
CREATE INDEX IX_TaiKhoan_MaKH ON TaiKhoan(MaKH);
GO

/* ---------- MODULE 4: GiaoDich (SỔ CÁI) ----------
   Mỗi biến động số dư là 1 dòng, KHÔNG sửa, KHÔNG xóa.
   Muốn sửa sai thì ghi 1 giao dịch đảo (HOAN_NAP / HOAN_RUT, MaGDGoc trỏ về giao dịch gốc).
   Kênh: QUAY (nhân viên), UNG_DUNG (khách hàng), HE_THONG (đảo giao dịch, tự động).
   SoTaiKhoan / SoTKDoiTac: chép số tài khoản vào sổ để tra cứu không cần ghép bảng.
   Chuyển khoản tạo 3 dòng: CHUYEN_DI + PHI (bên gửi) và CHUYEN_DEN (bên nhận);
   MaNhom của PHI và CHUYEN_DEN trỏ về dòng CHUYEN_DI của cùng một lần chuyển. */
CREATE TABLE GiaoDich (
    MaGD        INT IDENTITY(1,1) PRIMARY KEY,
    MaThamChieu AS ('GD' + RIGHT('00000000' + CAST(MaGD AS VARCHAR(8)), 8)) PERSISTED,
    MaTK        INT           NOT NULL,
    SoTaiKhoan  VARCHAR(20)   NOT NULL,
    LoaiGD      VARCHAR(12)   NOT NULL,
    SoTien      DECIMAL(18,0) NOT NULL,
    SoDuSau     DECIMAL(18,0) NOT NULL,
    NgayGD      DATETIME      NOT NULL DEFAULT GETDATE(),
    Kenh        VARCHAR(10)   NOT NULL,
    GhiChu      NVARCHAR(200) NULL,
    SoTKDoiTac  VARCHAR(20)   NULL,
    MaGDGoc     INT           NULL,                  -- giao dịch gốc nếu đây là giao dịch đảo
    MaNhom      INT           NULL,                  -- nhóm các dòng của cùng một lần chuyển khoản
    MaND        INT           NULL,                  -- người thực hiện
    CONSTRAINT CK_GiaoDich_Loai CHECK (LoaiGD IN
        ('NAP', 'RUT', 'CHUYEN_DI', 'CHUYEN_DEN', 'PHI', 'HOAN_NAP', 'HOAN_RUT')),
    CONSTRAINT CK_GiaoDich_Kenh CHECK (Kenh IN ('QUAY', 'UNG_DUNG', 'HE_THONG')),
    CONSTRAINT CK_GiaoDich_SoTien CHECK (SoTien > 0),
    CONSTRAINT FK_GiaoDich_TaiKhoan FOREIGN KEY (MaTK) REFERENCES TaiKhoan(MaTK),
    CONSTRAINT FK_GiaoDich_GiaoDichGoc FOREIGN KEY (MaGDGoc) REFERENCES GiaoDich(MaGD),
    CONSTRAINT FK_GiaoDich_Nhom FOREIGN KEY (MaNhom) REFERENCES GiaoDich(MaGD),
    CONSTRAINT FK_GiaoDich_NguoiDung FOREIGN KEY (MaND) REFERENCES NguoiDung(MaND)
);
GO
CREATE INDEX IX_GiaoDich_MaTK ON GiaoDich(MaTK, MaGD);
-- Mỗi giao dịch chỉ được đảo 1 lần (chặn ở mức CSDL, kể cả khi 2 người đảo cùng lúc)
CREATE UNIQUE INDEX UX_GiaoDich_MaGDGoc ON GiaoDich(MaGDGoc) WHERE MaGDGoc IS NOT NULL;
GO

/* ---------- MODULE 9: CauHinh (tham số, biểu phí, lãi suất) ----------
   Admin chỉnh trên giao diện; các module khác đọc qua CauHinhController.
   Thiếu tham số thì chương trình dùng giá trị mặc định trong code. */
CREATE TABLE ThamSo (
    MaThamSo VARCHAR(40)   PRIMARY KEY,
    GiaTri   BIGINT        NOT NULL,
    DonVi    NVARCHAR(20)  NULL,
    MoTa     NVARCHAR(200) NULL
);
GO
CREATE TABLE BieuPhi (
    MaPhi     INT IDENTITY(1,1) PRIMARY KEY,
    LoaiPhi   VARCHAR(20)   NOT NULL,                -- hiện có: CHUYEN_KHOAN
    TuSoTien  DECIMAL(18,0) NOT NULL,                -- bậc phí: từ số tiền (bao gồm)
    DenSoTien DECIMAL(18,0) NULL,                    -- đến số tiền (bao gồm); NULL = không giới hạn
    Phi       DECIMAL(18,0) NOT NULL,
    CONSTRAINT CK_BieuPhi_Phi CHECK (Phi >= 0)
);
GO
CREATE TABLE LaiSuat (                               -- dùng cho module Tiết kiệm / Khoản vay (đợt 3-4)
    MaLS       INT IDENTITY(1,1) PRIMARY KEY,
    LoaiLai    VARCHAR(10)  NOT NULL,                -- TIET_KIEM | VAY
    KyHanThang INT          NOT NULL,                -- 0 = không kỳ hạn
    LaiSuatNam DECIMAL(5,2) NOT NULL,                -- %/năm
    CONSTRAINT CK_LaiSuat_Loai CHECK (LoaiLai IN ('TIET_KIEM', 'VAY')),
    CONSTRAINT CK_LaiSuat_Muc  CHECK (LaiSuatNam >= 0 AND LaiSuatNam <= 30),
    CONSTRAINT UQ_LaiSuat UNIQUE (LoaiLai, KyHanThang)
);
GO

/* ---------- MODULE 5: Chuyển khoản (danh bạ + lệnh định kỳ) ---------- */
CREATE TABLE NguoiThuHuong (
    MaNTH        INT IDENTITY(1,1) PRIMARY KEY,
    MaKH         INT           NOT NULL,
    SoTaiKhoan   VARCHAR(20)   NOT NULL,
    TenGoiNho    NVARCHAR(50)  NOT NULL,
    TenNguoiNhan NVARCHAR(100) NOT NULL,             -- tên chủ tài khoản tại thời điểm lưu
    NgayTao      DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_NguoiThuHuong_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT UQ_NguoiThuHuong UNIQUE (MaKH, SoTaiKhoan)
);
GO
CREATE TABLE LenhDinhKy (
    MaLenh         INT IDENTITY(1,1) PRIMARY KEY,
    MaKH           INT           NOT NULL,
    MaTKNguon      INT           NOT NULL,
    SoTKNguon      VARCHAR(20)   NOT NULL,
    SoTKNhan       VARCHAR(20)   NOT NULL,
    TenNguoiNhan   NVARCHAR(100) NOT NULL,
    SoTien         DECIMAL(18,0) NOT NULL,
    NoiDung        NVARCHAR(100) NULL,
    TanSuat        VARCHAR(10)   NOT NULL,           -- HANG_NGAY | HANG_TUAN | HANG_THANG
    NgayBatDau     DATE          NOT NULL,
    NgayKetThuc    DATE          NULL,
    NgayChayKeTiep DATE          NOT NULL,
    TrangThai      VARCHAR(10)   NOT NULL DEFAULT 'HOAT_DONG',
    SoLanChay      INT           NOT NULL DEFAULT 0,
    KetQuaLanCuoi  NVARCHAR(200) NULL,
    NgayTao        DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_LenhDinhKy_SoTien CHECK (SoTien > 0),
    CONSTRAINT CK_LenhDinhKy_TanSuat CHECK (TanSuat IN ('HANG_NGAY', 'HANG_TUAN', 'HANG_THANG')),
    CONSTRAINT CK_LenhDinhKy_TrangThai CHECK (TrangThai IN ('HOAT_DONG', 'TAM_DUNG', 'HET_HAN', 'DA_HUY')),
    CONSTRAINT FK_LenhDinhKy_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_LenhDinhKy_TaiKhoan FOREIGN KEY (MaTKNguon) REFERENCES TaiKhoan(MaTK)
);
GO
CREATE INDEX IX_LenhDinhKy_DenHan ON LenhDinhKy(TrangThai, NgayChayKeTiep);
GO

/* ---------- MODULE 10: NhatKy (kiểm toán) và ThongBao ---------- */
CREATE TABLE NhatKy (
    MaNK        INT IDENTITY(1,1) PRIMARY KEY,
    ThoiGian    DATETIME      NOT NULL DEFAULT GETDATE(),
    MaND        INT           NULL,
    TenDangNhap VARCHAR(50)   NULL,                  -- chép lại để còn tra cứu kể cả khi đăng nhập thất bại
    HanhDong    VARCHAR(40)   NOT NULL,
    DoiTuong    NVARCHAR(100) NULL,
    ChiTiet     NVARCHAR(400) NULL,
    CONSTRAINT FK_NhatKy_NguoiDung FOREIGN KEY (MaND) REFERENCES NguoiDung(MaND)
);
GO
CREATE INDEX IX_NhatKy_ThoiGian ON NhatKy(ThoiGian DESC);
GO
CREATE TABLE ThongBao (
    MaTB     INT IDENTITY(1,1) PRIMARY KEY,
    MaKH     INT           NOT NULL,
    TieuDe   NVARCHAR(100) NOT NULL,
    NoiDung  NVARCHAR(400) NOT NULL,
    ThoiGian DATETIME      NOT NULL DEFAULT GETDATE(),
    DaDoc    BIT           NOT NULL DEFAULT 0,
    CONSTRAINT FK_ThongBao_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)
);
GO
CREATE INDEX IX_ThongBao_MaKH ON ThongBao(MaKH, MaTB DESC);
GO

/* ---------- Dữ liệu mẫu ----------
   Tên đăng nhập / mật khẩu (trong CSDL là mã băm):
     admin / admin123   Quản trị viên
     gdv1  / gdv123     Giao dịch viên
     an    / 123456     Khách hàng (2 tài khoản)
     binh  / 123456     Khách hàng
     cuong / 123456     Khách hàng
   Khách hàng "Dũng" đang CHO_DUYET KYC, chưa có đăng nhập để thử luồng duyệt.  */
INSERT INTO KhachHang(HoTen, NgaySinh, CCCD, SoDienThoai, Email, DiaChi, TrangThaiKYC) VALUES
(N'Nguyễn Văn An', '1990-05-12', '001200000001', '0901000001', 'an@example.com',    N'Hà Nội',    'DA_XAC_MINH'),
(N'Trần Thị Bình', '1995-08-20', '001200000002', '0901000002', 'binh@example.com',  N'Hà Nội',    'DA_XAC_MINH'),
(N'Lê Văn Cường',  '1988-03-03', '001200000003', '0901000003', 'cuong@example.com', N'Hải Phòng', 'DA_XAC_MINH'),
(N'Phạm Văn Dũng', '2000-11-30', '001200000004', '0901000004', 'dung@example.com',  N'Đà Nẵng',   'CHO_DUYET');

INSERT INTO NguoiDung(TenDangNhap, MatKhau, HoTen, VaiTro, MaKH) VALUES
('admin', '3cd4fd87ccd0ff1728e507eb80b8c1fd8fa3aea7e8cce6fd5ed3c13c21627b8c', N'Quản trị viên',      'ADMIN',        NULL),
('gdv1',  '4e15e0caab57d6c16c47ef395e91d67bc5359daffb1a70e345ad322c82f51c23', N'Phạm Thị Giao Dịch', 'GIAODICHVIEN', NULL),
('an',    '41ec295cca95d49496b1087d006ef5e1a2ec60a14f2c4df49c6865ff11c4f3e7', NULL, 'KHACH', 1),
('binh',  '04fda08ac399d08db7d368644f803afa8060fe08b9ab13a0adef96ad2f38da05', NULL, 'KHACH', 2),
('cuong', 'c5c9fcf3e6ed690e6a43964eb1214e54c1ae09a498167b62ec09c0654cb0b857', NULL, 'KHACH', 3);

INSERT INTO TaiKhoan(SoTaiKhoan, MaKH, TenGoiNho, SoDu) VALUES
('1000000001', 1, N'Tài khoản chính',    5000000),
('1000000002', 2, N'Tài khoản chính',    2000000),
('1000000003', 3, N'Tài khoản chính',   10000000),
('1000000004', 1, N'Quỹ mua xe',         1000000);

-- Sổ cái: mỗi tài khoản có 1 giao dịch nạp tiền mở tài khoản bằng đúng số dư hiện tại
INSERT INTO GiaoDich(MaTK, SoTaiKhoan, LoaiGD, SoTien, SoDuSau, NgayGD, Kenh, GhiChu, MaND)
SELECT MaTK, SoTaiKhoan, 'NAP', SoDu, SoDu, DATEADD(DAY, -5, GETDATE()), 'QUAY',
       N'Nạp tiền mở tài khoản', (SELECT MaND FROM NguoiDung WHERE TenDangNhap = 'gdv1')
FROM TaiKhoan WHERE SoDu > 0;
GO

-- Tham số hệ thống (admin chỉnh trong module Cấu hình)
INSERT INTO ThamSo(MaThamSo, GiaTri, DonVi, MoTa) VALUES
('SO_LAN_SAI_TOI_DA',          5,          N'lần',    N'Số lần nhập sai mật khẩu liên tiếp thì khóa tài khoản đăng nhập'),
('TUOI_TOI_THIEU',             18,         N'tuổi',   N'Tuổi tối thiểu để mở hồ sơ khách hàng'),
('SO_TAI_KHOAN_TOI_DA',        5,          N'tài khoản', N'Số tài khoản (chưa đóng) tối đa của mỗi khách hàng'),
('DO_DAI_MAT_KHAU_TOI_THIEU',  6,          N'ký tự',  N'Độ dài mật khẩu tối thiểu'),
('THOI_GIAN_HET_PHIEN_PHUT',   10,         N'phút',   N'Tự đăng xuất khi không thao tác (0 = không tự đăng xuất; áp dụng từ lần đăng nhập sau)'),
('HAN_MUC_NAP_RUT_QUAY',       500000000,  N'VND',    N'Nạp / rút tối đa mỗi lần tại quầy'),
('HAN_MUC_NAP_RUT_UNG_DUNG',   50000000,   N'VND',    N'Nạp / rút tối đa mỗi lần trên ứng dụng'),
('CHUYEN_TOI_THIEU',           10000,      N'VND',    N'Số tiền chuyển khoản tối thiểu mỗi lần'),
('HAN_MUC_CHUYEN_MOI_LAN',     200000000,  N'VND',    N'Chuyển khoản tối đa mỗi lần'),
('HAN_MUC_CHUYEN_MOI_NGAY',    500000000,  N'VND',    N'Tổng chuyển khoản tối đa trong một ngày của mỗi tài khoản'),
('NGUONG_OTP',                 10000000,   N'VND',    N'Chuyển khoản từ số tiền này trở lên phải xác nhận bằng mã OTP');

-- Biểu phí chuyển khoản theo bậc số tiền
INSERT INTO BieuPhi(LoaiPhi, TuSoTien, DenSoTien, Phi) VALUES
('CHUYEN_KHOAN',         0,   2000000,     0),
('CHUYEN_KHOAN',   2000001, 100000000,  3300),
('CHUYEN_KHOAN', 100000001,      NULL, 11000);

-- Lãi suất (dùng ở đợt 3-4 cho Tiết kiệm và Khoản vay)
INSERT INTO LaiSuat(LoaiLai, KyHanThang, LaiSuatNam) VALUES
('TIET_KIEM', 0, 0.10), ('TIET_KIEM', 1, 3.00), ('TIET_KIEM', 3, 3.50),
('TIET_KIEM', 6, 5.00), ('TIET_KIEM', 12, 5.80),
('VAY', 12, 10.50), ('VAY', 24, 11.00), ('VAY', 36, 11.50);

-- Danh bạ mẫu: An lưu Bình làm người thụ hưởng
INSERT INTO NguoiThuHuong(MaKH, SoTaiKhoan, TenGoiNho, TenNguoiNhan) VALUES
(1, '1000000002', N'Bình (bạn)', N'Trần Thị Bình');
GO
