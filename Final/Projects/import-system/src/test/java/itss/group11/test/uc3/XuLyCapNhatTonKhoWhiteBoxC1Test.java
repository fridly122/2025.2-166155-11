package inventory.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import inventory.model.KetQuaCapNhatTonKho;
import inventory.service.XuLyCapNhatTonKho;

public class XuLyCapNhatTonKhoWhiteBoxC1Test {
    private XuLyCapNhatTonKho xuLy;

    @Before
    public void setUp() {
        xuLy = new XuLyCapNhatTonKho();
    }

    @Test
    public void WB01_nhanhTrueKhiMaHangHoaNull() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                null,
                "SITE01",
                10,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB02_nhanhTrueKhiMaHangHoaRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "",
                "SITE01",
                10,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB03_nhanhTrueKhiMaSiteNull() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                null,
                10,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB04_nhanhTrueKhiMaSiteRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "",
                10,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB05_nhanhTrueKhiSoLuongKhongHopLe() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                0,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB06_nhanhTrueKhiDeliveryMeansNull() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                10,
                null,
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB07_nhanhTrueKhiDeliveryMeansRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                10,
                "",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB08_nhanhTrueKhiDeliveryMeansKhongHopLe() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                10,
                "Truck",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB09_nhanhTrueKhiNguoiCapNhatNull() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                10,
                "Ship",
                null
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB10_nhanhTrueKhiNguoiCapNhatRong() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                10,
                "Ship",
                ""
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB11_nhanhTrueKhiHangHoaKhongTonTai() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH999",
                "SITE01",
                10,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB12_nhanhTrueKhiSiteKhongTonTai() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE99",
                10,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB13_nhanhTrueKhiTonKhoSiteKhongTonTai() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH002",
                "SITE02",
                10,
                "Ship",
                "user01"
        );

        assertFalse(result.isThanhCong());
    }

    @Test
    public void WB14_nhanhFalseTatCaDieuKienHopLe() {
        KetQuaCapNhatTonKho result = xuLy.capNhatTonKho(
                "HH001",
                "SITE01",
                10,
                "Ship",
                "user01"
        );

        assertTrue(result.isThanhCong());
    }
}