# Bài tập 7 - Kiểm thử UC001: Tạo yêu cầu nhập hàng

**Thành viên phụ trách:** Nguyên (UC1)

## 1. Module được chọn

- **Use case phụ trách:** UC001 - Tạo yêu cầu nhập hàng.
- **Lớp kiểm thử:** `itss.group11.subsystem.uc1.OrderRequestService`
- **Phương thức trọng tâm:**
  - `createRequest(OrderRequestCreationDTO dto)` — luồng chính tạo yêu cầu
  - `searchRequests(String requestCode)` — tìm kiếm yêu cầu
  - `updateRequest(String requestCode, OrderRequestCreationDTO dto)` — cập nhật yêu cầu PENDING
  - `deleteRequest(String requestCode)` — hủy/xóa yêu cầu PENDING

**Lý do chọn module:** `OrderRequestService` là subsystem xử lý nghiệp vụ chính của UC1. Lớp này nhận dữ liệu từ REST controller, kiểm tra hợp lệ, sinh mã yêu cầu, gắn mặt hàng và lưu `OrderRequest` / `OrderRequestItem` với trạng thái `PENDING`.

**Các class kiểm thử tự động (full name):**

| Full name | Mục đích |
| --- | --- |
| `itss.group11.test.uc1.OrderRequestServiceBlackBoxTest` | Kiểm thử hộp đen |
| `itss.group11.test.uc1.OrderRequestServiceWhiteBoxC1Test` | Kiểm thử hộp trắng độ đo C1 |
| `itss.group11.test.uc1.OrderRequestUseCaseScenarioTest` | Kiểm thử use case UC001 |
| `itss.group11.test.uc1.OrderRequestServiceTestData` | Dữ liệu dùng chung cho test |

---

## 2. Kiểm thử hộp đen (áp dụng trước)

### 2.1 Kỹ thuật áp dụng

Áp dụng **phân hoạch tương đương** và **giá trị biên** trên đầu vào/đầu ra quan sát được của `createRequest`, không cần biết chi tiết nhánh bên trong.

| Nhóm | Điều kiện đầu vào | Kỳ vọng |
| --- | --- | --- |
| P1 | DTO hợp lệ, mã yêu cầu chưa tồn tại | Tạo thành công, trạng thái `PENDING` |
| P2 | Không nhập mã yêu cầu | Hệ thống tự sinh mã `REQxxx` |
| P3 | Mã yêu cầu đã tồn tại | Báo lỗi trùng mã |
| P4 | DTO null hoặc không có mặt hàng | Báo lỗi dữ liệu không hợp lệ |
| P5 | Mã mặt hàng không tồn tại | Báo lỗi không tìm thấy mặt hàng |
| P6 | Số lượng ≤ 0 | Báo lỗi số lượng |
| P7 | Ngày nhận ≤ hôm nay | Báo lỗi ngày phải sau hôm nay |
| P8 | Ngày sai định dạng | Báo lỗi định dạng `yyyy-MM-dd` |

### 2.2 Test case hộp đen

| ID | Test case | Dữ liệu | Kết quả mong đợi | Test tự động | Kết quả |
| --- | --- | --- | --- | --- | --- |
| BB01 | Tạo yêu cầu hợp lệ | `REQ010`, `MH001`, SL 5, ngày tương lai | Trả về detail `PENDING`, 1 dòng hàng | `createRequest_validDataWithExplicitCode_returnsPendingDetail` | PASS |
| BB02 | Tự sinh mã yêu cầu | Mã trống, max hiện có `REQ005` | Sinh `REQ006` | `createRequest_blankRequestCode_generatesUniqueCode` | PASS |
| BB03 | Mã yêu cầu trùng | `REQ010` đã tồn tại | Ném lỗi "Mã yêu cầu đã tồn tại" | `createRequest_duplicateRequestCode_throwsError` | PASS |
| BB04 | DTO null | `null` | Ném lỗi dữ liệu trống | `createRequest_nullDto_throwsError` | PASS |
| BB05 | Không có mặt hàng | Danh sách item rỗng | Ném lỗi thiếu mặt hàng | `createRequest_emptyItems_throwsError` | PASS |
| BB06 | Mặt hàng không tồn tại | `MH404` | Ném lỗi không tìm thấy mặt hàng | `createRequest_unknownMerchandise_throwsError` | PASS |
| BB07 | Số lượng = 0 | `MH001`, SL 0 | Ném lỗi số lượng | `createRequest_zeroQuantity_throwsError` | PASS |
| BB08 | Ngày nhận = hôm nay | Ngày hiện tại | Ném lỗi ngày phải sau hôm nay | `createRequest_pastDeliveryDate_throwsError` | PASS |
| BB09 | Ngày sai định dạng | `31/12/2026` | Ném lỗi định dạng | `createRequest_invalidDateFormat_throwsError` | PASS |

