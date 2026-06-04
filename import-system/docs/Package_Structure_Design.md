# Thiet ke cau truc thu muc

Tai lieu nay chuan hoa cau truc package cho He thong dat hang nhap khau theo mo ta he thong va 6 use case trong anh.

## Danh sach use case

- `uc1`: Tao yeu cau nhap hang
- `uc2`: Tim kiem Site va phan loai mat hang
- `uc3`: Cap nhat thong tin ton kho Site
- `uc4`: Quan ly thong tin van chuyen Site
- `uc5`: Xu ly lap ke hoach dat hang
- `uc6`: Doi soat va xac nhan nhap kho
- `chung`: Thanh phan dung chung cho nhieu use case

## Cau truc tong the

```text
import-system/
|-- pom.xml
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   |-- module-info.java
|   |   |   `-- itss/group11/
|   |   |       |-- ImportSystemBackendApplication.java
|   |   |       |-- entity/
|   |   |       |   |-- chung/
|   |   |       |   |-- uc1/
|   |   |       |   |-- uc2/
|   |   |       |   |-- uc3/
|   |   |       |   |-- uc4/
|   |   |       |   |-- uc5/
|   |   |       |   `-- uc6/
|   |   |       |-- controller/
|   |   |       |   |-- chung/
|   |   |       |   |-- uc1/
|   |   |       |   |-- uc2/
|   |   |       |   |-- uc3/
|   |   |       |   |-- uc4/
|   |   |       |   |-- uc5/
|   |   |       |   `-- uc6/
|   |   |       `-- subsystem/
|   |   |           |-- chung/
|   |   |           |-- uc1/
|   |   |           |-- uc2/
|   |   |           |-- uc3/
|   |   |           |-- uc4/
|   |   |           |-- uc5/
|   |   |           `-- uc6/
|   |   `-- resources/
|   |       `-- itss/group11/
|   |           `-- view/
|   |               |-- chung/
|   |               |-- uc1/
|   |               |-- uc2/
|   |               |-- uc3/
|   |               |-- uc4/
|   |               |-- uc5/
|   |               `-- uc6/
|   `-- test/
|       `-- java/
|           `-- itss/group11/
|               `-- test/
|                   |-- chung/
|                   |-- uc1/
|                   |-- uc2/
|                   |-- uc3/
|                   |-- uc4/
|                   |-- uc5/
|                   `-- uc6/
`-- docs/
    |-- Package_Structure_Design.md
    `-- allocation/
```

## Quy uoc tung tang

### `entity`

Chua cac lop du lieu cua he thong.

- `entity/chung`: JPA entity/domain model dung chung, vi cac bang co quan he qua nhieu use case.
- `entity/uc1..uc6`: DTO, request/response object, row model hoac data object rieng cua tung use case.

Vi du:

```text
entity/chung/
|-- Merchandise.java
|-- ImportSite.java
|-- OrderRequest.java
|-- OrderRequestItem.java
|-- SiteInventory.java
|-- PurchaseOrder.java
|-- PurchaseOrderLine.java
`-- TransportInfo.java

entity/uc5/
|-- AllocationPlanDTO.java
|-- AllocationPlanItemDTO.java
|-- AllocationRequestRowDTO.java
|-- AllocationResultDTO.java
`-- SiteStockDTO.java
```

### `controller`

Chua lop dieu phoi giao dien va API endpoint.

- `controller/chung`: App, Login, Dashboard, StageManager, session, phan quyen.
- `controller/ucX`: REST controller va JavaFX controller cua use case X.

Vi du:

```text
controller/uc5/
|-- AllocationController.java
`-- AllocationListController.java
```

### `subsystem`

Chua xu ly nghiep vu, tich hop du lieu va cong tac truy cap repository.

- `subsystem/chung`: Repository va thanh phan truy cap du lieu dung chung.
- `subsystem/ucX`: Service/subsystem nghiep vu rieng cua use case X.

Vi du:

```text
subsystem/chung/
|-- ImportSiteRepository.java
|-- OrderRequestRepository.java
|-- PurchaseOrderRepository.java
`-- SiteInventoryRepository.java

subsystem/uc5/
|-- AllocationService.java
`-- InventoryCheckService.java
```

### `view`

Chua FXML theo use case.

```text
resources/itss/group11/view/chung/
|-- login.fxml
`-- dashboard.fxml

resources/itss/group11/view/uc5/
`-- allocationList.fxml
```

### `test`

Chua test tuong ung voi use case.

- `test/chung`: Test thanh phan dung chung.
- `test/ucX`: Unit test, white-box test, black-box test va scenario test cua use case X.

Vi du:

```text
test/uc5/
|-- AllocationServiceBlackBoxTest.java
|-- AllocationServiceWhiteBoxC1Test.java
|-- AllocationUseCaseScenarioTest.java
`-- AllocationServiceTestData.java
```

