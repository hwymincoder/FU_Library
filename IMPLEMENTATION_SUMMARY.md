# Tóm tắt Implementation - Hệ thống phân quyền Manager & User

## 📋 Tổng quan
Dự án đã được cập nhật để phân chia rõ ràng thành 2 hệ thống:
- **Manager System** (Quản lý): Truy cập tại `/manager/*`
- **User System** (Người dùng): Truy cập tại `/user/*`

## 🔐 Hệ thống Authentication & Authorization

### 1. AuthInterceptor (Updated)
**File**: `src/main/java/com/example/demo/config/AuthInterceptor.java`

**Chức năng**:
- Kiểm tra đăng nhập cho tất cả requests
- Phân quyền theo role:
  - Manager chỉ có thể truy cập `/manager/*`
  - User chỉ có thể truy cập `/user/*`
- Auto-redirect về trang phù hợp khi truy cập `/` hoặc `/dashboard`

### 2. LoginController (Updated)
**File**: `src/main/java/com/example/demo/controller/LoginController.java`

**Thay đổi**:
- Manager login → redirect về `/manager/dashboard`
- User login → redirect về `/user/home`
- Kiểm tra user có bị ban không
- Tự động redirect về trang tương ứng nếu đã đăng nhập

## 👨‍💼 Manager System

### Controllers

#### 1. ManagerController (New)
**File**: `src/main/java/com/example/demo/controller/ManagerController.java`
**Route**: `/manager/dashboard`

**Features**:
- Dashboard tổng quan với thống kê:
  - Tổng số sách, sách có sẵn
  - Tổng người dùng, tác giả
  - Sách đang mượn, quá hạn
- Danh sách giao dịch mượn gần đây
- Links thao tác nhanh

#### 2. Updated Controllers
Tất cả controllers đã được update với prefix `/manager`:
- **BookController**: `/manager/books/*`
- **AuthorController**: `/manager/authors/*`
- **UserController**: `/manager/users/*`
- **BorrowController**: `/manager/borrows/*`

### Templates

#### Manager Layout
**File**: `src/main/resources/templates/manager/layout.html`

**Features**:
- Sidebar navigation với gradient design
- Thống kê và thông tin manager
- Menu điều hướng đầy đủ
- Alert messages đẹp mắt

#### Manager Dashboard
**File**: `src/main/resources/templates/manager/dashboard.html`

**Sections**:
1. Statistics Cards (6 cards)
   - Tổng số sách
   - Sách có sẵn
   - Tổng người dùng
   - Đang mượn
   - Sách quá hạn
   - Tổng tác giả

2. Quick Actions
   - Thêm sách mới
   - Thêm tác giả
   - Thêm người dùng
   - Mượn sách

3. Recent Borrows Table
   - 10 giao dịch gần nhất
   - Hiển thị đầy đủ thông tin

## 👤 User System

### Entity & Repository

#### 1. Cart Entity (New)
**File**: `src/main/java/com/example/demo/entity/Cart.java`

**Purpose**: Lưu trữ sách tạm thời trước khi xác nhận mượn

**Fields**:
- `id`: Primary key
- `user`: User đang chọn sách
- `book`: Sách được chọn
- `addedAt`: Thời gian thêm vào giỏ

#### 2. CartRepository (New)
**File**: `src/main/java/com/example/demo/repository/CartRepository.java`

**Methods**:
- `findByUserId()`: Lấy giỏ của user
- `findByUserIdAndBookId()`: Check sách đã có trong giỏ chưa
- `deleteByUserId()`: Xóa toàn bộ giỏ

#### 3. CartService (New)
**File**: `src/main/java/com/example/demo/service/CartService.java`

**Features**:
- `addToCart()`: Thêm sách vào giỏ
- `removeFromCart()`: Xóa sách khỏi giỏ
- `clearUserCart()`: Xóa toàn bộ giỏ
- `getCartCount()`: Đếm số sách trong giỏ

### Controllers

