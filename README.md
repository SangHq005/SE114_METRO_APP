# 🚇 Metro Ticket App - Android

**Metro Ticket App** là ứng dụng đặt vé tàu Metro hiện đại dành cho người dùng Android. Ứng dụng tích hợp đầy đủ tính năng như đăng ký/đăng nhập, mua và quản lý vé, tìm đường, cập nhật tin tức và tham gia diễn đàn cộng đồng.

---

## 📱 Giới thiệu ứng dụng

Metro Ticket App hỗ trợ hành khách:

- ✅ Đăng ký / đăng nhập tài khoản
- 🚉 Tìm tuyến đường & mua vé (lượt/tháng)
- 📲 Quét QR Code để qua cổng
- 📰 Cập nhật tin tức Metro
- 💬 Tham gia diễn đàn cộng đồng

---

## 📚 Thông tin đồ án

- **Môn học**: Nhập môn Ứng dụng Di động - UIT  
- **Giáo viên hướng dẫn**: ThS. Nguyễn Tấn Toàn

### 👥 Nhóm sinh viên

| Họ tên               | MSSV       |
|----------------------|------------|
| Phạm Hà Anh Thư      | 23521544   |
| Huỳnh Quốc Sang      | 23521340   |
| Nguyễn Minh Thiện    | 23521484   |
| Nguyễn Lê Duy        | 23520378   |

---

## 📋 Mục lục
 
