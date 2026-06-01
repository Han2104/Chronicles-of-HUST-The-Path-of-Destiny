# Walkthrough — Map B1 Implementation

**Kết quả:** ✅ BUILD SUCCESS — 0 compile errors | Game đang chạy

---

## Những gì đã được triển khai

### Kiến trúc mới (CardLayout)

```
WORLD_MAP ──[click B1]──► "MAP_B1" (B1LobbyPanel)   ← Sảnh chờ mới
                              └──[VÀO ĐẤU TRƯỜNG]──► "B1_ARENA" (B1Panel)
                                                           ├──[VICTORY]──► WORLD_MAP
                                                           ├──[GAME OVER]──► MAP_B1 (retry)
                                                           └──[ESC]──► MAP_B1
```

---

## Files đã tạo / sửa

### 🆕 File mới

| File | Mô tả |
|------|-------|
| [B1LobbyPanel.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/ui/panels/B1LobbyPanel.java) | Sảnh chờ B1: boss card, player stats, nút VÀO ĐẤU TRƯỜNG |
| [B1DebuffManager.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/maps/b1/B1DebuffManager.java) | 3 loại debuff GDD 6.4B + Thẻ B2 auto-answer |

### ✏️ File sửa

| File | Thay đổi chính |
|------|---------------|
| [B1Panel.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/ui/panels/B1Panel.java) | Constructor `(GameWindow, StatsPanel)`, sprite rendering, HUD đầy đủ, 3 endings, debuff quiz dialog |
| [B1Engine.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/maps/b1/B1Engine.java) | Player auto-fire, damage multiplier GDD, B1DebuffManager tích hợp, State.DEBUFF_QUIZ_MODE |
| [B1Boss.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/models/entities/B1Boss.java) | `applySlowDebuff()`, speed multiplier ảnh hưởng attackCooldown |
| [B1ItemManager.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/maps/b1/B1ItemManager.java) | Effects đúng GDD: OOP -60HP+slow, Notebook shield, KKHT -80HP+heal |
| [B1CollisionManager.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/maps/b1/B1CollisionManager.java) | `shieldActive` param — bỏ qua đạn boss khi SO_GHI_CHEP |
| [B1BulletManager.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/maps/b1/B1BulletManager.java) | Overload `bossSpawnSpreadBullets` gọn hơn |
| [GameWindow.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/ui/GameWindow.java) | Register `"MAP_B1"=B1LobbyPanel`, `"B1_ARENA"=B1Panel`, lifecycle hooks |
| [Player.java](file:///d:/Chronicles-of-HUST-The-Path-of-Destiny/src/main/java/com/hust/game/models/entities/Player.java) | `addDisciplineScore()` hỗ trợ âm, thêm `setFinance()` |

---

## Tính năng theo GDD V2.0 — Status

| GDD Section | Tính năng | Status |
|-------------|-----------|--------|
| 6.1 | WASD movement, mouse aim, auto-fire chưởng | ✅ |
| 6.2 | Boss HP 600, 3-phase movement | ✅ |
| 6.2 | Damage check: Lv≥8 & Discipline≥80 (else -80%) | ✅ |
| 6.3 | GIAO_TRINH_OOP: Skill≥70 → -60HP boss + slow 5s | ✅ |
| 6.3 | SO_GHI_CHEP: Discipline≥80 → Shield 4s | ✅ |
| 6.3 | HOC_BONG_KKHT: Lv≥8&Tiền≥100 → -80HP boss + +10 energy | ✅ |
| 6.3 | Cảnh báo đỏ khi item chưa đủ điều kiện | ✅ |
| 6.4A | Đạn trúng → Time Freeze → Quiz → đúng phản đòn, sai -HP -Energy | ✅ |
| 6.4B | Camera Surveillance → Encapsulation quiz → Khóa bắn 6s | ✅ |
| 6.4B | Xoáy AOE → Polymorphism quiz → Speed -50% 5s | ✅ |
| 6.4B | Trực Xuất → OOP quiz → -20 DisciplineScore | ✅ |
| 6.4B | NERF vĩnh viễn khi Discipline < 50 | ✅ |
| 6.5 | Áo HUST: damage reduction (qua B1CollisionManager) | 🔶 partial |
| 6.5 | Thẻ B2: auto-answer 2 lần | ✅ |
| Endings | ENDING 1 (True): Lv≥10 & Discipline≥80 | ✅ |
| Endings | ENDING 2 (Normal): thắng boss nhưng stats thấp | ✅ |
| Endings | ENDING 3 (Bad): chết ≥ 3 lần | ✅ |

> [!NOTE]
> 🔶 **Áo HUST** (giảm 10% damage): cần thêm boolean `hasHUSTUniform` vào Player và đọc trong `B1CollisionManager` để apply giảm damage khi quiz sai. Có thể thêm trong 1 commit nhỏ.

---

## Luồng chơi hoàn chỉnh

1. **WorldMap** → Click `B1 Đấu Trường` → **B1LobbyPanel** (sảnh)
2. Xem thống kê, kiểm tra điều kiện damage → Click **VÀO ĐẤU TRƯỜNG**
3. **B1Panel** khởi động (engine reset, timer bắt đầu 60fps)
4. **Gameplay**: WASD di chuyển, chuột aim, tự động bắn chưởng
5. **Đạn trúng** → Quiz → Đúng: phản đòn boss | Sai: -HP -Energy
6. **Debuff trigger** mỗi 8s → Quiz riêng với icon/tiêu đề theo loại
7. **Items** (phím 1/2/3): check conditions → apply effects → cooldown HUD
8. **Boss Phase 1→2→3** khi HP giảm: pattern bắn thay đổi, nhanh hơn
9. **Kết thúc**: Victory (2 endings) hoặc Game Over (retry hoặc thoát)

---

## Compile Verification

```
javac -encoding UTF-8 -cp "src/main/java" -d "bin" [50 files]
→ BUILD SUCCESS — 0 errors, 0 warnings
```
