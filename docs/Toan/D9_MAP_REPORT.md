# Map D9 — Tháp OOP

## 1. Tên map
Map D9 — Tháp OOP.

## 2. Vai trò
D9 là Map 3 theo thứ tự câu chuyện, sau C2 và trước B1. Hiện tại điều hướng không bắt buộc theo thứ tự này: D9 có thể mở trực tiếp từ bản đồ thế giới ngay từ đầu.

## 3. Tóm tắt gameplay
- Thử thách leo tháp theo phong cách Jump King.
- 7 tầng.
- Checkpoint giữa các tầng.
- Cửa quiz bảo vệ tiến trình mỗi tầng.
- Một cánh cửa quiz cuối cùng hoàn thành thử thách.

## 4. Đường dẫn tài sản D9
- Map: `assets/Map/D9/d9_map.tmx`
- Tilesets: `assets/Map/D9/d9_tileset.tsx`, `assets/Map/D9/d9_tileset2.tsx`, `assets/Map/D9/d9_tileset3.tsx`, `assets/Map/D9/d9_tileset4.tsx`
- Tileset images: `assets/Map/D9/d9_tileset.png`, `assets/Map/D9/d9_tileset2.png`, `assets/Map/D9/d9_tileset3.png`, `assets/Map/D9/d9_tileset4.png`
- Lưu ý: TMX có một số tham chiếu TSX đường dẫn tuyệt đối Windows, nhưng runtime hiện chuyển về các file tileset D9 cục bộ và tránh vẽ tile placeholder cho các gid không giải quyết được.

## 4.1. Tileset 4 và cơ chế nhiều tileset
- `d9_tileset4.tsx` và `d9_tileset4.png` hiện được nhận diện và tải bình thường.
- Loader đọc tất cả thẻ `<tileset>` trong TMX, bất kể số lượng TSX.
- GID được phân giải bằng `firstgid` để tìm tileset phù hợp nhất.
- `gid=0` được bỏ qua hoàn toàn.
- Không có ô placeholder màu xám/xanh được vẽ cho tile trống hoặc tile không tìm thấy.

- Quiz panel image: `assets/Map/D9/d9_quiz_panel.png`
- Question bank: `assets/Data/questions/oop_questions.json`

## 5. Cấu trúc layer Tiled
- `Background`
- `Platforms`
- `Decor`
- `FloorDivider`
- `Checkpoints`
- `Objects`

## 6. Quy tắc va chạm
Chỉ layer `Platforms` tạo các hộp va chạm nền. Background, decor, floor divider, checkpoints và object layer không được dùng làm va chạm nền.

Các object `MapBoundary` từ layer `Objects` tạo các hộp va chạm biên riêng. TMX hiện có các object `map_bound_left`, `map_bound_right`, `map_bound_top`, và `map_bound_bottom`. Các biên fallback chỉ được tạo nếu một bên bị thiếu.

Phong cách di chuyển Jump King trong D9 dùng thả phím Space, không phải nhấn phím. Khi giữ Space, người chơi vào trạng thái `CHARGE` và `chargePower` tăng. Khi thả, code áp dụng `velocityY = -chargePower` và chuyển sang `JUMP`. Dấu âm của Y cần thiết vì toạ độ Swing tăng xuống dưới.

## 7. Logic object
- `PlayerSpawn`: vị trí bắt đầu ban đầu của người chơi.
- `Checkpoint`: mục tiêu lưu hoặc điểm dịch chuyển, phân biệt bằng `checkpointId`.
- `QuizDoor`: kích hoạt quiz bình thường của tầng.
- `FinalQuizDoor`: kích hoạt quiz cuối cùng.
- `FloorDivider`: metadata tầng và phân tách hình ảnh.
- `MapBoundary`: va chạm biên cứng của map.

`spawn_floor_1` là vùng spawn thật sự của tầng 1. Nếu có nhiều object `spawn_floor_1`, runtime sẽ cảnh báo và chọn object có vị trí snap nền phù hợp nhất với khu vực cửa gỗ khởi đầu thay vì tin theo thứ tự file.

