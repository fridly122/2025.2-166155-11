I. CHÚ THÍCH

1. Lập kế hoạch đặt hàng (Hiếu) - allocation
2. Tạo yêu cầu nhập hàng (Nguyên) - requestManage
3. Tìm kiếm Site và phân loại mặt hàng (Hòa) - siteSync
4. Cập nhật thông tin tồn kho Site (Tài) - warehouse
5. Quản lý thông tin vận chuyển Site (Nhật) - transport
6. Đối soát và xác nhận nhập kho (Cao Anh) - orderExecution

II. LUỒNG HOẠT ĐỘNG GIAO DIỆN TỔNG THỂ (END-TO-END SCREEN FLOW)
Giao diện hệ thống sẽ được chia theo tuyến tính thời gian tương ứng với 4 bước lớn sau:

Bước 1: Bộ phận Bán hàng - Màn hình 1.1: Danh sách Yêu cầu nhập hàng. Hiển thị tất cả yêu cầu bán hàng. Có nút "Tạo yêu cầu". - Màn hình 1.2: Form Tạo yêu cầu nhập hàng. Nhập mã hàng, số lượng, ngày nhận mong muốn (desiredDeliveryDate). Bấm gửi -> Yêu cầu lưu vào DB với trạng thái PENDING.

Bước 2: Tìm kiếm Site & Phân loại mặt hàng - Màn hình 2.1: Danh sách Yêu cầu chờ phân loại (Dành cho BP Đặt hàng quốc tế). - Màn hình 2.2: Chi tiết phân loại & Hỏi tồn kho. Khi chọn một yêu cầu PENDING, nhân viên bấm nút "Bắt đầu phân loại" -> Giao diện hiển thị danh sách các mặt hàng đã được gom nhóm theo các Site nước ngoài có kinh doanh mặt hàng đó. Nhân viên bấm "Gửi hỏi tồn kho" -> Hệ thống cập nhật số lượng tồn kho thật của các Site vào bảng site_inventory.

Bước 3: Xử lý lập kế hoạch đặt hàng - Màn hình 3.1: Danh sách Yêu cầu chờ lập kế hoạch. Hiển thị các yêu cầu đã có dữ liệu tồn kho từ nước ngoài gửi về. - Màn hình 3.2: Chi tiết phân bổ & Sinh đơn PO. + Hiển thị thông tin yêu cầu gốc và danh sách mặt hàng cần mua. + Có nút "Xem trước kế hoạch" (Preview): Khi bấm, Frontend gọi API GET /preview/{requestCode} để hiển thị một bảng mô phỏng (dự kiến mặt hàng này sẽ lấy từ Kho Tokyo bao nhiêu cái, Kho Osaka bao nhiêu cái).Có nút "Xác nhận đặt hàng" (Process): Khi bấm, Frontend gọi API POST /process/{requestCode}, hệ thống chính thức tách đơn, sinh ra các mã đơn đặt hàng PurchaseOrder (ví dụ: DH-A1B2C3), và chuyển trạng thái yêu cầu thành ORDERED.

Bước 4: Đối soát và Xác nhận nhập kho - Màn hình 4.1: Danh sách Đơn đặt hàng (PO) đang vận chuyển (IN_TRANSIT). - Màn hình 4.2: Chi tiết đối soát. Khi hàng về đến kho nội địa, nhân viên kho chọn mã PO, nhập số lượng thực nhận (received_qty). Hệ thống tự động hiển thị số chênh lệch. Nếu có sai lệch -> Hiện Pop-up lập biên bản. Nếu khớp -> Bấm "Xác nhận nhập kho" để hoàn tất vòng đời.
