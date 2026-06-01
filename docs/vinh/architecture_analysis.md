# Phân Tích Kiến Trúc: Chronicles of HUST

## 1. Toàn Bộ Danh Sách File

```
com.hust.game
├── Main.java                          ← Entry point
│
├── core/
│   └── GameManager.java               ← Singleton điều phối game state
│
├── ui/
│   ├── GameWindow.java                ← JFrame chính, quản lý CardLayout
│   └── panels/
│       ├── StatsPanel.java            ← HUD cố định (NORTH)
│       ├── WorldMapPanel.java         ← Panel: Bản đồ thế giới
│       ├── C2Panel.java               ← Panel: Ký túc xá C2
│       ├── FarmingPanel.java          ← Panel: Sơn La (farming)
│       ├── D9Panel.java               ← Panel: D9 (platformer, 821 lines)
│       ├── ArenaPanel.java            ← Panel: B1 (boss selection)
│       ├── CombatPanel.java           ← Panel: Màn hình chiến đấu
│       ├── LazyEncounterDialog.java   ← Dialog: NPC lười
│       └── ShopDialog.java            ← Dialog: Cửa hàng
│
├── maps/
│   ├── b1/B1Map.java                  ← STUB (chỉ có 1 method trống)
│   ├── c2/C2Map.java                  ← STUB (chỉ có 1 method trống)
│   ├── sonla/SonLaMap.java            ← Logic farming đầy đủ
│   ├── worldmap/WorldMap.java         ← Logic điều hướng
│   └── d9/
│       ├── D9Map.java                 ← Data model bản đồ TMX
│       ├── D9MapLoader.java           ← Parser TMX (33KB)
│       ├── D9Player.java              ← Physics player D9
│       ├── D9CollisionManager.java    ← Collision detection
│       ├── D9CheckpointManager.java   ← Checkpoint system
│       ├── D9QuestionBank.java        ← Câu hỏi OOP (JSON)
│       ├── D9QuizManager.java         ← Quiz logic
│       ├── D9Object.java              ← TMX object wrapper
│       └── D9Settings.java            ← Cấu hình D9
│
├── models/
│   ├── entities/
│   │   ├── Player.java                ← Nhân vật chính (stats, inventory)
│   │   ├── Enemy.java
│   │   ├── BossTung.java
│   │   └── LazyNPC.java               ← Enum NPC lười
│   ├── combat/
│   │   └── Boss.java                  ← Boss data + factory methods
│   ├── d9/
│   │   └── D9Maze.java
│   ├── farming/
│   │   └── FarmPlot.java              ← Ô đất trồng trọt
│   └── items/
│       └── Seed.java                  ← Hạt giống
│
└── util/
    └── AssetLoader.java               ← Load ảnh/file từ filesystem hoặc classpath
```

---

## 2. Sơ Đồ Dependency

```
Main
 └─creates──► GameWindow
               ├─creates──► StatsPanel
               │             └─reads───► GameManager (singleton)
               │                          └─owns────► Player
               │
               ├─creates──► WorldMapPanel ──calls──► GameManager.switchMap()
               │                           └─calls──► window.showPanel()
               │
               ├─creates──► FarmingPanel   (→ SonLaMap internally)
               ├─creates──► C2Panel ───────calls──► GameManager.handleMap2Actions()
               │                           └─opens──► ShopDialog
               │
               ├─creates──► D9Panel ────────────────────────────────────────────┐
               │             ├─uses──► D9MapLoader                              │
               │             ├─uses──► D9Map ←── D9MapLoader                   │
               │             ├─uses──► D9Player                                 │
               │             ├─uses──► D9CollisionManager                       │
               │             ├─uses──► D9CheckpointManager                      │
               │             ├─uses──► D9QuestionBank                           │
               │             └─uses──► D9QuizManager                            │
               │                                                                 │
               ├─creates──► ArenaPanel ──calls──► Boss.createBossXxx()          │
               │              └─calls──► window.getCombatPanel().startCombat()  │
               │                                                                 │
               └─creates──► CombatPanel ◄────────────────────────────────────────┘
                              ├─reads──► GameManager.getPlayer()
                              └─calls──► window.showPanel()

GameManager (Singleton)
 ├─owns──────► Player
 ├─holds─────► StatsPanel (ref để gọi updateStats())
 ├─holds─────► GameWindow  (ref để gọi showPanel())
 ├─runs──────► energyRegenTimer (javax.swing.Timer, 30s)
 └─calls─────► triggerLazyEncounter() → LazyEncounterDialog

Player
 └─calls──► GameManager.handlePlayerFaint() khi energy = 0
             (circular reference: Player → GameManager)

SonLaMap
 └─reads──► GameManager.getInstance().getPlayer()

WorldMap (model, ít dùng)
 └─calls──► GameManager.switchMap()
```

