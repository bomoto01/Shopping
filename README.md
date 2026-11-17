# E-Commerce Clothing Website with AI Recommendation (Spring Boot Backend)

Dự án Website bán hàng quần áo thời trang tích hợp trí tuệ nhân tạo (AI) gợi ý sản phẩm cá nhân hóa, phát triển trên nền tảng **Java 21** và **Spring Boot 3.x**.

---

## 🛠️ Công nghệ sử dụng
- **Ngôn ngữ:** Java 21 (Adoptium Temurin LTS)
- **Framework:** Spring Boot 3.3.5
- **ORM / Persistence:** Spring Data JPA / Hibernate (MySQL 8.x)
- **Security:** Spring Security 6, Stateless JWT (JJWT 0.12.6, SHA-256 Hashed Refresh Token)
- **Validation:** Hibernate Validator / Spring Validation
- **Boilerplate & Object Mapping:** Project Lombok 1.18.36, MapStruct 1.5.5.Final
- **API Documentation:** SpringDoc OpenAPI 3 (Swagger UI 2.6.0)
- **Monitoring & Metrics:** Spring Boot Actuator
- **Build Tool:** Apache Maven 3.9+
- **Unit & Integration Testing:** JUnit 5, Mockito 5 (Subclass Mock Maker tối ưu), MockMvc, H2 In-Memory Test Database

---

## 📂 Cấu trúc thư mục (Layered Architecture)
```
src/main/java/com/shopping/
├── apiResponse/       # Chuẩn hóa định dạng phản hồi ApiResponse<T>, PageResponse<T>
├── config/            # Cấu hình OpenAPI, CORS, JPA Auditing, RestClient AI
├── constants/         # Hằng số bảo mật, phân trang, thông báo hệ thống
├── controller/        # REST Controllers (/api/v1/...)
├── dto/               # Data Transfer Objects (Request / Response riêng biệt)
├── entity/            # 20 JPA Entities tương ứng 20 bảng quan hệ
├── enums/             # Các kiểu liệt kê Role, OrderStatus, InteractionType,...
├── exception/         # Custom Exceptions và GlobalExceptionHandler (@RestControllerAdvice)
├── mapper/            # MapStruct Mappers ánh xạ Entity <-> DTO
├── repository/        # Spring Data JPA Repositories
├── security/          # Cấu hình Spring Security, JWT Token Provider, Filters
├── service/           # Service Interfaces & Implementations
└── util/              # Tiện ích tạo mã đơn hàng, xử lý slug tiếng Việt,...
```

---

## 🚀 Hướng dẫn khởi chạy

### 1. Yêu cầu môi trường
- **Java 21** (JDK 21 LTS)
- **Maven 3.8+**
- **MySQL 8.0+**

### 2. Cấu hình cơ sở dữ liệu & Biến môi trường
Tạo cơ sở dữ liệu MySQL:
```sql
CREATE DATABASE shopping_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Cấu hình các biến môi trường (hoặc sử dụng mặc định trong `application.yml`):
| Biến môi trường | Mặc định | Mô tả |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Cổng HTTP server |
| `DB_HOST` | `localhost` | Địa chỉ máy chủ MySQL |
| `DB_PORT` | `3306` | Cổng kết nối MySQL |
| `DB_NAME` | `shopping_db` | Tên cơ sở dữ liệu |
| `DB_USERNAME` | `root` | Tài khoản MySQL |
| `DB_PASSWORD` | `(trống)` | Mật khẩu MySQL |
| `JWT_SECRET` | `(chuỗi 256-bit bí mật)` | Khóa bí mật ký JWT |
| `AI_SERVICE_URL` | `http://localhost:8000` | URL dịch vụ gợi ý FastAPI |

Tài khoản quản trị viên mặc định được khởi tạo tự động khi chạy lần đầu:
- **Email:** `admin@shopping.vn`
- **Mật khẩu:** `Admin@123456`

