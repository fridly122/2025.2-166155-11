# Bài tập 7 - Kiểm thử UC005: Xử lý lập kế hoạch đặt hàng

## 1. Module được chọn

- Use case phụ trách: UC005 - Xử lý lập kế hoạch đặt hàng.
- Lớp kiểm thử: `itss.group11.services.allocation.AllocationService`.
- Các phương thức trọng tâm:
  - `previewAllocationPlan(String requestCode)`
  - `processAllocationPlan(String requestCode)`

Lý do chọn module: `AllocationService` là lớp xử lý nghiệp vụ chính của UC005. Lớp này nhận yêu cầu nhập hàng ở trạng thái `PENDING`, kiểm tra tồn kho tại các site, lập kế hoạch phân bổ, tạo `PurchaseOrder`, tạo `PurchaseOrderLine`, chọn phương tiện vận chuyển và cập nhật trạng thái yêu cầu sang `ORDERED`.

Các class kiểm thử tự động:

- `itss.group11.allocation.AllocationServiceBlackBoxTest`
- `itss.group11.allocation.AllocationServiceWhiteBoxC1Test`
- `itss.group11.allocation.AllocationUseCaseScenarioTest`

## 2. Kiểm thử hộp đen

### 2.1 Kỹ thuật áp dụng

Áp dụng phân hoạch tương đương và giá trị biên theo dữ liệu đầu vào/đầu ra quan sát được của `processAllocationPlan`.

Các phân hoạch chính:

| Nhóm | Điều kiện đầu vào | Kỳ vọng |
| --- | --- | --- |
| P1 | Mã yêu cầu tồn tại, trạng thái `PENDING`, có item, đủ tồn kho | Tạo PO thành công, request chuyển `ORDERED` |
| P2 | Mã yêu cầu không tồn tại | Báo lỗi không tìm thấy yêu cầu |
| P3 | Yêu cầu tồn tại nhưng không ở trạng thái `PENDING` | Báo lỗi trạng thái không hợp lệ |
| P4 | Yêu cầu `PENDING` nhưng không có mặt hàng | Báo lỗi thiếu mặt hàng |
| P5 | Yêu cầu hợp lệ nhưng tồn kho không đủ | Báo lỗi `INSUFFICIENT_INVENTORY`, không tạo PO |

### 2.2 Test case hộp đen

| ID | Test case | Dữ liệu | Kết quả mong đợi | Test tự động |
| --- | --- | --- | --- | --- |
| BB01 | Xử lý yêu cầu hợp lệ | `REQ001`, item `MH001`, SL 10, site có 10 hàng | Trả về success, tạo 1 PO, line SL 10, request `ORDERED` | `processAllocationPlan_validPendingRequestWithEnoughInventory_createsPurchaseOrderAndMarksRequestOrdered` |
| BB02 | Mã yêu cầu không tồn tại | `REQ404` | Ném lỗi "Không tìm thấy yêu cầu nhập hàng" | `processAllocationPlan_unknownRequestCode_throwsNotFoundError` |
| BB03 | Request không còn `PENDING` | `REQ002` trạng thái `ORDERED` | Ném lỗi "không ở trạng thái PENDING" | `processAllocationPlan_nonPendingRequest_throwsInvalidStatusError` |
| BB04 | Request không có item | `REQ003`, danh sách item rỗng | Ném lỗi "chưa có mặt hàng" | `processAllocationPlan_pendingRequestWithoutItems_throwsNoItemError` |
| BB05 | Không đủ tồn kho | `REQ004`, cần 20 nhưng service báo không đủ | Ném lỗi `INSUFFICIENT_INVENTORY`, không lưu PO | `processAllocationPlan_pendingRequestWithInsufficientInventory_throwsInsufficientInventory` |

## 3. Kiểm thử hộp trắng C1

### 3.1 Kỹ thuật áp dụng

Áp dụng kiểm thử hộp trắng theo độ đo C1. Trong bài này, C1 được hiểu là bao phủ nhánh/quyết định. Các test case được thiết kế để mỗi quyết định quan trọng trong `AllocationService` có ít nhất một lần nhận giá trị đúng/sai.

Các quyết định cần bao phủ:

