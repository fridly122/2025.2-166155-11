# Bài tập 7 - Kiểm thử UC6: Đối soát và xác nhận nhập kho

## 1. Module được chọn

- Use case phụ trách: UC6 - Đối soát và xác nhận nhập kho.
- Lớp kiểm thử chính: `itss.group11.subsystem.uc6.PurchaseOrderService`.
- Phương thức trọng tâm: `reconcile(String orderId, ReconciliationSubmitDTO dto)`.
- Các phương thức/phần hỗ trợ được kiểm thử kèm theo:
  - `getInTransitOrders()`
  - `getReconciliationDetail(String orderId)`
  - `ReconciliationValidator`
  - `WarehouseInventoryService`
  - `DiscrepancyReportService`

Lý do chọn module: `PurchaseOrderService.reconcile` là nghiệp vụ chính của UC6. Phương thức này kiểm tra đơn hàng đang vận chuyển, nhận dữ liệu số lượng thực nhận, ghi nhận sai lệch nếu có, cập nhật tồn kho nội bộ và chuyển trạng thái đơn hàng sang `RECEIVED`.

Các class kiểm thử tự động:

- `itss.group11.test.uc6.PurchaseOrderServiceBlackBoxTest`
- `itss.group11.test.uc6.PurchaseOrderServiceWhiteBoxC1Test`
- `itss.group11.test.uc6.PurchaseOrderUseCaseScenarioTest`
- `itss.group11.test.uc6.Uc6TestData`

## 2. Kiểm thử hộp đen

### 2.1 Kỹ thuật áp dụng

Áp dụng phân hoạch tương đương và giá trị biên theo dữ liệu đầu vào/đầu ra quan sát được của UC6.

| Nhóm | Điều kiện đầu vào | Kết quả mong đợi |
| --- | --- | --- |
| P1 | Mã đơn tồn tại, trạng thái `IN_TRANSIT`, có dòng hàng | Trả về chi tiết đối soát, có giá tiền, số lượng đặt, số lượng nhận mặc định |
| P2 | Đơn hợp lệ, số lượng nhận bằng số lượng đặt | Cập nhật tồn kho, chuyển đơn sang `RECEIVED`, không lập biên bản sai lệch |
| P3 | Đơn hợp lệ, số lượng nhận nhỏ hơn số lượng đặt | Cập nhật tồn kho theo số lượng nhận, lập biên bản sai lệch |
| P4 | Mã đơn không tồn tại | Báo lỗi không tìm thấy đơn, không cập nhật kho |
| P5 | Đơn tồn tại nhưng không ở trạng thái `IN_TRANSIT` | Từ chối xác nhận nhập kho |
| P6 | Số lượng thực nhận âm | Từ chối dữ liệu trước khi truy vấn đơn hàng |

### 2.2 Test case hộp đen

| ID | Test case | Dữ liệu | Kết quả mong đợi | Test tự động |
| --- | --- | --- | --- | --- |
| BB01 | Lấy chi tiết đối soát của đơn hợp lệ | `PO-UC6-001`, 1 dòng hàng giá `125000.00`, `receivedQty = null` | Hiển thị dòng hàng, giá tiền, `receivedQty` mặc định bằng `orderedQty`, lệch bằng 0 | `getReconciliationDetail_validInTransitOrder_returnsOrderLinePriceAndDefaultReceivedQty` |
| BB02 | Xác nhận nhập kho không sai lệch | Đặt 10, nhận 10 | Đơn chuyển `RECEIVED`, tồn kho tăng 10, không lưu biên bản | `reconcile_validMatchingQuantity_marksReceivedAndDoesNotCreateDiscrepancyReport` |
| BB03 | Xác nhận nhập kho có sai lệch | Đặt 10, nhận 7 | Tồn kho tăng 7, tạo biên bản lệch 3 | `reconcile_validShortReceivedQuantity_createsDiscrepancyReportAndUpdatesExistingInventory` |
| BB04 | Mã đơn không tồn tại | Repository trả `Optional.empty()` | Ném lỗi không tìm thấy đơn, không cập nhật kho/biên bản | `reconcile_unknownOrderId_reportsNotFoundAndDoesNotUpdateInventory` |
| BB05 | Đơn không ở trạng thái đang vận chuyển | Status `CREATED` | Ném lỗi chỉ cho phép `IN_TRANSIT`, không load dòng hàng | `reconcile_orderNotInTransit_rejectsConfirmationBeforeLoadingLines` |
| BB06 | Số lượng nhận âm | `receivedQty = -1` | Ném lỗi dữ liệu không hợp lệ trước khi query đơn | `reconcile_negativeReceivedQuantity_rejectsInputBeforeQueryingOrder` |

## 3. Kiểm thử hộp trắng C1

### 3.1 Kỹ thuật áp dụng

Áp dụng kiểm thử hộp trắng theo độ đo C1. Với module này, C1 được hiểu là bao phủ nhánh/quyết định quan trọng trong luồng xử lý.

| Mã quyết định | Vị trí logic | Nhánh cần phủ |
| --- | --- | --- |
| D1 | `toReceivedLineMap`: DTO hoặc danh sách dòng có rỗng không | Rỗng / Không rỗng |
| D2 | `toReceivedLineMap`: dòng hàng có hợp lệ không | Hợp lệ / Không hợp lệ |
| D3 | `toReceivedLineMap`: mã dòng có bị trùng không | Trùng / Không trùng |
| D4 | `requireInTransit`: trạng thái đơn có là `IN_TRANSIT` không | Đúng / Sai |
| D5 | `requireOnlyExistingLines`: dòng nhận có thuộc đơn đang chọn không | Thuộc / Không thuộc |
| D6 | Vòng lặp `reconcile`: mỗi dòng hàng được xử lý | Một dòng / Nhiều dòng |
| D7 | `createIfDiscrepant`: có sai lệch số lượng không | Có / Không |
| D8 | `increaseInternalInventory`: số lượng nhận có lớn hơn 0 không | Có / Không |
| D9 | `increaseInternalInventory`: tồn kho nội bộ đã tồn tại chưa | Có / Chưa |
| D10 | Mapping DTO: request/site/merchandise/createdAt có null không | Null / Không null |

