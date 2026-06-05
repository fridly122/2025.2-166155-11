# Hướng Dẫn Cuối: Class Diagram Mức Thiết Kế Khớp Code

Tài liệu này tổng hợp cách vẽ class diagram mức thiết kế cho hệ thống đặt hàng nhập khẩu theo tiêu chí **các class/attributes/methods đưa vào diagram phải khớp source code hiện tại**.

Quy ước:

- Không đưa `Service`, `Repository`, `DTO`, REST controller, JavaFX controller vào diagram chính.
- Không đưa các class phân tích không tồn tại trong code như `User`, `OrderStaff`, `TransportStaff`, `WarehouseStaff`, `SalesDepartment`, `ReceivingRecord`, `ReceivingItem`, `TransportRecord`, `AllocationPlan`, `ClassificationResult`, `SiteMerchandise`.
- Không vẽ enum thành class riêng. Chỉ giữ enum như kiểu dữ liệu của attribute, ví dụ `status: OrderRequestStatus`, `deliveryMeans: DeliveryMeans`.
- Không vẽ security class như `UserRole`, `AppFeature`, `LoginSession`.
- Không vẽ getter/setter/builder/constructor do Lombok hoặc Java sinh ra.
- Role/association name trong Astah ghi bằng tiếng Anh, ví dụ `items`, `orderRequest`, `merchandise`, `sourceSite`.
- Dùng đúng tên class trong code để đảm bảo khớp code: `InventoryInquiry`, `InventoryInquiryItem`, `PurchaseOrderLine`, `InternalWarehouseInventory`.

## 1. Package Nên Tạo

Trong Astah, tạo các package:

- `entity.common`: các entity nghiệp vụ chính.

Nếu diagram quá rộng, tách thành các diagram:

- `OrderRequestDesign`
- `SiteInventoryAndInquiryDesign`
- `PurchaseOrderAndTransportDesign`
- `ReceivingAndInternalInventoryDesign`

## 2. Entity/Common Classes

### `OrderRequest`

Source: `itss.group11.entity.chung.OrderRequest`

Attributes:

- `requestCode: String`
- `status: OrderRequestStatus`
- `desiredDeliveryDate: LocalDate`
- `createdAt: LocalDateTime`
- `createdBy: String`
- `items: List<OrderRequestItem>`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `OrderRequest "1" *-- "0..*" OrderRequestItem : items`
- `OrderRequest "1" -- "0..*" InventoryInquiry : inventoryInquiries`
- `OrderRequest "1" -- "0..*" PurchaseOrder : purchaseOrders`

### `OrderRequestItem`

Source: `itss.group11.entity.chung.OrderRequestItem`

Attributes:

- `id: Long`
- `orderRequest: OrderRequest`
- `merchandise: Merchandise`
- `quantityOrdered: Integer`
- `unit: String`
- `desiredDeliveryDate: LocalDate`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `OrderRequestItem "*" --> "1" OrderRequest : orderRequest`
- `OrderRequestItem "*" --> "1" Merchandise : merchandise`

### `Merchandise`

Source: `itss.group11.entity.chung.Merchandise`

Attributes:

- `code: String`
- `name: String`
- `unit: String`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `Merchandise "1" -- "0..*" OrderRequestItem : orderRequestItems`
- `Merchandise "1" -- "0..*" SiteInventory : siteInventories`
- `Merchandise "1" -- "0..*" InventoryInquiryItem : inventoryInquiryItems`
- `Merchandise "1" -- "0..*" PurchaseOrderLine : purchaseOrderLines`
- `Merchandise "1" -- "0..*" DiscrepancyReport : discrepancyReports`
- `Merchandise "1" -- "0..*" InternalWarehouseInventory : internalWarehouseInventories`

### `ImportSite`

Source: `itss.group11.entity.chung.ImportSite`

Attributes:

- `siteCode: String`
- `siteName: String`
- `daysByShip: Integer`
- `daysByAir: Integer`
- `otherInfo: String`
- `merchandiseList: List<Merchandise>`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `ImportSite "*" -- "*" Merchandise : merchandiseList`
- `ImportSite "1" -- "0..*" SiteInventory : siteInventories`
- `ImportSite "1" -- "0..*" InventoryInquiry : inventoryInquiries`
- `ImportSite "1" -- "0..*" PurchaseOrder : purchaseOrders`
- `ImportSite "1" -- "0..*" TransportInfo : sourceTransports`

### `SiteInventory`

Source: `itss.group11.entity.chung.SiteInventory`

Attributes:

- `id: Long`
- `importSite: ImportSite`
- `merchandise: Merchandise`
- `inStockQuantity: Integer`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `SiteInventory "*" --> "1" ImportSite : importSite`
- `SiteInventory "*" --> "1" Merchandise : merchandise`

### `InventoryInquiry`

Source: `itss.group11.entity.chung.InventoryInquiry`

Attributes:

- `inquiryId: String`
- `orderRequest: OrderRequest`
- `importSite: ImportSite`
- `status: InquiryStatus`
- `itemCount: Integer`
- `createdAt: LocalDateTime`
- `items: List<InventoryInquiryItem>`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `InventoryInquiry "*" --> "1" OrderRequest : orderRequest`
- `InventoryInquiry "*" --> "1" ImportSite : importSite`
- `InventoryInquiry "1" *-- "0..*" InventoryInquiryItem : items`

### `InventoryInquiryItem`

Source: `itss.group11.entity.chung.InventoryInquiryItem`

Attributes:

- `id: Long`
- `inventoryInquiry: InventoryInquiry`
- `merchandise: Merchandise`
- `requestedQty: Integer`
- `unit: String`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `InventoryInquiryItem "*" --> "1" InventoryInquiry : inventoryInquiry`
- `InventoryInquiryItem "*" --> "1" Merchandise : merchandise`

### `PurchaseOrder`

Source: `itss.group11.entity.chung.PurchaseOrder`

Attributes:

- `orderId: String`
- `orderRequest: OrderRequest`
- `site: ImportSite`
- `status: PurchaseOrderStatus`
- `createdAt: LocalDateTime`
- `orderLines: List<PurchaseOrderLine>`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `PurchaseOrder "*" --> "1" OrderRequest : orderRequest`
- `PurchaseOrder "*" --> "1" ImportSite : site`
- `PurchaseOrder "1" *-- "0..*" PurchaseOrderLine : orderLines`
- `PurchaseOrder "1" -- "0..*" TransportInfo : transports`
- `PurchaseOrder "1" -- "0..*" DiscrepancyReport : discrepancyReports`

### `PurchaseOrderLine`

Source: `itss.group11.entity.chung.PurchaseOrderLine`

Attributes:

- `id: Long`
- `purchaseOrder: PurchaseOrder`
- `merchandise: Merchandise`
- `orderedQty: Integer`
- `receivedQty: Integer`
- `unit: String`
- `deliveryMeans: DeliveryMeans`

Methods:

- `calculateDifference(): int`

Relationships:

- `PurchaseOrderLine "*" --> "1" PurchaseOrder : purchaseOrder`
- `PurchaseOrderLine "*" --> "1" Merchandise : merchandise`

### `TransportInfo`

Source: `itss.group11.entity.chung.TransportInfo`

Attributes:

- `id: Long`
- `sourceSite: ImportSite`
- `destinationSiteName: String`
- `transportStatus: TransportStatus`
- `deliveryDays: Integer`
- `vehicle: String`
- `purchaseOrder: PurchaseOrder`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `TransportInfo "*" --> "1" ImportSite : sourceSite`
- `TransportInfo "*" --> "0..1" PurchaseOrder : purchaseOrder`

### `DiscrepancyReport`

Source: `itss.group11.entity.chung.DiscrepancyReport`

Attributes:

