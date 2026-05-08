# Cấu trúc asset game

## Sơ đồ thư mục hiện tại

```text
assets/
  Data/
    questions/
      oop_questions.json
  Map/
    B1/
    D9/
      d9_map.tmx
      d9_tileset.tsx
      d9_tileset.png
      d9_tileset2.tsx
      d9_tileset2.png
      d9_tileset3.tsx
      d9_tileset3.png
      d9_quiz_panel.png
  UI/
    d9_quiz_panel.png
  Vu/
    character_*.png
  boss.png
  c2_map.png
  sonla_map.png
  world_map.png
```

## Đường dẫn chuẩn
- D9 map: `assets/Map/D9/d9_map.tmx`
- D9 quiz panel: `assets/Map/D9/d9_quiz_panel.png`
- D9 question bank: `assets/Data/questions/oop_questions.json`
- Vu/player sprites: `assets/Vu/`
- B1 map/assets: `assets/Map/B1/`
- UI assets: `assets/UI/`

## Những gì thuộc assets
Cây thư mục `assets/` nên chứa dữ liệu runtime của game: maps, tilesets, sprite, ảnh, texture UI và dữ liệu JSON được game load.

## Những gì thuộc docs
Báo cáo, ghi chú triển khai, track file và tài liệu thuộc về `docs/`, không nên để trong thư mục runtime asset.

## Vị trí báo cáo
Các báo cáo của Toan được lưu dưới:

```text
docs/Toan/
  D9_MAP_REPORT.md
  GAME_ASSET_STRUCTURE.md
  NAVIGATION_AND_UNLOCK_REPORT.md
```

Thư mục theo dõi asset cũ đã được xóa sau khi nội dung báo cáo hữu ích được chuyển và hợp nhất.

## Hành vi load asset
Runtime load trước tiên thử đường dẫn filesystem từ thư mục gốc dự án, sau đó thử classpath resource. Điều này hỗ trợ cả chạy từ thư mục gốc và cấu hình Maven khi `assets/` được copy vào `target/classes`.