---

## 3. Kiểm thử hộp trắng C1 (áp dụng sau hộp đen)

### 3.1 Kỹ thuật áp dụng

Áp dụng kiểm thử hộp trắng theo **độ đo C1** (statement/branch coverage). Mỗi quyết định quan trọng trong `OrderRequestService` cần ít nhất một test kích hoạt nhánh **đúng** hoặc **sai**.

| Mã | Vị trí logic | Nhánh cần bao phủ |
| --- | --- | --- |
| D1 | `validateCreationDTO`: dto == null | Null / Không null |
| D2 | `validateCreationDTO`: items rỗng | Rỗng / Có item |
| D3 | `normalizeOrGenerateRequestCode`: mã trống | Tự sinh / Dùng mã nhập |
| D4 | `existsById`: mã đã tồn tại | Có / Không |
| D5 | `generateUniqueRequestCode`: vòng `while exists` | Lần đầu trùng / Không trùng |
| D6 | `formatRequestCode`: number < 1000 | Padding 3 chữ số |
| D7 | `formatRequestCode`: number ≥ 1000 | Không padding |
| D8 | `extractRequestNumber`: pattern `REQ(\d+)` | Khớp / Không khớp |
| D9 | `parseDesiredDeliveryDate`: ngày trống | Trống / Có giá trị |
| D10 | `parseDesiredDeliveryDate`: parse lỗi | Hợp lệ / Sai format |
| D11 | `parseDesiredDeliveryDate`: sau hôm nay | Đúng / Sai |
| D12 | `buildItems`: quantity null hoặc ≤ 0 | Hợp lệ / Không hợp lệ |
| D13 | `normalizeOrGenerateRequestCode`: trim + uppercase | Có khoảng trắng / chữ thường |

### 3.2 Test case hộp trắng C1

| ID | Mục tiêu bao phủ | Dữ liệu | Kết quả mong đợi | Test tự động | Kết quả |
| --- | --- | --- | --- | --- | --- |
| WB01 | D3, D6 | Mã trống, max `REQ002` | Sinh `REQ003` | `createRequest_autoGenerateCodeUsesThreeDigitFormat_coversFormatBelow1000Branch` | PASS |
| WB02 | D3, D7 | Mã trống, max `REQ999` | Sinh `REQ1000` | `createRequest_autoGenerateCodeUsesPlainNumber_coversFormatAtLeast1000Branch` | PASS |
| WB03 | D5 | `REQ002` trùng, `REQ003` trống | Sinh `REQ003` | `createRequest_codeCollisionRetriesGeneration_coversWhileExistsLoopBranch` | PASS |
| WB04 | D8 | Mã cũ `INVALID` | Bỏ qua, sinh `REQ001` | `createRequest_nonStandardExistingCodesIgnored_coversExtractRequestNumberNoMatchBranch` | PASS |
| WB05 | D3, D13 | Nhập ` req020 `, `mh001` | Lưu `REQ020`, `MH001` | `createRequest_trimAndUppercaseProvidedCode_coversNormalizeProvidedCodeBranch` | PASS |
| WB06 | D12 | quantity = null | Ném lỗi số lượng | `createRequest_nullQuantity_coversQuantityValidationBranch` | PASS |
| WB07 | D9 | Ngày chỉ có khoảng trắng | Ném lỗi ngày trống | `createRequest_blankDeliveryDate_coversRequireTextBranch` | PASS |

