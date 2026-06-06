package inventory.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import inventory.model.KetQuaCapNhatTonKho;
import inventory.service.XuLyCapNhatTonKho;

public class UseCaseCapNhatTonKhoTest {
    private XuLyCapNhatTonKho xuLy;

    @Before
    public void setUp() {
        xuLy = new XuLyCapNhatTonKho();
    }

    @Test
    public void UC01_capNhatTonKhoThanhCong() {
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
        assertEquals(1, xuLy.demSoLichSuCapNhat());
    }

    @Test
    public void UC02_hangHoaKhongTonTai() {
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
    public void UC03_siteKhongTonTai() {
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
    public void UC04_soLuongCapNhatKhongHopLe() {
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
    public void UC05_thongTinVanChuyenKhongHopLe() {
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
    public void UC06_thongTinTonKhoTaiSiteKhongTonTai() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH002",
                "SITE02",
                20,
                "Air",
                "user01"
        );

        assertFalse(result.isThanhCong());
        assertEquals("Không tìm thấy thông tin tồn kho tại site", result.getThongBao());
    }
}