#### UserPageController (New)
**File**: `src/main/java/com/example/demo/controller/UserPageController.java`

**Routes**:

1. **`GET /user/home`** - Trang chủ User
   - Hiển thị thông tin user (tên, số dư, membership)
   - Tìm kiếm sách
   - Danh sách sách có sẵn
   - Thêm sách vào giỏ

2. **`GET /user/profile`** - Trang hồ sơ
   - Thông tin cá nhân đầy đủ
   - Danh sách sách đang mượn
   - Lịch sử mượn sách
   - Hiển thị tiền phạt (nếu có)

3. **`POST /user/cart/add`** - Thêm sách vào giỏ
   - Validate sách chưa có trong giỏ
   - Flash message thành công/lỗi

4. **`GET /user/cart`** - Xem giỏ sách
   - Danh sách sách trong giỏ
   - Thông tin membership
   - Xác nhận mượn

5. **`POST /user/cart/remove`** - Xóa sách khỏi giỏ

6. **`POST /user/cart/confirm`** - Xác nhận mượn sách
   - Validate membership
   - Check số sách tối đa
   - Tạo borrow records
   - Xóa giỏ sau khi thành công
   - Due date: 14 ngày

### Templates

#### User Layout
**File**: `src/main/resources/templates/user/layout.html`

**Design**:
- Modern gradient background (purple theme)
- Fixed navbar với logo
- Cart badge hiển thị số lượng sách
- Responsive design
- Beautiful alert messages

**Navigation**:
- Trang chủ
- Hồ sơ
- Giỏ sách (với badge)
- Tên user
- Đăng xuất

#### 1. User Home
**File**: `src/main/resources/templates/user/home.html`

**Sections**:

1. **User Info Box** (Gradient banner)
   - Tên user
   - Số dư tài khoản
   - Membership type & expiry
   - Quick link to cart

2. **Search Section**
   - Large search bar
   - Search by book title

3. **Books Grid**
   - Responsive cards (3 columns)
   - Book information:
     - Title, ISBN, Description
     - Available copies
     - Add to cart button
   - Disabled button nếu hết sách

#### 2. User Profile
**File**: `src/main/resources/templates/user/profile.html`

**Sections**:

1. **Personal Information Cards**
   - Họ tên
   - Email
   - Số điện thoại
   - Số dư (với format VNĐ)
   - Membership card (gradient, premium look)

2. **Currently Borrowed Books**
   - Table hiển thị sách đang mượn
   - Ngày mượn, ngày hẹn trả
   - Status badge (Đang mượn/Quá hạn)
   - Tiền phạt (nếu có)

3. **Borrow History**
   - Full history table
   - Tất cả giao dịch mượn/trả
   - Ngày trả thực tế
   - Final status

#### 3. User Cart
**File**: `src/main/resources/templates/user/cart.html`

**Sections**:

1. **Page Header**
   - Title với icon
   - "Continue shopping" button

2. **Empty State**
   - Large cart icon
   - Friendly message
   - Call-to-action button

3. **Account Summary Cards**
   - Số dư
   - Membership type
   - Số sách tối đa
   - Warning nếu không có membership

4. **Cart Items Grid**
   - Responsive 2-column layout
   - Book cards với:
     - Title, ISBN, Description
     - Added timestamp
     - Remove button

5. **Confirmation Section**
   - Important info alert:
     - Thời hạn mượn: 14 ngày
     - Phí phạt: 30,000 VNĐ/ngày
     - Số sách đang chọn
   - Action buttons:
     - Quay lại
     - Xác nhận mượn (disabled nếu không có membership)

## 🎨 Design Highlights

### Manager Theme
- Professional dark sidebar with gradient
- Clean, corporate look
- Bootstrap cards with colored borders
- Font Awesome icons throughout

### User Theme
- Modern gradient backgrounds (purple/blue)
- Card-based layout with shadows
- Gradient buttons with hover effects
- Friendly, consumer-focused UI
- Badge notifications
- Beautiful info boxes