## Mapping file theo use case

### UC1: Tao yeu cau nhap hang

```text
entity/uc1/
|-- MerchandiseOptionDTO.java
|-- OrderRequestCreationDTO.java
|-- OrderRequestDetailDTO.java
`-- OrderRequestSummaryDTO.java

controller/uc1/
|-- OrderRequestController.java
`-- OrderRequestCreateController.java

subsystem/uc1/
`-- OrderRequestService.java

view/uc1/
`-- orderRequestCreate.fxml

test/uc1/
```

### UC2: Tim kiem Site va phan loai mat hang

```text
entity/uc2/
|-- InventoryInquiryRequestDTO.java
|-- InventoryInquiryResponseDTO.java
|-- InventoryInquirySendResultDTO.java
|-- OrderRequestClassificationDTO.java
`-- SiteClassificationResultDTO.java

controller/uc2/
|-- SiteSyncController.java
`-- SiteClassificationController.java

subsystem/uc2/
`-- SiteSyncService.java

view/uc2/
`-- siteClassification.fxml

test/uc2/
```

### UC3: Cap nhat thong tin ton kho Site

```text
entity/uc3/
|-- InternalInventoryRowDTO.java
|-- InventoryRowDTO.java
|-- InventoryUpdateDTO.java
`-- SiteOptionDTO.java

controller/uc3/
|-- WarehouseController.java
`-- SiteInventoryManageController.java

subsystem/uc3/
`-- InventoryManagementService.java

view/uc3/
`-- siteInventoryManage.fxml

test/uc3/
```

### UC4: Quan ly thong tin van chuyen Site

```text
entity/uc4/
|-- PendingPurchaseOrderDTO.java
|-- TransportCreateDTO.java
|-- TransportDetailDTO.java
|-- TransportStatusUpdateDTO.java
`-- TransportUpdateDTO.java

controller/uc4/
|-- TransportController.java
`-- SiteShippingManageController.java

subsystem/uc4/
`-- TransportService.java

view/uc4/
`-- siteShippingManage.fxml

test/uc4/
```

### UC5: Xu ly lap ke hoach dat hang

```text
entity/uc5/
|-- AllocationPlanDTO.java
|-- AllocationPlanItemDTO.java
|-- AllocationRequestRowDTO.java
|-- AllocationResultDTO.java
`-- SiteStockDTO.java

controller/uc5/
|-- AllocationController.java
`-- AllocationListController.java

subsystem/uc5/
|-- AllocationService.java
`-- InventoryCheckService.java

view/uc5/
`-- allocationList.fxml

test/uc5/
|-- AllocationServiceBlackBoxTest.java
|-- AllocationServiceWhiteBoxC1Test.java
|-- AllocationUseCaseScenarioTest.java
`-- AllocationServiceTestData.java
```

### UC6: Doi soat va xac nhan nhap kho

```text
entity/uc6/
|-- PartialOrderSelectionDTO.java
|-- PurchaseOrderResponseDTO.java
|-- ReceivedLineDTO.java
|-- ReconciliationDetailDTO.java
|-- ReconciliationResultDTO.java
`-- ReconciliationSubmitDTO.java

controller/uc6/
|-- PurchaseOrderController.java
`-- OrderReconciliationController.java

subsystem/uc6/
`-- PurchaseOrderService.java

view/uc6/
`-- orderReconciliation.fxml

test/uc6/
```

## Nguyen tac dat file moi

- Lop JPA entity moi dat trong `entity/chung` neu duoc nhieu UC dung.
- DTO chi phuc vu mot UC dat trong `entity/ucX`.
- JavaFX controller va REST controller cua UC nao dat trong `controller/ucX`.
- Service xu ly nghiep vu cua UC nao dat trong `subsystem/ucX`.
- Repository dat trong `subsystem/chung` neu dung chung entity va duoc nhieu service inject.
- FXML cua UC nao dat trong `resources/itss/group11/view/ucX`.
- Test cua UC nao dat trong `src/test/java/itss/group11/test/ucX`.
