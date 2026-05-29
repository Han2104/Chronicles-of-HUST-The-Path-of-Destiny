# Báo cáo điều hướng và mở khóa

## Vấn đề trước đây
D9 đã được đăng ký trong UI, nhưng việc mở nó có thể bị lỗi runtime. Dự án cũng còn các kiểm tra tiến trình cũ trong logic world-map mô tả C2, D9 và B1 bị khoá cho đến khi các map trước đó, điểm finance, điểm discipline hoặc tiến trình tầng D9 hoàn thành.

## Nguyên nhân gốc
- `WorldMapPanel` đã gọi `window.showPanel("MAP_D9")`, và `GameWindow` đã đăng ký `D9Panel` dưới tên `MAP_D9`.
- Render panel D9 có thể bị lỗi vì đường dẫn sprite đứng được hardcode thành `character_stand_left.png` và `character_stand_right.png`, trong khi file thực tế là `character_stand_left (1).png` và `character_stand_right (1).png`.
- Load asset D9 dùng đường dẫn filesystem trực tiếp `File`, nên chạy qua classpath có thể thất bại im lặng hoặc giảm chất lượng.
- Lớp `WorldMap` trên console vẫn áp dụng các cổng tiến trình cũ cho C2, D9 và B1.

## Cách hạn chế bị loại bỏ hoặc bỏ qua
- `WorldMap.displayStatus()` hiện in tất cả map như mở.
- `WorldMap.selectLocation(...)` giờ chuyển đến SonLa, C2, D9 và B1 mà không kiểm tra completion flags, finance, discipline score hoặc tầng D9.
- Các trường tiến trình người chơi vẫn tồn tại trong `Player` để dùng cho nội dung tương lai, nhưng không còn chặn điều hướng.
- Các nút map UI vẫn hiển thị, enabled, có thể click và không hiện hộp thoại khoá.

## Khả năng truy cập hiện tại
Tất cả map có thể truy cập từ đầu:
- SonLa
- C2
- D9
- B1

## Các file đã chỉnh sửa
- `src/main/java/com/hust/game/ui/GameWindow.java`
- `src/main/java/com/hust/game/ui/panels/WorldMapPanel.java`
- `src/main/java/com/hust/game/ui/panels/D9Panel.java`
- `src/main/java/com/hust/game/ui/panels/C2Panel.java`
- `src/main/java/com/hust/game/ui/panels/FarmingPanel.java`
- `src/main/java/com/hust/game/ui/panels/CombatPanel.java`
- `src/main/java/com/hust/game/maps/worldmap/WorldMap.java`
- `src/main/java/com/hust/game/maps/d9/D9MapLoader.java`
- `src/main/java/com/hust/game/maps/d9/D9Map.java`
- `src/main/java/com/hust/game/maps/d9/D9Object.java`
- `src/main/java/com/hust/game/maps/d9/D9QuestionBank.java`
- `src/main/java/com/hust/game/util/AssetLoader.java`

## Các bước kiểm tra
1. Khởi động game.
2. Từ world map, click SonLa và xác nhận nó mở.
3. Quay trở lại world map.
4. Click C2 và xác nhận nó mở.
5. Quay lại world map.
6. Click D9 và xác nhận panel D9 mở.
7. Xác nhận log bao gồm `[Navigation] Opening D9`, `[Navigation] Switching to panel: D9`, và `[D9] Loading map: assets/Map/D9/d9_map.tmx`.
8. Quay lại world map bằng `Esc`.
9. Click B1 và xác nhận nó mở.

## Kết quả
Điều hướng không còn phụ thuộc vào yêu cầu tiến trình. D9 có màn hình dự phòng nếu asset runtime của nó không thể load, nên đường click khả dụng và có thể kiểm tra thay vì lỗi im lặng.