## 🔄 Updated Files

### Java Files
1. `config/AuthInterceptor.java` - ✅ Updated
2. `controller/LoginController.java` - ✅ Updated
3. `controller/ManagerController.java` - 🆕 New
4. `controller/UserPageController.java` - 🆕 New
5. `controller/BookController.java` - ✅ Updated
6. `controller/AuthorController.java` - ✅ Updated
7. `controller/UserController.java` - ✅ Updated (renamed paths)
8. `controller/BorrowController.java` - ✅ Updated
9. `entity/Cart.java` - 🆕 New
10. `repository/CartRepository.java` - 🆕 New
11. `service/CartService.java` - 🆕 New

### HTML Templates
1. `manager/layout.html` - 🆕 New
2. `manager/dashboard.html` - 🆕 New
3. `user/layout.html` - 🆕 New
4. `user/home.html` - 🆕 New
5. `user/profile.html` - 🆕 New
6. `user/cart.html` - 🆕 New
7. `layout.html` - ✅ Updated
8. `index.html` - ✅ Updated

## 🚀 How to Use

### Manager Login
1. Login với manager account
2. Auto redirect về `/manager/dashboard`
3. Access full CRUD operations
4. Manage users, books, authors, borrows

### User Login
1. Login với user account
2. Auto redirect về `/user/home`
3. Browse và search books
4. Add books to cart
5. View cart và confirm borrow
6. Check profile và borrow history

### Flow mượn sách (User)
1. **Browse** → `/user/home`
2. **Search** → Tìm sách theo tên
3. **Add to cart** → Click "Thêm vào giỏ"
4. **View cart** → `/user/cart`
5. **Confirm** → Xác nhận mượn sách
6. **Check profile** → `/user/profile` để xem sách đã mượn

## 📊 Business Logic

### Borrow Rules
- User cần có membership hợp lệ
- Số sách tối đa theo membership type
- Thời hạn mượn: 14 ngày
- Phí phạt trễ: 30,000 VNĐ/ngày/cuốn

### Cart System
- Mỗi user có 1 cart riêng
- Cart lưu tạm thời
- Xóa sau khi confirm thành công
- Validate trùng lặp sách

### Authorization
- Manager: Full access to management features
- User: Browse, borrow, view own data only
- Auto-redirect nếu truy cập sai role

## ✅ Testing Checklist

### Manager Features
- [ ] Login as manager
- [ ] View dashboard statistics
- [ ] Manage books (CRUD)
- [ ] Manage authors (CRUD)
- [ ] Manage users (CRUD)
- [ ] Manage borrows
- [ ] View overdue books

### User Features
- [ ] Login as user
- [ ] View home page
- [ ] Search books
- [ ] Add book to cart
- [ ] View cart
- [ ] Remove from cart
- [ ] Confirm borrow (with membership)
- [ ] View profile
- [ ] View active borrows
- [ ] View borrow history

### Authorization
- [ ] Manager cannot access `/user/*`
- [ ] User cannot access `/manager/*`
- [ ] Logout works correctly
- [ ] Auto-redirect works

## 🎯 Achievements

✅ **Phân quyền rõ ràng** - Manager và User hoàn toàn tách biệt
✅ **UI/UX hiện đại** - Beautiful, responsive design
✅ **Cart system** - Smooth borrowing flow
✅ **Validation đầy đủ** - Membership, limits, duplicates
✅ **Responsive** - Works on all screen sizes
✅ **Clean code** - Well-organized, maintainable

## 📝 Notes

- Database sẽ tự động tạo bảng `carts` khi chạy application
- Default data được load từ `DataInitializer.java`
- Tất cả templates đều sử dụng Thymeleaf
- Bootstrap 5.1.3 và Font Awesome 6.0.0
- Gradient colors có thể customize trong CSS

---

**Hoàn thành**: Hệ thống đã sẵn sàng để test và deploy! 🎉


