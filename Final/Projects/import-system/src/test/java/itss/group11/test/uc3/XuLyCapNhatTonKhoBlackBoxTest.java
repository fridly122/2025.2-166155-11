package inventory.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import inventory.model.KetQuaCapNhatTonKho;
import inventory.service.XuLyCapNhatTonKho;

public class XuLyCapNhatTonKhoBlackBoxTest {
    private XuLyCapNhatTonKho xuLy;

    @Before
    public void setUp() {
        xuLy = new XuLyCapNhatTonKho();
    }

    @Test
    public void BB01_capNhatHopLeBangShip() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                20,
                "Ship",
                "user01"
        );

        assertTrue(result.isThanhCong());
        assertEquals("Cập nhật tồn kho thành công", result.getThongBao());
        assertEquals(120, xuLy.laySoLuongTon("HH001", "SITE01"));
    }

    @Test
    public void BB02_capNhatHopLeBangAir() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH002",
                "SITE01",
                10,
                "Air",
                "user01"
        );

        assertTrue(result.isThanhCong());
        assertEquals("Cập nhật tồn kho thành công", result.getThongBao());
        assertEquals(60, xuLy.laySoLuongTon("HH002", "SITE01"));
    }

    @Test
    public void BB03_maHangHoaRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "",
                "SITE01",
                20,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Mã hàng hóa không được để trống", result.getThongBao());
    }

    @Test
    public void BB04_maSiteRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "",
                20,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Mã site không được để trống", result.getThongBao());
    }

    @Test
    public void BB05_soLuongBangKhong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                0,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Số lượng cập nhật phải lớn hơn 0", result.getThongBao());
    }

    @Test
    public void BB06_soLuongAm() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                -5,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Số lượng cập nhật phải lớn hơn 0", result.getThongBao());
    }

    @Test
    public void BB07_phuongThucVanChuyenRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                20,
                "",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Phương thức vận chuyển không được để trống", result.getThongBao());
    }

    @Test
    public void BB08_phuongThucVanChuyenSai() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                20,
                "Truck",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Phương thức vận chuyển không hợp lệ", result.getThongBao());
    }

    @Test
    public void BB09_hangHoaKhongTonTai() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH999",
                "SITE01",
                20,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Không tìm thấy hàng hóa", result.getThongBao());
    }

    @Test
    public void BB10_siteKhongTonTai() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE99",
                20,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Không tìm thấy import site", result.getThongBao());
    }

    @Test
    public void BB11_nguoiCapNhatRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                20,
                "Ship",
                ""
        );

        assertFalse(result.isThanhCong());
        assertEquals("Người cập nhật không được để trống", result.getThongBao());
    }
}