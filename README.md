<<<<<<< HEAD
# FU_Library
=======
# Hệ thống quản lý thư viện

Hệ thống quản lý thư viện được xây dựng bằng Spring Boot + Thymeleaf với cấu trúc 3 lớp (Controller-Service-Repository).

## Tính năng chính

### 1. Quản lý sách

- Thêm, sửa, xóa sách
- Tìm kiếm sách theo tên, tác giả
- Quản lý số lượng sách có sẵn
- Xem chi tiết sách và lịch sử mượn

### 2. Quản lý tác giả

- Thêm, sửa, xóa tác giả
- Tìm kiếm tác giả theo tên
- Quản lý thông tin tiểu sử tác giả

### 3. Quản lý người dùng

- Đăng ký tài khoản người dùng
- Quản lý số dư tài khoản
- Nạp tiền vào tài khoản
- Mua gói membership

### 4. Quản lý mượn/trả sách

- Mượn sách với hạn trả
- Trả sách và tính phạt trả muộn
- Theo dõi lịch sử mượn sách
- Quản lý sách quá hạn

### 5. Gói membership

- **Gói thường (0 VND)**: 5 sách/tháng
- **Gói nâng cao (20,000 VND)**: 10 sách/tháng
- **Gói cao cấp (100,000 VND)**: Không giới hạn

### 6. Hệ thống phạt

- Phạt 30,000 VND/ngày/quyển khi trả muộn
- Tự động cấm tài khoản khi số dư âm

## Cấu trúc dự án

```
src/main/java/com/example/demo/
├── config/                 # Cấu hình
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── controller/             # Controller layer
│   ├── HomeController.java
│   ├── BookController.java
│   ├── AuthorController.java
│   ├── UserController.java
│   ├── BorrowController.java
│   └── LoginController.java
├── service/                # Service layer
│   ├── BookService.java
│   ├── AuthorService.java
│   ├── UserService.java
│   ├── BorrowService.java
│   ├── MembershipService.java
│   └── LibraryService.java
├── repository/             # Repository layer
│   ├── BookRepository.java
│   ├── AuthorRepository.java
│   ├── UserRepository.java
│   ├── BorrowRecordRepository.java
│   ├── PaymentTransactionRepository.java
│   ├── MembershipRepository.java
│   ├── LibraryRepository.java
│   └── ManagerRepository.java
└── entity/                 # Entity layer
    ├── Book.java
    ├── Author.java
    ├── User.java
    ├── BorrowRecord.java
    ├── PaymentTransaction.java
    ├── Membership.java
    ├── Library.java
    └── Manager.java
```

## Cài đặt và chạy

### Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Cấu hình database

1. Tạo database MySQL:

```sql
CREATE DATABASE library_db;
```

2. Cập nhật thông tin kết nối trong `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Chạy ứng dụng

```bash
# Clone project
git clone <repository-url>
cd demo

# Build và chạy
mvn spring-boot:run
```

Truy cập: http://localhost:8080

## Tài khoản mặc định

### Manager (Quản lý)

- **Email**: admin@library.com
- **Mật khẩu**: admin123

### User (Người dùng)

- **Email**: user1@email.com
- **Mật khẩu**: 123456

## API Endpoints

### Sách

- `GET /books` - Danh sách sách
- `GET /books/create` - Form thêm sách
- `POST /books/create` - Tạo sách mới
- `GET /books/{id}` - Chi tiết sách
- `GET /books/{id}/edit` - Form sửa sách
- `POST /books/{id}/edit` - Cập nhật sách
- `POST /books/{id}/delete` - Xóa sách

### Tác giả

- `GET /authors` - Danh sách tác giả
- `GET /authors/create` - Form thêm tác giả
- `POST /authors/create` - Tạo tác giả mới
- `GET /authors/{id}` - Chi tiết tác giả
- `GET /authors/{id}/edit` - Form sửa tác giả
- `POST /authors/{id}/edit` - Cập nhật tác giả
- `POST /authors/{id}/delete` - Xóa tác giả

### Người dùng

- `GET /users` - Danh sách người dùng
- `GET /users/create` - Form thêm người dùng
- `POST /users/create` - Tạo người dùng mới
- `GET /users/{id}` - Chi tiết người dùng
- `GET /users/{id}/edit` - Form sửa người dùng
- `POST /users/{id}/edit` - Cập nhật người dùng
- `GET /users/{id}/deposit` - Form nạp tiền
- `POST /users/{id}/deposit` - Nạp tiền
- `POST /users/{id}/membership` - Mua membership
- `POST /users/{id}/delete` - Xóa người dùng

### Mượn/Trả sách

- `GET /borrows` - Danh sách giao dịch mượn
- `GET /borrows/create` - Form mượn sách
- `POST /borrows/create` - Mượn sách
- `POST /borrows/{id}/return` - Trả sách
- `POST /borrows/{id}/pay-fine` - Thanh toán phạt
- `GET /borrows/active` - Sách đang mượn
- `GET /borrows/overdue` - Sách quá hạn

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.2.0
- **Frontend**: Thymeleaf + Bootstrap 5
- **Database**: MySQL 8.0
- **Authentication**: Session-based login
- **Build Tool**: Maven
- **Java Version**: 17

## Tính năng nổi bật

1. **Authentication**: Hệ thống đăng nhập đơn giản với session-based
2. **Responsive**: Giao diện thân thiện trên mọi thiết bị
3. **Validation**: Kiểm tra dữ liệu đầu vào đầy đủ
4. **Transaction**: Quản lý giao dịch an toàn
5. **Search**: Tìm kiếm linh hoạt theo nhiều tiêu chí
6. **Statistics**: Thống kê và báo cáo trực quan

## Mở rộng trong tương lai

- [ ] API REST cho mobile app
- [ ] Báo cáo thống kê chi tiết
- [ ] Gửi email thông báo
- [ ] QR code cho sách
- [ ] Tích hợp thanh toán online
- [ ] Quản lý đa thư viện
>>>>>>> f06fc04 (chore: initial project setup)