- `reportId: String`
- `purchaseOrder: PurchaseOrder`
- `merchandise: Merchandise`
- `orderedQty: Integer`
- `receivedQty: Integer`
- `differenceQty: Integer`
- `reason: String`
- `note: String`
- `createdAt: LocalDateTime`
- `createdBy: String`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `DiscrepancyReport "*" --> "1" PurchaseOrder : purchaseOrder`
- `DiscrepancyReport "*" --> "1" Merchandise : merchandise`

### `InternalWarehouseInventory`

Source: `itss.group11.entity.chung.InternalWarehouseInventory`

Attributes:

- `id: Long`
- `merchandise: Merchandise`
- `inStockQuantity: Integer`
- `updatedAt: LocalDateTime`

Methods:

- Không có method nghiệp vụ trực tiếp trong source.

Relationships:

- `InternalWarehouseInventory "*" --> "1" Merchandise : merchandise`

## 3. Relationship Checklist

Composition:

- `OrderRequest "1" *-- "0..*" OrderRequestItem : items`
- `InventoryInquiry "1" *-- "0..*" InventoryInquiryItem : items`
- `PurchaseOrder "1" *-- "0..*" PurchaseOrderLine : orderLines`

Association:

- `OrderRequestItem "*" --> "1" OrderRequest : orderRequest`
- `OrderRequestItem "*" --> "1" Merchandise : merchandise`
- `ImportSite "*" -- "*" Merchandise : merchandiseList`
- `SiteInventory "*" --> "1" ImportSite : importSite`
- `SiteInventory "*" --> "1" Merchandise : merchandise`
- `InventoryInquiry "*" --> "1" OrderRequest : orderRequest`
- `InventoryInquiry "*" --> "1" ImportSite : importSite`
- `InventoryInquiryItem "*" --> "1" InventoryInquiry : inventoryInquiry`
- `InventoryInquiryItem "*" --> "1" Merchandise : merchandise`
- `PurchaseOrder "*" --> "1" OrderRequest : orderRequest`
- `PurchaseOrder "*" --> "1" ImportSite : site`
- `PurchaseOrderLine "*" --> "1" PurchaseOrder : purchaseOrder`
- `PurchaseOrderLine "*" --> "1" Merchandise : merchandise`
- `TransportInfo "*" --> "1" ImportSite : sourceSite`
- `TransportInfo "*" --> "0..1" PurchaseOrder : purchaseOrder`
- `DiscrepancyReport "*" --> "1" PurchaseOrder : purchaseOrder`
- `DiscrepancyReport "*" --> "1" Merchandise : merchandise`
- `InternalWarehouseInventory "*" --> "1" Merchandise : merchandise`

## 4. Bố Cục Gợi Ý Trong Astah

Đặt các class theo cụm:

1. Cụm yêu cầu: `OrderRequest`, `OrderRequestItem`.
2. Cụm hàng/site/tồn kho: `Merchandise`, `ImportSite`, `SiteInventory`.
3. Cụm hỏi tồn kho: `InventoryInquiry`, `InventoryInquiryItem`.
4. Cụm đơn đặt hàng: `PurchaseOrder`, `PurchaseOrderLine`.
5. Cụm vận chuyển: `TransportInfo`.
6. Cụm nhập kho/sai lệch: `InternalWarehouseInventory`, `DiscrepancyReport`.

## 5. Không Đưa Vào Diagram Chính

Không đưa các class sau nếu mục tiêu là diagram chính khớp code và gọn:

- `Service`
- `Repository`
- `DTO`
- REST controller
- JavaFX controller
- Row model của màn hình
- Enum class: `OrderRequestStatus`, `InquiryStatus`, `PurchaseOrderStatus`, `DeliveryMeans`, `TransportStatus`
- Security class: `UserRole`, `AppFeature`, `LoginSession`
- Class phân tích không tồn tại trong code: `User`, `OrderStaff`, `TransportStaff`, `WarehouseStaff`, `SalesDepartment`, `SiteMerchandise`, `ClassificationResult`, `AllocationPlan`, `AllocationPlanItem`, `TransportRecord`, `ReceivingRecord`, `ReceivingItem`.