---

## 3. CardLayout Flow

### Cơ chế hoạt động
- **GameWindow** khởi tạo một `CardLayout` trên `mainContainer` (JPanel).
- Mỗi panel được đăng ký bằng `registerPanel(name, panel)`.
- Chuyển màn bằng `window.showPanel(name)` → gọi `cardLayout.show(mainContainer, name)`.

### Bảng đăng ký Panel

| Tên Card Key    | Class                    | MapID | Ghi chú |
|-----------------|--------------------------|-------|---------|
| `"WORLD_MAP"`   | `WorldMapPanel`          | 0     | Màn hình chính |
| `"MAP_SONLA"`   | `FarmingPanel`           | 1     | Nông trại |
| `"MAP_C2"`      | `C2Panel`                | 2     | Ký túc xá |
| `"MAP_D9"`      | `D9Panel`                | 3     | Platformer 7 tầng |
| `"MAP_B1"`      | `ArenaPanel`             | 4     | Chọn Boss |
| `"COMBAT_SCREEN"` | `CombatPanel`          | -     | Màn chiến đấu |
| `"MAP_LIBRARY"` | `new JPanel()` (trống)  | -     | **STUB chưa triển khai** |

### Luồng điều hướng

```
WORLD_MAP
  ├──[click Sơn La]──► MAP_SONLA
  ├──[click C2]──────► MAP_C2
  ├──[click D9]──────► MAP_D9
  └──[click B1]──────► MAP_B1
                           └──[THÁCH ĐẤU]──► COMBAT_SCREEN
                                                 └──[thắng/thua]──► MAP_B1 / WORLD_MAP

MAP_D9
  └──[ESC]──────────► WORLD_MAP
  
GameManager.handlePlayerFaint()
  └──────────────────► WORLD_MAP (bất kỳ map nào)
```

### Đặc biệt của D9Panel
D9Panel có lifecycle callbacks riêng:
- `onShown()` → khởi động `gameTimer` (60 FPS)
- `onHidden()` → dừng `gameTimer`, reset input flags

`GameWindow.showPanel()` gọi 2 method này tự động.

---

## 4. Phân Loại Trách Nhiệm

### UI (Giao diện)
| Class | Vai trò UI |
|-------|-----------|
| `GameWindow` | JFrame chính, quản lý CardLayout, tạo và đăng ký tất cả panels |
| `StatsPanel` | HUD cố định ở trên, hiển thị Level/Energy/Finance/EXP |
| `WorldMapPanel` | Vẽ ảnh nền world_map.png + invisible hotspot buttons |
| `C2Panel` | Vẽ ảnh c2_map.png + di chuyển nhân vật Vũ + hotspot buttons |
| `FarmingPanel` | Giao diện farming Sơn La |
| `D9Panel` | Rendering TMX tilemap + physics player + input handling (821 dòng) |
| `ArenaPanel` | Danh sách Boss cards để chọn thách đấu |
| `CombatPanel` | Màn chiến đấu turn-based, HP bars, combat log |
| `LazyEncounterDialog` | Dialog ngẫu nhiên khi ở Map C2 |
| `ShopDialog` | Dialog cửa hàng trong C2 |

### Game State (Trạng thái Game)
| Class | Vai trò State |
|-------|--------------|
| `GameManager` | **Singleton trung tâm**: lưu `currentMapID`, `currentHour`, energy regen timer, lazy NPC timer. Điều phối chuyển map. |
| `Player` | Lưu toàn bộ stats (energy, finance, willpower, discipline, hp, exp, level) và inventory |
| `D9Player` | State riêng của D9: vị trí, physics (velocity), animation state, checkpoint floor |
| `D9CheckpointManager` | Lưu checkpoint hiện tại trong D9 |

