
# 🚇 Metro Ticket App - Android

Ứng dụng di động Android hỗ trợ **mua vé**, **quét vé**, **theo dõi thông tin tuyến metro** và **diễn đàn cộng đồng** dành cho người dùng tàu điện Metro tại TP. Hồ Chí Minh.

## 📱 Giới thiệu
Metro Ticket App giúp hành khách:
- Đăng ký / đăng nhập tài khoản.
- Tìm tuyến đường & mua vé (lượt, tháng).
- Quét QR Code để qua cổng.
- Cập nhật tin tức và tương tác với cộng đồng người đi metro.

> Đồ án môn: *Nhập môn Ứng dụng Di động* – UIT  
> GVHD: *ThS. Nguyễn Tấn Toàn*  
> Nhóm sinh viên: Phạm Hà Anh Thư, Huỳnh Quốc Sang, Nguyễn Minh Thiện, Nguyễn Lê Duy

## ⚙️ Môi trường phát triển

- **Ngôn ngữ:** Java, XML
- **IDE:** Android Studio (Arctic Fox trở lên)
- **Kiến trúc:** MVVM
- **Backend:** Firebase (Authentication, Firestore, Storage)
- **Công cụ UI/UX:** Figma
- **API tích hợp:** Google Maps, VNPAY

## 🚀 Cài đặt môi trường

### 1. Clone source code

```bash
git clone https://github.com/<your_team>/metro-ticket-app.git
cd metro-ticket-app
```

### 2. Cấu hình Firebase

> Tạo project Firebase tại [https://console.firebase.google.com](https://console.firebase.google.com)

- Tải tệp `google-services.json` về.
- Đặt file vào `app/google-services.json`.

Cấu hình các dịch vụ:

- **Authentication**: bật Email/Password và Google
- **Firestore Database**: bật chế độ test
- **Storage**: bật quyền đọc/ghi

### 3. Cấu hình API Keys

Trong `local.properties`, thêm dòng:

```
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

Đồng thời, cập nhật `AndroidManifest.xml` với key này.

### 4. Cài đặt SDK & thư viện

Mở bằng Android Studio và đảm bảo:

- SDK ≥ 30 (API Level 30+)
- Gradle sync không lỗi
- Internet/WiFi ổn định (để sync Firebase dependencies)

## 🛠️ Cách chạy ứng dụng

1. **Mở Android Studio** → File → Open → Chọn thư mục project
2. **Kết nối thiết bị Android hoặc Emulator**
3. Bấm **Run** ▶️ để khởi chạy ứng dụng

Ứng dụng bao gồm:

- `app-client`: chức năng cho người dùng
- `app-admin`: giao diện quản trị viên (đăng bài, kiểm duyệt, soát vé)

## ✅ Tài khoản test

| Vai trò | Email | Mật khẩu |
|--------|-------|----------|
| Admin | admin@metro.com | 123456 |
| Người dùng | user@metro.com | 123456 |

## ✨ Các chức năng chính

### 📱 Khách hàng (User)
- Đăng nhập / Đăng ký
- Tìm kiếm lộ trình
- Mua vé online (VNPAY)
- Nhận vé QR Code
- Xem tin tức metro
- Đăng bài, bình luận trên diễn đàn

### 🛡️ Quản trị viên (Admin)
- Quản lý trạm, tuyến, giá vé
- Kiểm duyệt bài viết
- Quản lý người dùng
- Quản lý giao dịch
- Soát vé bằng QR

## 🔒 Bảo mật & hiệu suất

- Firebase Rules bảo vệ truy cập database
- Quản lý phân quyền giữa Admin / User
- Giao diện chuẩn Material Design, dễ dùng

## 📸 Demo & Tài liệu

- 🎥 Demo video: [Link Google Drive / YouTube]
- 📄 File báo cáo: `SE114_BaoCao.docx` (đính kèm trong repository)

## 🧑‍💻 Thành viên thực hiện

| Tên | MSSV | Vai trò |
|-----|------|---------|
| Phạm Hà Anh Thư | 23521544 | UI/UX, Admin App |
| Huỳnh Quốc Sang | 23521340 | UI/UX Figma,Client App |
| Nguyễn Minh Thiện | 23521484 | Backend, VNPAY |
| Nguyễn Lê Duy | 23520378 | Backend, Google Map |

## 📬 Liên hệ

Mọi góp ý hoặc liên hệ vui lòng gửi qua email: **metroapp.groupUIT@gmail.com**

## 📄 Giấy phép

Dự án thuộc đồ án học phần tại UIT. Không sử dụng vào mục đích thương mại khi chưa được cấp phép.