## 8. Hệ thống quiz
Các bộ câu hỏi được tải từ `assets/Data/questions/oop_questions.json`:
- `oop_floor_1`
- `oop_floor_2`
- `oop_floor_3`
- `oop_floor_4`
- `oop_floor_5`
- `oop_floor_6`
- `oop_final`

Mỗi bộ hiện chứa 15 câu hỏi trắc nghiệm OOP bằng tiếng Việt với `id`, `topic`, `difficulty`, `question`, `options`, `answerIndex`, và `explanation`.

## 9. Cài đặt sprite người chơi
Player sprites are loaded from `assets/Vu/`.
- `STAND`: `character_stand_left (1).png`, `character_stand_right (1).png`
- `CHARGE`: `character_charge_left.png`, `character_charge_right.png`
- `JUMP`: `character_jump_left.png`, `character_jump_right.png`
- Fallback: `character_icon.png`, then a drawn rectangle if all images are missing.

## 10. Điều hướng
Tất cả map đều mở tự do từ đầu. D9 mở trực tiếp từ world map/menu qua `MAP_D9`. Đường click D9 ghi:
- `[Navigation] Opening D9`
- `[Navigation] Switching to panel: D9`
- `[D9] Loading map: assets/Map/D9/d9_map.tmx`

## 11. Các file kỹ thuật đã thay đổi
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

## 12. Cách chạy và kiểm tra D9
Nếu có Maven:

```bash
mvn exec:java
```

Phương án thay thế:

```bash
mkdir -p bin
javac -d bin $(find src/main/java -name "*.java")
java -cp bin com.hust.game.Main
```

Các bước kiểm tra:
1. Khởi động game.
2. Mở world map.
3. Nhấp vào vùng D9.
4. Xác nhận panel D9 mở lên.
5. Quan sát log terminal cho các thông báo điều hướng và tải map D9.
6. Nhấn `Esc` để trở về world map.

Runtime logs liên quan:
- `[D9Input] keyPressed SPACE`
- `[D9Input] start charge`
- `[D9Input] keyReleased SPACE`
- `[D9Input] release jump with power=...`
- `[D9Input] jump blocked reason=...`
- `[D9Spawn] PlayerSpawn candidates: ...`
- `[D9Spawn] selected spawn_floor_1 at ...`

## 13. Những hạn chế đã biết
- Gameplay D9 hoạt động nhưng vẫn còn giới hạn trong vòng thử thách tháp D9.
- TMX hiện có object `MapBoundary` rõ ràng cho cả bốn cạnh, nên các biên dự phòng chỉ dùng khi một bên thiếu trong lần sửa Tiled sau.
- Chưa có lưu/khôi phục D9 giữa các phiên.
- Nếu thiếu asset D9, game hiện màn hình dự phòng thay vì crash.
- Maven không có sẵn trong môi trường local này, nên đã kiểm tra biên dịch bằng `javac`.

## 14. Checklist cuối cùng
- [x] Báo cáo D9 đã chuyển ra khỏi thư mục asset-side cũ.
- [x] Đường dẫn map D9 được chuẩn hóa thành `assets/Map/D9/d9_map.tmx`.
- [x] Tileset D9 dùng tham chiếu TSX/PNG cục bộ.
- [x] Question bank D9 tồn tại ở `assets/Data/questions/oop_questions.json`.
- [x] Sprite người chơi load từ `assets/Vu/`.
- [x] D9 có thể mở từ world map mà không cần hoàn thành C2.
- [x] Va chạm platform chỉ đến từ layer `Platforms`.
- [x] Biên map được load từ object `MapBoundary` hoặc tạo dự phòng theo kích thước map.
- [x] `spawn_floor_1` được dùng như vùng spawn và snap vào platform gần nhất phía dưới.
- [x] Checkpoint, QuizDoor và FinalQuizDoor được parse từ layer `Objects`.
- [x] F3 bật/tắt debug draw D9.
- [x] Thiếu asset D9 không khiến panel crash.

## 15. Bổ sung MapBoundary và platform khởi đầu tầng 1

