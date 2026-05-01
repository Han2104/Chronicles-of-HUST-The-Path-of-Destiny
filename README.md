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
- Đã cài đặt **Java 17** hoặc phiên bản mới hơn.
- Đã cài đặt **Maven**.

### Bước 1: Clone Repository
```bash
git clone https://github.com/B21DCPT102/Chronicles-of-HUST.git
cd Chronicles-of-HUST
```

### Bước 2: Build dự án
```bash
mvn clean package
```

### Bước 3: Chạy game
Sau khi build thành công, bạn có thể chạy game bằng lệnh:
```bash
mvn exec:java -Dexec.mainClass="Main"
```

Hoặc mở dự án trong **IntelliJ IDEA**, tìm đến file `Main.java` và nhấn **Run**.

### Roadmap (Lộ trình phát triển)
Thể hiện rằng bạn có tầm nhìn dài hạn cho dự án:
*   [x] **Phase 1:** Hoàn thiện Map 1 & Hệ thống chỉ số sinh tồn[cite: 1].
*   [ ] **Phase 2:** Triển khai hệ thống Check-in kỷ luật tại Map 2[cite: 1].
*   [ ] **Phase 3:** Tích hợp âm thanh và đồ họa nâng cao (LibGDX)[cite: 1].

## Hướng dẫn chơi cơ bản

### Giao diện game
Game sử dụng giao diện **Console (Text-based)** với các tùy chọn được đánh số để người chơi lựa chọn.

### Các phím tắt chính
- **W, A, S, D:** Di chuyển (ở các khu vực cần di chuyển)
- **1, 2, 3...:** Chọn hành động
- **Enter:** Xác nhận

### Lưu & Tải game
Game hỗ trợ lưu tiến trình tự động. Bạn có thể tiếp tục chơi tại điểm dừng bất cứ lúc nào.

## Cấu trúc dự án

```
src/main/java/
├── com/hust/game/
│   ├── main/             # Logic game chính
│   ├── entity/           # Định nghĩa các thực thể (Player, NPC, Enemy)
│   ├── items/            # Hệ thống vật phẩm
│   ├── skills/           # Kỹ năng và phép thuật
│   ├── world/            # Xử lý bản đồ và tương tác
│   └── ui/               # Giao diện người dùng và menu
└── resources/            # Tài nguyên game
```

## Đội ngũ phát triển

- **B21DCPT102** - [Vũ Tộc]

## Đóng góp

[Đang cập nhật...]

## Giấy phép

MIT License