### 3.2 Test case hộp trắng C1

| ID | Mục tiêu bao phủ | Dữ liệu | Kết quả mong đợi | Test tự động |
| --- | --- | --- | --- | --- |
| WB01 | D6, D7, D9 với nhiều dòng, một dòng khớp và một dòng sai lệch | 2 dòng: nhận đủ 10, nhận thiếu 3 | Lưu 2 dòng, cập nhật 2 tồn kho, tạo 1 biên bản | `reconcile_twoLinesCoversLoopBranchesForMatchedAndDiscrepantLines` |
| WB02 | D8 và default text của biên bản | Đặt 8, nhận 0, reason/note/createdBy rỗng | Không tăng tồn kho, biên bản dùng default reason/createdBy | `reconcile_zeroReceivedQuantityCoversInventorySkipAndDefaultReportTextBranches` |
| WB03 | D3 | DTO có 2 dòng cùng `lineId` | Ném lỗi dòng trùng trước khi truy vấn DB | `reconcile_duplicateLineIdCoversDuplicateValidationBranchBeforeDbAccess` |
| WB04 | D1 | DTO có danh sách dòng rỗng | Ném lỗi dữ liệu rỗng trước khi truy vấn DB | `reconcile_emptyLineListCoversEmptyInputValidationBranchBeforeDbAccess` |
| WB05 | D5 | DTO chứa `lineId` không thuộc đơn | Ném lỗi, không cập nhật kho, không lưu biên bản | `reconcile_lineIdNotBelongingToOrderCoversOnlyExistingLinesValidationBranch` |
| WB06 | D10 trong chi tiết đối soát | Order không có request/site, line không có merchandise | Mapping trả chuỗi rỗng/null đúng thiết kế | `getReconciliationDetail_nullAssociationsCoverNullableMappingBranches` |
| WB07 | D10 trong danh sách đơn đang vận chuyển | Một đơn đủ thông tin, một đơn thiếu request/site/createdAt | Mapping ngày tạo và trường null đúng thiết kế | `getInTransitOrders_coversCreatedDateAndNullableResponseMappingBranches` |

## 4. Kiểm thử use case UC6

### 4.1 Scenarios

| Scenario | Mô tả | Kết quả mong đợi |
| --- | --- | --- |
| S1 | Luồng chính: bộ phận quản lý kho chọn đơn đang vận chuyển, nhập số lượng nhận đúng, xác nhận nhập kho | Đơn chuyển `RECEIVED`, tồn kho tăng, không có biên bản sai lệch |
| S2 | Luồng thay thế: hàng nhận thực tế thiếu so với đơn đặt | Đơn vẫn được nhập kho, tồn kho tăng theo số nhận, hệ thống tạo biên bản sai lệch |
| S3 | Luồng lỗi: đơn đã nhập kho hoặc không còn đang vận chuyển | Hệ thống từ chối xác nhận lại, không cập nhật kho |
| S4 | Luồng lỗi: dữ liệu gửi lên chứa dòng hàng không thuộc đơn đang chọn | Hệ thống từ chối, không cập nhật dòng hàng/tồn kho/biên bản |

### 4.2 Test case use case

| ID | Scenario | Dữ liệu | Kết quả mong đợi | Test tự động |
| --- | --- | --- | --- | --- |
| UC6-01 | S1 | 2 dòng: đặt 5 nhận 5, đặt 8 nhận 8 | `RECEIVED`, lưu 2 dòng, tăng tồn kho 2 lần, không lưu biên bản | `uc006_mainScenario_confirmReceivingWithoutDiscrepancy` |
| UC6-02 | S2 | 1 dòng: đặt 4 nhận 3 | `RECEIVED`, tạo biên bản lệch 1 | `uc006_alternativeScenario_confirmReceivingWithDiscrepancyCreatesReport` |
| UC6-03 | S3 | Đơn status `RECEIVED` | Ném lỗi `IN_TRANSIT`, không cập nhật kho/biên bản | `uc006_exceptionScenario_orderAlreadyReceivedCannotBeConfirmedAgain` |
| UC6-04 | S4 | Đơn có line `301`, DTO gửi line `999` | Ném lỗi line không thuộc đơn, đơn vẫn `IN_TRANSIT` | `uc006_exceptionScenario_receivedLineNotBelongingToSelectedOrderStopsBeforeUpdatingStock` |

## 5. Cách chạy kiểm thử

Chạy toàn bộ test UC6:

```powershell
.\mvnw.cmd -q "-Dtest=PurchaseOrderServiceBlackBoxTest,PurchaseOrderServiceWhiteBoxC1Test,PurchaseOrderUseCaseScenarioTest" test
```

Chạy từng nhóm test:

```powershell
.\mvnw.cmd -q "-Dtest=PurchaseOrderServiceBlackBoxTest" test
.\mvnw.cmd -q "-Dtest=PurchaseOrderServiceWhiteBoxC1Test" test
.\mvnw.cmd -q "-Dtest=PurchaseOrderUseCaseScenarioTest" test
```

Các test này là unit test dùng Mockito để giả lập repository, không cần kết nối Supabase và không cần chạy JavaFX UI.