### Mục đích của MapBoundary
`MapBoundary` được dùng để tạo các biên va chạm cứng bao quanh map D9. Các object này ngăn người chơi đi ra ngoài vùng chơi: không vượt qua mép trái, mép phải, trần map, hoặc rơi xuống đáy map.

### Danh sách MapBoundary trong Tiled
Các object biên phải nằm trong layer `Objects`, với `Class = MapBoundary`:

- `map_bound_left`: biên trái của map.
- `map_bound_right`: biên phải của map.
- `map_bound_top`: biên trên của map.
- `map_bound_bottom`: biên dưới của map.

Mỗi object `MapBoundary` cần có các property:

- `side`: `left`, `right`, `top`, hoặc `bottom`.
- `solid`: `true` để đánh dấu đây là vùng va chạm cứng.

### Giải thích `map_bound_bottom`
`map_bound_bottom` là biên an toàn ở đáy map. Nó không phải sàn chơi chính của tầng 1, mà là vùng chặn cuối cùng để người chơi không rơi khỏi map nếu va chạm platform bị thiếu hoặc người chơi rơi xuống quá sâu. Object này nên đặt ngay dưới chiều cao pixel của map, có chiều rộng phủ hết map và chiều cao đủ lớn để bắt va chạm rơi xuống.

### Platform mới dưới `spawn_floor_1`
Platform khởi đầu tầng 1 là sàn chơi thật sự nơi người chơi bắt đầu đứng sau khi spawn. Platform này được thêm bên dưới vùng `spawn_floor_1` để hệ thống spawn snapping có thể đặt đáy hitbox của người chơi lên mặt sàn gần nhất, tránh trường hợp nhân vật xuất hiện giữa không trung rồi rơi xuyên xuống dưới.

Platform này bắt buộc phải là tile không rỗng trong layer `Platforms`, vì chỉ layer `Platforms` được dùng để sinh collision platform. Không đặt platform khởi đầu trong `Objects`, `Decor`, `Background`, `FloorDivider`, hoặc `Checkpoints` nếu muốn nó có va chạm nền.

### Khác nhau giữa platform tầng 1 và `map_bound_bottom`
Platform tầng 1 là sàn gameplay bình thường: người chơi đứng, nhảy, tiếp đất và bắt đầu thử thách tại đó. Collision của nó được sinh từ tile trong layer `Platforms`.

`map_bound_bottom` là biên bảo vệ của map: nó chỉ có nhiệm vụ chặn người chơi rơi ra ngoài map. Nó nằm trong layer `Objects`, có `Class = MapBoundary`, và không thay thế cho sàn tầng 1. Nếu chỉ có `map_bound_bottom` mà không có platform tầng 1 trong `Platforms`, người chơi có thể bị rơi xuống đáy map thay vì đứng đúng tại khu vực bắt đầu.

### Checklist cho Tiled
- [ ] Layer `Platforms` có tile sàn thật bên dưới `spawn_floor_1`.
- [ ] Sàn khởi đầu tầng 1 nằm trong layer `Platforms`, không nằm trong `Objects`.
- [ ] Layer `Objects` có đủ object `map_bound_left`, `map_bound_right`, `map_bound_top`, `map_bound_bottom`.
- [ ] Mỗi object boundary có `Class = MapBoundary`.
- [ ] Mỗi object boundary có property `side` đúng với vị trí của nó.
- [ ] Mỗi object boundary có property `solid = true`.
- [ ] `map_bound_bottom` nằm dưới đáy map và phủ toàn bộ chiều rộng khu vực chơi.
- [ ] `spawn_floor_1` vẫn là object spawn zone, không phải platform collision.

## 16. Cập nhật implementation D9 theo cấu hình tổng hợp

### Map loading và render
`D9MapLoader` đọc `assets/Map/D9/d9_map.tmx`, lấy `width`, `height`, `tilewidth`, `tileheight`, đọc các layer tile CSV và lưu các layer:

- `Background`
- `Platforms`
- `Decor`
- `FloorDivider`
- `Checkpoints`