---

## 4. Kiểm thử use case UC001

### 4.1 Scenarios

| Scenario | Mô tả | Kết quả mong đợi |
| --- | --- | --- |
| S1 | Luồng chính: nhân viên sales nhập mặt hàng, số lượng, ngày nhận và tạo yêu cầu | Yêu cầu lưu DB với trạng thái `PENDING` |
| S2 | Luồng thay thế: dữ liệu không hợp lệ (mặt hàng sai, ngày quá khứ) | Hệ thống từ chối, không lưu |
| S3 | Luồng thay thế: tìm kiếm yêu cầu theo mã | Trả về danh sách khớp |
| S4 | Luồng thay thế: cập nhật yêu cầu PENDING | Cập nhật số lượng và ngày nhận |
| S5 | Luồng thay thế: hủy/xóa yêu cầu PENDING | Xóa cứng nếu chưa có inquiry; chuyển `CANCELLED` nếu đã có inquiry |

**Số test case thiết kế:** 7 (1 main + 6 alternative/extension)

### 4.2 Test case use case

| ID | Scenario | Dữ liệu | Kết quả mong đợi | Test tự động | Kết quả |
| --- | --- | --- | --- | --- | --- |
| UC001-01 | S1 | `REQ201`, `MH001`, SL 8 | Tạo thành công `PENDING` | `uc001_mainScenario_salesCreatesPendingOrderRequest` | PASS |
| UC001-02 | S2 | `MH999` không tồn tại | Không lưu, báo lỗi | `uc001_alternativeScenario_invalidMerchandiseRejected` | PASS |
| UC001-03 | S2 | Ngày `2020-01-01` | Không lưu, báo lỗi ngày | `uc001_alternativeScenario_invalidDeliveryDateRejected` | PASS |
| UC001-04 | S3 | Tìm `"204"` | Trả về `REQ204` | `uc001_alternativeScenario_searchRequestByCode` | PASS |
| UC001-05 | S4 | Cập nhật `REQ205`, SL 5 | Detail mới SL 5 | `uc001_alternativeScenario_updatePendingRequest` | PASS |
| UC001-06 | S5 | Xóa `REQ206`, chưa có inquiry | Xóa cứng khỏi DB | `uc001_alternativeScenario_deletePendingRequestWithoutInquiry` | PASS |
| UC001-07 | S5 | Hủy `REQ207`, đã có inquiry | Chuyển `CANCELLED` | `uc001_alternativeScenario_cancelRequestWithInventoryInquiry` | PASS |

---

## 5. Cách chạy kiểm thử

Chạy toàn bộ test UC1:

```bash
cd import-system
mvn -q "-Dtest=OrderRequest*Test" test
```

Chạy từng nhóm:

```bash
mvn -q "-Dtest=OrderRequestServiceBlackBoxTest" test
mvn -q "-Dtest=OrderRequestServiceWhiteBoxC1Test" test
mvn -q "-Dtest=OrderRequestUseCaseScenarioTest" test
```

Các test là **unit test**, dùng Mockito giả lập repository nên không cần kết nối PostgreSQL.

---

## 6. Gợi ý trình bày

1. Giới thiệu module `OrderRequestService` và phương thức `createRequest`.
2. Trình bày **hộp đen trước**: phân hoạch P1–P8 → bảng BB01–BB09.
3. Trình bày **hộp trắng C1 sau**: liệt kê quyết định D1–D13 → bảng WB01–WB07.
4. Trình bày **kiểm thử UC**: 5 scenarios → 7 test case UC001-01..07.
5. Chạy live `mvn test` và chỉ rõ 3 class test: `OrderRequestServiceBlackBoxTest`, `OrderRequestServiceWhiteBoxC1Test`, `OrderRequestUseCaseScenarioTest`.
