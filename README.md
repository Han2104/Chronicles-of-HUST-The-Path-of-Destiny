# Chronicles of HUST: Biên Niên Sử Bách Khoa
> **"Học tập chính là định mệnh!"**

![Java Version](https://img.shields.io/badge/Java-17%2B-orange)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

**Chronicles of HUST** là một trò chơi nhập vai (RPG) kết hợp giải đố và sinh tồn, được xây dựng trên nền tảng Java/OOP. Game tái hiện hành trình đầy thử thách của một sinh viên từ miền núi Sơn La chinh phục đỉnh cao tri thức tại Đại học Bách Khoa Hà Nội.

## Tính năng nổi bật

### 1. Hệ thống thế giới mở thu nhỏ
Khám phá 4 bản đồ đặc trưng từ **nương rẫy Sơn La** đến **đấu trường B1** huyền thoại.

### 2. Cơ chế phát triển nhân vật sâu sắc
Hệ thống **10 cấp độ** với các kỹ năng thụ động độc đáo như "Canh Tác Nhanh" hay "Ôn Thi Thần Tốc".

### 3. Thử thách trí tuệ
Vượt qua mê cung **D9** với hàng loạt câu hỏi trắc nghiệm và debug code Java thực tế.

### 4. Trận chiến Boss hoành tráng
Đối đầu với các **"Boss Giáo Sư"** dựa trên cơ chế đánh theo lượt (Turn-based) đầy chiến thuật.

### 5. Tương tác xã hội phong phú
Kết bạn, xây dựng mối quan hệ với các NPC và tham gia các hoạt động ngoại khóa để nhận buff đặc biệt.

## Công nghệ sử dụng

- **Ngôn ngữ:** Java 17
- **IDE:** IntelliJ IDEA (khuyến nghị)
- **Build Tool:** Maven
- **Game Engine:** Swing/AWT (Custom Framework)
- **OOP Principles:** Áp dụng triệt để 4 nguyên lý: Đóng gói (Encapsulation), Kế thừa (Inheritance), Đa hình (Polymorphism) và Trừu tượng (Abstraction)

## Hướng dẫn cài đặt & Chơi

### Điều kiện tiên quyết
- Máy tính đã cài đặt **Java 17** hoặc phiên bản mới hơn.

### Bước 1: Tải mã nguồn
```bash
git clone https://github.com/Han2104/Chronicles-of-HUST-The-Path-of-Destiny.git
cd Chronicles-of-HUST-The-Path-of-Destiny
```

### Bước 2: Biên dịch và Chạy game (Nhanh nhất)
Bạn có thể biên dịch toàn bộ và khởi chạy game chỉ với 2 lệnh đơn giản:

**Trên Linux/macOS:**
```bash
# Tạo thư mục chứa file thực thi và biên dịch
mkdir -p bin
javac -d bin $(find src -name "*.java")

# Chạy game
java -cp bin com.hust.game.Main
```

**Trên Windows (PowerShell):**
```powershell
# Tạo thư mục bin
if (!(Test-Path bin)) { New-Item -ItemType Directory bin }

# Biên dịch tất cả file java
$javaFiles = Get-ChildItem -Recurse src/*.java | Select-Object -ExpandProperty FullName
javac -d bin $javaFiles

# Chạy game
java -cp bin com.hust.game.Main
```

---

## Hướng dẫn chơi cơ bản

### Giao diện game
Trò chơi sử dụng giao diện đồ họa **Java Swing**. Bạn sẽ tương tác thông qua chuột (Click) và các phím điều hướng trên màn hình.

### Các khu vực chính
- **World Map:** Lựa chọn các địa điểm để di chuyển (Sơn La, C2, D9, B1).
- **Nông trại Sơn La:** Click vào các ô đất để Cuốc đất, Gieo hạt (Ngô/Lúa) và Thu hoạch.
- **Cửa hàng:** Mua thêm hạt giống bằng tiền (VNĐ) kiếm được từ việc bán nông sản.

### Lưu ý quan trọng
- Đảm bảo thư mục `assets/` nằm cùng cấp với lệnh chạy để game có thể tải hình ảnh bản đồ.
- Nếu gặp lỗi hiển thị trong IDE, hãy thực hiện **Invalidate Caches** hoặc **Clean Project**.

## Cấu trúc dự án hiện tại

```
.
├── assets/               # Hình ảnh bản đồ (World map, Sơn La)
├── bin/                  # File đã biên dịch (.class)
├── src/
│   └── main/java/
│       └── com/hust/game/
│           ├── Main.java     # Khởi chạy game
│           ├── core/         # Singleton GameManager điều phối game
│           ├── models/       # Thực thể (Player, Enemy) và Vật phẩm
│           ├── maps/         # Logic xử lý riêng cho từng bản đồ
│           └── ui/           # Giao diện người dùng (Panels, Windows)
└── README.md
```

## Đội ngũ phát triển

- **Han2104** - [Chronicles of HUST Team]

## Giấy phép

MIT License