| Mã quyết định | Vị trí logic | Nhánh cần phủ |
| --- | --- | --- |
| D1 | `findPendingRequest`: request có tồn tại không | Có / Không |
| D2 | `findPendingRequest`: status có phải `PENDING` không | Đúng / Sai |
| D3 | `findPendingRequest`: danh sách item có rỗng không | Rỗng / Không rỗng |
| D4 | `previewAllocationPlan`: `remainingQty <= 0` trong vòng lặp site | Đúng / Sai |
| D5 | `previewAllocationPlan`: sau khi duyệt site còn thiếu hàng không | Đủ / Thiếu |
| D6 | `processAllocationPlan`: tổng tồn kho có đủ không | Đủ / Không đủ |
| D7 | `executeOptimalAllocationAndCreatePO`: site đã có PO trong map chưa | Tạo mới / Dùng lại |
| D8 | `suggestDeliveryMeans`: site có `daysByShip` không | Có -> `SHIP` / Không |
| D9 | `suggestDeliveryMeans`: nếu không có ship, có `daysByAir` không | Có -> `AIR` / Không -> lỗi |

### 3.2 Test case hộp trắng C1

| ID | Mục tiêu bao phủ | Dữ liệu | Kết quả mong đợi | Test tự động |
| --- | --- | --- | --- | --- |
| WB01 | D4, D5, D8, D9 nhánh `AIR` | Request cần 5; site1 có 3 và ship; site2 có 2 chỉ air; site3 dư hàng | Preview có 2 dòng, đủ hàng, dòng 1 `SHIP`, dòng 2 `AIR` | `previewAllocationPlan_multipleStocksCoversLoopBreakEnoughInventoryShipAndAirBranches` |
| WB02 | D5 nhánh thiếu hàng | Request cần 10; chỉ có 4 | Preview báo không đủ tồn kho | `previewAllocationPlan_remainingQuantityAfterAllStocksCoversInsufficientInventoryBranch` |
| WB03 | D9 nhánh lỗi không có dữ liệu vận chuyển | Site có hàng nhưng không có `daysByShip` và `daysByAir` | Ném lỗi thiếu dữ liệu vận chuyển | `previewAllocationPlan_siteWithoutTransportDaysCoversTransportErrorBranch` |
| WB04 | D7 nhánh tạo mới và dùng lại PO theo site | Request có 2 item đều lấy từ cùng site | Chỉ tạo 1 PO, PO có 2 line | `processAllocationPlan_twoItemsAllocatedToSameSiteCoversPurchaseOrderGroupingBranch` |

## 4. Kiểm thử use case UC005

### 4.1 Scenarios

| Scenario | Mô tả | Kết quả mong đợi |
| --- | --- | --- |
| S1 | Luồng chính: nhân viên chọn yêu cầu `PENDING`, xem trước kế hoạch, xử lý lập kế hoạch | Hệ thống tạo PO, tạo PO line, cập nhật request sang `ORDERED` |
| S2 | Luồng thay thế: tồn kho các site không đủ | Hệ thống không tạo PO, request vẫn `PENDING`, hiển thị lỗi |
| S3 | Luồng thay thế: yêu cầu đã xử lý trước đó | Hệ thống từ chối lập kế hoạch lại |
| S4 | Luồng phân bổ nhiều site: một item cần lấy từ nhiều site | Hệ thống tạo số PO tương ứng với số site được chọn |

### 4.2 Test case use case

| ID | Scenario | Dữ liệu | Kết quả mong đợi | Test tự động |
| --- | --- | --- | --- | --- |
| UC005-01 | S1 | `REQ201`, item `MH001`, SL 8, site đủ hàng | Preview đủ hàng, process success, request `ORDERED` | `uc005_mainScenario_previewAndProcessSuccessfulAllocation` |
| UC005-02 | S2 | `REQ202`, item `MH001`, SL 50, tồn kho không đủ | Ném `INSUFFICIENT_INVENTORY`, không tạo PO | `uc005_alternativeScenario_insufficientInventoryStopsBeforeCreatingPurchaseOrder` |
| UC005-03 | S3 | `REQ203` trạng thái `ORDERED` | Ném lỗi trạng thái không hợp lệ, không tạo PO | `uc005_alternativeScenario_requestAlreadyProcessedCannotBeAllocatedAgain` |
| UC005-04 | S4 | `REQ204`, cần 12; site1 có 7, site2 có 5 | Tạo 2 PO, số lượng line lần lượt 7 và 5 | `uc005_alternativeScenario_oneRequestIsSplitIntoMinimumPurchaseOrdersBySite` |

## 5. Cách chạy kiểm thử

Chạy toàn bộ test allocation:

```bash
mvn -q "-Dtest=Allocation*Test" test
```

Chạy từng nhóm test:

```bash
mvn -q "-Dtest=AllocationServiceBlackBoxTest" test
mvn -q "-Dtest=AllocationServiceWhiteBoxC1Test" test
mvn -q "-Dtest=AllocationUseCaseScenarioTest" test
```

Các test này là unit test, dùng Mockito để giả lập repository/service phụ thuộc nên không cần kết nối Supabase.