### Map Logic (Logic Bản Đồ)
| Class | Vai trò Map Logic |
|-------|-----------------|
| `SonLaMap` | Logic trồng cây, thu hoạch, tiêu Energy, loot |
| `C2Map` | **STUB** — logic thực tế nằm trong `GameManager.handleMap2Actions()` |
| `B1Map` | **STUB** — logic thực tế nằm trong `ArenaPanel` + `CombatPanel` |
| `WorldMap` | Logic điều hướng (gọi `GameManager.switchMap()`) — ít dùng |
| `D9Map` | Data model bản đồ TMX: layers, tilesets, collision boxes, quiz doors, spawns |
| `D9MapLoader` | Parse file TMX → `D9Map` object |
| `D9CollisionManager` | Kiểm tra va chạm platform/boundary, snap player |
| `Boss` | Data boss (HP, skills) + static factory methods |

---

## 5. Vấn Đề Kiến Trúc Quan Trọng

### ⚠️ Circular Reference
`Player` → gọi `GameManager.getInstance()` trực tiếp trong `setEnergy()`.
Điều này tạo vòng lặp phụ thuộc: `GameManager → Player → GameManager`.

### ⚠️ God Class trong GameManager
`GameManager` đang giữ quá nhiều thứ:
- Game state (`currentMapID`, `currentHour`)
- Energy regen timer
- Lazy NPC spawn logic
- C2 action handler (`handleMap2Actions`)
- Player faint handler
- Tham chiếu trực tiếp đến `StatsPanel` và `GameWindow`

### ⚠️ Map model Stub chưa có logic
`B1Map` và `C2Map` trong `maps/` chỉ là stub trống.  
Logic thực tế của B1 nằm hoàn toàn ở `ArenaPanel` + `CombatPanel`.  
Logic thực tế của C2 nằm ở `GameManager.handleMap2Actions()`.

---

## 6. Vị Trí Tích Hợp Map B1 Mới

### Bối cảnh hiện tại
- **`ArenaPanel`** = UI màn B1 hiện tại: hiển thị 3 boss card (static UI, không có gameplay thực sự).
- **`B1Map.java`** = STUB hoàn toàn trống (`startBossFight()` chỉ in ra console).
- **`CombatPanel`** = Màn chiến đấu turn-based đơn giản (đòn đánh, HP bar, combat log).
- MapID = **4**, Card Key = **`"MAP_B1"`**.

### Điểm tích hợp phù hợp nhất

| Hạng | Nơi tích hợp | Lý do |
|------|-------------|-------|
| ✅ **1** | `maps/b1/` → thêm các class logic mới | Đúng package, giữ cấu trúc nhất quán với D9 (có `D9Map`, `D9Player`,...) |
| ✅ **2** | `ArenaPanel.java` | Là điểm vào duy nhất của B1 từ WorldMap. Cần sửa để gọi logic thực. |
| ✅ **3** | `CombatPanel.java` | Sẽ cần bổ sung nếu gameplay chiến đấu B1 phức tạp hơn. |
| ⚠️ **4** | `GameManager.switchMap(4)` | Chỉ set `currentMapID = 4`, không trigger logic B1 nào. Có thể bổ sung hook ở đây. |

### Chiến lược tích hợp đề xuất (PHÂN TÍCH THUẦN TÚY)

**Nếu Map B1 mới là một màn chơi có gameplay riêng** (như D9 có platformer):
```
maps/b1/
  ├── B1Map.java            ← Mở rộng (thêm data model, floor/room structure)
  ├── B1MapLoader.java      ← NEW: Load map data (TMX hoặc JSON)
  ├── B1Player.java         ← NEW: Player state riêng B1 (nếu cần)
  └── B1BossManager.java    ← NEW: Quản lý boss sequence

ui/panels/
  └── ArenaPanel.java       ← SỬA: thêm logic khởi động B1Map,
                               hoặc tạo B1Panel mới tương tự D9Panel
```
→ **Đăng ký `"MAP_B1"` mới trong `GameWindow`**, hoặc giữ key cũ và thay class.

**Nếu Map B1 mới chỉ thêm boss/cơ chế vào flow hiện tại**:
- Sửa `ArenaPanel.java` + `B1Map.java` là đủ.
- `CombatPanel` đã sẵn sàng nhận `Boss` object.

### ⚡ Điểm kết nối quan trọng nhất
```java
// WorldMapPanel.java, line 112-117
private void openMap4() {
    window.showPanel("MAP_B1");          // ← Đây là cổng vào
    GameManager.getInstance().switchMap(4);
    statsPanel.updateStats();
}
```
Mọi thay đổi của Map B1 đều phải đảm bảo card key `"MAP_B1"` trong `GameWindow` 
trỏ đến đúng panel mới, và `GameManager.switchMap(4)` có thể bổ sung hook nếu cần.