### 3. Biên dịch và kiểm thử
```bash
mvn clean test
```

### 4. Khởi chạy ứng dụng
```bash
mvn spring-boot:run
```

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Health Check:** `http://localhost:8080/api/v1/health`

---

## 📌 Tiến độ phát triển & Nghiệm thu
- [x] **Giai đoạn 1:** Khởi tạo dự án, Maven POM, cấu hình MySQL, ApiResponse, GlobalExceptionHandler, cấu trúc Layered.
- [x] **Giai đoạn 2:** Xây dựng Authentication & Authorization:
  - Đăng ký, Đăng nhập, JWT Access/Refresh Token (SHA-256 hashed), Logout, Thu hồi token, Đổi mật khẩu.
  - Phân quyền RBAC (`ROLE_CUSTOMER`, `ROLE_STAFF`, `ROLE_ADMIN`).
- [x] **Giai đoạn 3:** Xây dựng CRUD cho 20 bảng cơ sở dữ liệu:
  - Nhóm 1: User Profile, Sổ địa chỉ (Address), Quyền riêng tư AI (TC-10).
  - Nhóm 2: Danh mục phân cấp (Category), Sản phẩm (Product), Biến thể SKU (ProductVariant), Thư viện ảnh (ProductImage).
  - Nhóm 3: Quản lý Tồn kho (Inventory) & Lịch sử biến động kho (InventoryTransaction).
  - Nhóm 4: Mã giảm giá (Coupon) với ràng buộc ngày hết hạn (TC-06), đơn tối thiểu (TC-07), giới hạn lượt dùng (TC-08).
  - Nhóm 5: Danh sách yêu thích (Wishlist) & Giỏ hàng (Cart, CartItem).
- [x] **Giai đoạn 4:** Triển khai nghiệp vụ bán hàng chuyên sâu:
  - Quản lý Đơn hàng (Orders): Checkout giao dịch `@Transactional`, kiểm tra và trừ tồn kho an toàn (`ORDER_RESERVATION`, TC-05), lưu snapshot giá/SKU/thuộc tính/địa chỉ, hủy đơn hoàn tồn kho (`ORDER_CANCELLATION`), quản lý vòng đời trạng thái đơn hàng và ghi nhật ký lịch sử (`OrderStatusHistory`).
  - Đánh giá Sản phẩm (Reviews): Ràng buộc chỉ đánh giá khi đơn hàng `DELIVERED`, ràng buộc duy nhất mỗi dòng sản phẩm chỉ được đánh giá 1 lần (TC-13), duyệt/ẩn đánh giá.
  - Tương tác Hành vi & Gợi ý AI: Chống giả mạo hành vi Client chỉ chấp nhận `VIEW`/`CLICK` (TC-11), tuân thủ quyền riêng tư Opt-out không lưu DB khi tắt (TC-10), gợi ý sản phẩm tích hợp AI và tự động chuyển sang Fallback sản phẩm phổ biến khi lỗi/timeout (TC-09).
  - Báo cáo Thống kê Doanh thu (Admin Dashboard): Tổng doanh thu các đơn hàng thành công, số lượng đơn theo từng trạng thái, tổng số khách hàng/sản phẩm, top sản phẩm bán chạy nhất hệ thống.
- [x] **Giai đoạn 5:** Kiểm thử toàn diện & Hoàn thiện:
  - Viết Integration Test hoàn chỉnh toàn bộ luồng nghiệp vụ (Đăng ký -> Đăng nhập -> Thêm địa chỉ -> Thêm giỏ hàng -> Đặt hàng -> Trừ kho -> Hủy đơn hoàn kho).
  - Đạt **70/70 ca kiểm thử (Unit Test + Integration Test)** thành công 100%.
  - Hoàn thiện tài liệu nghiệm thu hệ thống và đồng bộ toàn bộ source code lên GitHub.