1. [Môi trường phát triển](#-moi-truong-phat-trien)
2. [Cài đặt môi trường](#-cài-đặt-môi-trường)
3. [Cách chạy ứng dụng](#-cách-chạy-ứng-dụng)
4. [Tài khoản test](#-tài-khoản-test)
5. [Các chức năng chính](#-các-chức-năng-chính)
6. [Bảo mật & hiệu suất](#-bảo-mật--hiệu-suất)
7. [Thành viên thực hiện](#-thành-viên-thực-hiện)
8. [Liên hệ](#-liên-hệ)
9. [Giấy phép](#-giấy-phép)

---

## ⚙️ Môi trường phát triển

| Thành phần            | Phiên bản                           |
|-----------------------|-------------------------------------|
| Ngôn ngữ              | Java, XML                          |
| IDE                   | Android Studio Flamingo 2022.2.1+  |
| Kiến trúc             | MVVM                               |
| Backend               | Firebase (Auth, Firestore, Storage, Cloudinary)|
| Thiết kế giao diện    | Figma                              |
| API tích hợp          | MapBox, VNPAY                 |
| Android SDK           | minSdk: 24 → targetSdk: 33         |
| Gradle                | 7.4.2                              |
| Firebase BOM          | 32.2.2                             |

---

## 🚀 Cài đặt môi trường

### 1. Clone mã nguồn
```bash
git clone (https://github.com/SangHq005/SE114_METRO_APP.git)
cd SE114_METRO_APP
```

### 2. Cấu hình Firebase
- Tạo Firebase project tại [Firebase Console](https://console.firebase.google.com/)
- Thêm ứng dụng Android:
  - Package name: `com.uit.metroticket`
  - App nickname: Metro Ticket App
- Tải file `google-services.json` và đặt vào thư mục `app/`
---
### 🔐 Lấy SHA-1 để cấu hình Firebase

Để sử dụng xác thực Google Sign-In hoặc Dynamic Links, bạn cần cấu hình SHA-1 (và SHA-256) cho ứng dụng Android trên Firebase.

✅ Cách 1: Qua Android Studio

1. Mở Android Studio

2. Mở tab Gradle → :app > Tasks > android

3. Nhấn đúp vào signingReport

📋 Kết quả sẽ hiển thị trong tab "Run":
```
Variant: debug
SHA1: A1:B2:C3:...
SHA-256: ...
```
Copy SHA-1 và thêm vào Firebase: Project Settings > Android app > Add Fingerprint
### 3. Cấu hình Gradle 🛠️ 
Trong thư mục gốc của dự án, tạo file gradle.properties với nội dung sau:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.enableJetifier=true
android.useAndroidX=true
android.nonTransitiveRClass=true
#Cấu hình Java JDK trong máy bạn (Ví dụ: jdk-24) hoặc có thể ẩn dòng này đi
org.gradle.java.home=C\:\\Program Files\\Java\\jdk-24
# Cấu hình API Key
MAPBOX_DOWNLOADS_TOKEN=sk.eyJ1IjoidGhpZW5uZ3V5ZW4yNTA0IiwiYSI6ImNtYWRsZ2RzdTA1OGcybnM4bnZkOXo2emsifQ.XJU0gRz1_tU_lc6ZsTAY7g
# Cấu hình cloudinary
cloudinaryCloudName=dwa3wh9yb
cloudinaryApiKey=219257552732366
cloudinaryApiSecret=C1pLGgyPhmcu9wVn8mR61ToD2ow
```
### 4. Cài đặt phụ thuộc

- Mở dự án bằng Android Studio → chờ Gradle sync hoàn tất
---
## 🛠️ Cách chạy ứng dụng
### 👉 Chạy trên máy ảo (Emulator)
1. Vào Android Studio → Tools → Device Manager
2. Tạo thiết bị ảo (gợi ý: Pixel 7 Pro, API 30+)
3. Nhấn **Run ▶️** hoặc dùng tổ hợp `Shift + F10`

### 👉 Chạy trên thiết bị thật
1. Kết nối điện thoại qua USB
2. Bật Developer mode:
   - Vào Settings → About phone → Nhấn 7 lần vào “Build number”
   - Vào Developer Options → Bật “USB debugging”
3. Chọn thiết bị trong Android Studio → Bấm Run

---

## ✅ Hướng dẫn phân quyền admin/User

Firebase không có hệ thống phân quyền người dùng mặc định, bạn cần tự phân loại bằng cách lưu thông tin quyền vào Firestore.

✅ Bước 1: Tạo người dùng trên Firebase Authentication

- Vào Firebase Console → Authentication → Users → Add user

- Nhập email + mật khẩu cho người dùng

✅ Bước 2: Tạo field phân quyền trong Firestore

1. Vào Firestore Database

2. Vào collection: Account

3. Tìm document trùng ID với UID của người dùng

4. Sửa field:
```
role: "admin"  // hoặc "user"
```
---
## ✅ Tài khoản test VN Pay

| Thành phần            | Phiên bản           |
|-----------------------|---------------------|
| Ngân hàng             | 	NCB                |
| Số thẻ                | 9704198526191432198 |
| Tên chủ thẻ           | NGUYEN VAN A        |
| Ngày phát hành	       | 07/15               |
| Mật khẩu OTP          | 123456	             |

## ✨ Các chức năng chính

### 👤 Đăng nhập / Đăng ký
- Email/password
- Google Sign-In

### 🎟️ Mua vé
- Vé lượt (1 chiều), vé tháng (30 ngày)
- Thanh toán qua VNPAY
- Nhận QR Code ngay sau thanh toán

### 📂 Quản lý vé
- Danh sách vé đang sử dụng
- Lịch sử vé hết hạn
- Chi tiết vé (QR, thời hạn)

### 🗺️ Tìm đường
- Tìm trạm gần nhất
- Chỉ đường qua Google Maps

### 📰 Tin tức & Diễn đàn
- Xem bài viết mới nhất
- Đăng bài chia sẻ và bình luận

---

## 🔒 Bảo mật & hiệu suất

| Tính năng              | Mô tả                                      |
|------------------------|---------------------------------------------|
| Firebase Security Rules| Phân quyền truy cập dữ liệu                |
| Mã hoá dữ liệu         | AES-256 với thông tin nhạy cảm             |
| Xác thực 2 lớp         | Firebase Auth + Google Sign-In             |
| Tối ưu hình ảnh        | Glide + Resize trên Firebase Storage       |
| Caching                | Dữ liệu tĩnh lưu bằng Room Database        |
| Tách module            | Phân tách Client / Admin rõ ràng           |

---

## 🧑‍💻 Thành viên thực hiện

| Họ tên               | MSSV       | Vai trò            | Công việc chính                          |
|----------------------|------------|---------------------|-------------------------------------------|
| Phạm Hà Anh Thư      | 23521544   | Team Lead, UI/UX    | Thiết kế hệ thống, phát triển Admin      |
| Huỳnh Quốc Sang      | 23521340   | UI/UX Designer       | Thiết kế Figma, giao diện người dùng     |
| Nguyễn Minh Thiện    | 23521484   | Backend Developer    | Firebase, Tích hợp VNPAY                  |
| Nguyễn Lê Duy        | 23520378   | Backend Developer    | Google Maps API, Thiết kế database        |

---

## 📬 Liên hệ

📧 Email: 23521340@gm.uit.edu.vn  

---

## 📄 Giấy phép

```
Copyright 2025 Metro Ticket App - UIT

Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)

Dự án thuộc học phần tại UIT. Không sử dụng cho mục đích thương mại khi chưa được cấp phép.
```
