# Hướng dẫn chơi D9

## 1. Mục tiêu chung
D9 là thử thách leo tháp theo phong cách Jump King. Người chơi cần di chuyển qua 7 tầng, dùng checkpoint và giải quiz để tiến lên đỉnh.

## 2. Cách chạy game
1. Mở terminal tại thư mục gốc dự án.
2. Chạy các lệnh:

```bash
mkdir -p bin
javac -d bin $(find src/main/java -name "*.java")
java -cp bin com.hust.game.Main
```

3. Nếu có Maven, có thể dùng:

```bash
mvn exec:java
```

## 3. Mở D9
- Từ menu world map, chọn vùng D9.
- D9 mở trực tiếp mà không cần hoàn thành C2 hoặc B1.

## 4. Điều khiển cơ bản
- `Phím mũi tên trái/phải`: di chuyển trái/phải.
- `Space`: giữ để tích tụ `CHARGE`, thả để nhảy.
- `Esc`: trở về world map.

## 5. Cách chơi D9
- Bước đầu tiên là nhảy lên các nền tile trong layer `Platforms`.
- Người chơi chỉ va chạm với nền từ layer `Platforms`; các layer khác chỉ để trang trí hoặc tạo khu vực logic.
- Các object `QuizDoor` và `FinalQuizDoor` nằm trong layer `Objects` sẽ kích hoạt quiz khi bạn chạm vào.
- `Checkpoint` lưu vị trí và cho phép dịch chuyển nếu người chơi thất bại.

## 6. Hệ thống quiz
- Mỗi tầng D9 có quiz tương ứng.
- Nếu trả lời đúng đủ số câu yêu cầu, người chơi được dịch chuyển tới checkpoint tiếp theo hoặc thắng cuộc ở cửa quiz cuối.
- Nếu trả lời không đủ, người chơi sẽ bị phạt về checkpoint hoặc sàn an toàn trước đó.

## 7. Lưu ý về assets và lỗi
- D9 sử dụng tilemap `assets/Map/D9/d9_map.tmx` và nhiều TSX trong cùng thư mục.
- Nếu file ảnh tileset thiếu, game sẽ không vẽ placeholder màu xám/xanh cho tile không tìm thấy.
- `gid=0` được coi là ô trống và không được vẽ.

## 8. Thử nghiệm
- Khởi động game và mở D9.
- Kiểm tra xem đồ họa các tầng có hiển thị đúng.
- Nhấn Space giữ và thả, quan sát nhân vật nhảy.
- Chạm quiz door, hoàn thành một quiz và kiểm tra tiến trình lên tầng.

## 9. Ghi chú kỹ thuật
- D9 dùng `assets/Data/questions/oop_questions.json` để tải câu hỏi quiz.
- Sprite người chơi được load từ `assets/Vu/`.
- Nếu cần sửa D9, tập trung vào `src/main/java/com/hust/game/maps/d9/D9MapLoader.java` và `src/main/java/com/hust/game/ui/panels/D9Panel.java`.
