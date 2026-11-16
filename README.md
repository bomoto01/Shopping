# E-Commerce Clothing Website with AI Recommendation (Spring Boot Backend)

Dự án Website bán hàng quần áo thời trang tích hợp trí tuệ nhân tạo (AI) gợi ý sản phẩm cá nhân hóa, phát triển trên nền tảng **Java 21** và **Spring Boot 3.x**.

---

## 🛠️ Công nghệ sử dụng
- **Ngôn ngữ:** Java 21 (LTS)
- **Framework:** Spring Boot 3.3.5
- **ORM / Persistence:** Spring Data JPA / Hibernate (MySQL 8.x)
- **Security:** Spring Security 6, Stateless JWT (JJWT 0.12.6)
- **Validation:** Hibernate Validator / Spring Validation
- **Boilerplate & Object Mapping:** Project Lombok, MapStruct
- **API Documentation:** SpringDoc OpenAPI 3 (Swagger UI)
- **Monitoring & Metrics:** Spring Boot Actuator
- **Build Tool:** Apache Maven 3.9+

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

## 📌 Tiến độ phát triển
- [x] **Giai đoạn 1:** Khởi tạo dự án, Maven POM, cấu hình MySQL, ApiResponse, GlobalExceptionHandler, cấu trúc Layered.
- [ ] **Giai đoạn 2:** Xây dựng Authentication, Authorization, JWT Access/Refresh Token, phân quyền ROLE_CUSTOMER / ROLE_STAFF / ROLE_ADMIN.
- [ ] **Giai đoạn 3:** Xây dựng CRUD cho 20 bảng cơ sở dữ liệu (User, Product, Category, Variant, Cart, Order, Coupon, Review,...).
- [ ] **Giai đoạn 4:** Triển khai nghiệp vụ bán hàng chuyên sâu, trừ tồn kho an toàn, chống giả mạo sự kiện AI, fallback gợi ý.
- [ ] **Giai đoạn 5:** Kiểm thử toàn diện, viết Unit Test / Integration Test, tối ưu hiệu năng.
