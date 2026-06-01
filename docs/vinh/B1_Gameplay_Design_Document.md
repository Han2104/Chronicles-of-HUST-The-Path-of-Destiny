# Tổng quan gameplay Map B1
- **Thể loại:** Turn-based RPG kết hợp Quiz Game.
- **Mục tiêu:** Player phải sống sót, trả lời đúng các câu hỏi học thuật và đánh bại Boss của Map B1.
- **Core Loop:** Tương tác theo lượt. Player đưa ra các quyết định sinh tồn (Đánh, Đỡ, Dùng Item), sau đó đối mặt với Quiz. Trả lời Quiz đúng/sai sẽ quyết định lượng sát thương, hiệu ứng buff/debuff. Sau đó Boss sẽ phản công.

# Combat Loop
Luồng chiến đấu tuân theo thứ tự chặt chẽ:
1. **Start Turn:** Tính toán các hiệu ứng (Debuff mất máu, Buff hồi phục). Giảm duration của các effect.
2. **Player Action Phase:** Player chọn 1 trong các hành động: `Attack`, `Defend`, `Use Item`.
3. **Quiz Phase:** Nếu Player chọn `Attack` hoặc bị dính cơ chế đặc biệt, hệ thống hiển thị 1 câu hỏi (Quiz).
4. **Resolution Phase:** Tính toán sát thương, áp dụng các hiệu ứng buff/debuff dựa trên kết quả Quiz.
5. **Boss Phase:** Boss thực hiện hành động dựa trên AI/Phase.
6. **End Turn:** Kiểm tra điều kiện Thắng/Thua. Chuyển sang lượt mới.

# Player Turn
Player sẽ có các lựa chọn sau:
- **Attack:** Kích hoạt Quiz Phase. Tấn công Boss (sát thương phụ thuộc vào kết quả Quiz).
- **Defend:** Không kích hoạt Quiz. Giảm sát thương nhận vào trong lượt kế tiếp của Boss. Phục hồi một ít Thể Lực (Stamina) hoặc Năng lượng (Mana) nếu có.
- **Use Item:** Sử dụng vật phẩm từ Inventory (Hồi máu, giải debuff, buff sát thương). Lượt dùng item diễn ra ngay lập tức và không kích hoạt Quiz.

*Sơ đồ hành động:*
Player Turn → Chọn Action → (Nếu là Attack) → Trả lời Quiz → Kết quả Action

# Boss Turn
Boss hành động dựa trên danh sách kỹ năng và cơ chế AI (Pattern).
- **Normal Attack:** Gây sát thương vật lý trực tiếp lên Player.
- **Skill Attack:** Sử dụng kỹ năng đặc biệt (gây sát thương diện rộng, sát thương phép, hoặc kèm debuff).
- **Apply Debuff:** Phủ các hiệu ứng xấu (Khóa kỹ năng, Trừ giáp, Rút máu) lên Player.
- **Phase Shift:** Thay đổi trạng thái/hình dạng khi máu xuống dưới ngưỡng nhất định.

*Sơ đồ hành động:*
Boss Turn → AI tính toán Skill → Tính Damage nhận/phòng thủ của Player → Gây Damage/Hiệu ứng → Cập nhật UI

# Quiz System
Quiz là core mechanic để tạo đột biến trong combat.
- **Khi nào kích hoạt:** Mỗi khi Player chọn `Attack`.
- **Cơ chế:** Player có một khoảng thời gian ngắn (Ví dụ: 10 giây) để trả lời.
- **Trả lời ĐÚNG (Perfect):**
  - Player gây 150% - 200% Sát thương (Critical Strike).
  - Có tỷ lệ áp dụng Debuff lên Boss (Ví dụ: Choáng, Giảm giáp).
- **Trả lời SAI hoặc HẾT GIỜ (Miss):**
  - Player bị trượt (Miss) hoặc chỉ gây 50% Sát thương.
  - Boss phản đòn (Counter Attack) lập tức, hoặc Player tự bị dính Debuff (Rối loạn, Giảm sát thương).

# Damage Formula
Công thức sát thương cần đơn giản, dễ scale cho Designer:
- **Base Damage (Sát thương gốc):** `Atk (Bên đánh) - Def (Bên chịu đòn) * X%` (Ví dụ X = 0.5)
- **Quiz Multiplier (Hệ số Quiz - chỉ dành cho Player):**
  - Đúng: `x1.5`
  - Sai: `x0.5`
- **Defend Multiplier (Hệ số thủ):** Nếu mục tiêu đang Defend, `Damage = Damage * 0.3` (Giảm 70% sát thương).
- **Random Variance (Độ lệch):** `Damage * Random(0.9 tới 1.1)` (Giúp sát thương không bị cứng nhắc).

*Công thức chung:*
`Final Damage = Max(0, (Base Atk - Target Def * 0.5)) * Multipliers * Random(0.9, 1.1)`

# Status Effect System
Các hiệu ứng (Buff/Debuff) tồn tại theo số lượt (Turn Duration).
- **Cách hoạt động:** Duration sẽ giảm đi 1 vào bước `Start Turn` của nhân vật bị dính. Khi Duration = 0, hiệu ứng biến mất.
- **Các Debuff phổ biến (Player/Boss có thể dính):**
  - `Burn / Poison:` Trừ 5% Max HP mỗi đầu lượt.
  - `Stun:` Mất lượt tiếp theo (Bỏ qua Action Phase).
  - `Silence (Khóa Skill):` Không thể chọn `Attack` (hoặc không kích hoạt Quiz), chỉ có thể `Defend` hoặc `Use Item`.
  - `Armor Break:` Giảm 30% Def.
- **Các Buff phổ biến:**
  - `Regen:` Hồi 5% Max HP mỗi đầu lượt.
  - `Atk Up:` Tăng 30% Atk.

# Defend System
- **Khi nào dùng:** Khi máu Player thấp, chưa có Item hồi máu, hoặc đoán trước Boss chuẩn bị dùng Tuyệt Chiêu (Ultimate).
- **Tác dụng:**
  - Gắn Buff `Guarded` lên Player (Tồn tại 1 lượt).
  - Giảm sát thương nhận vào từ mọi nguồn xuống còn 30%.
  - Miễn nhiễm với các Debuff mới trong lượt đó.

# Inventory Combat
- **Khi nào dùng:** Thay thế cho hành động Attack/Defend trong lượt.
- **Tác dụng:** Dùng bình máu (HP Potion), bình giải thuật (Cure Potion).
- **Cooldown/Limit:** Mỗi trận chỉ được mang tối đa X item (Ví dụ: 3 bình máu, 2 bình giải) để tránh spam. Hành động dùng item ưu tiên chạy ngay lập tức mà không dính Quiz.

# Boss AI
AI Boss được thiết kế dạng State Machine hoặc Pattern đơn giản:
- **Turn 1:** Buff Atk Up.
- **Turn 2:** Normal Attack.
- **Turn 3:** Debuff Player (Silence / Poison).
- **Turn 4:** Gồng năng lượng (Báo hiệu cho Player biết để Defend).
- **Turn 5:** Ultimate Attack (Sát thương cực lớn, nếu Player không Defend sẽ chết).
- *Lặp lại.*

# Boss Phases
Để trận chiến không nhàm chán, Boss có các giai đoạn (Phases).
- **Phase 1 (100% - 50% HP):** Dùng AI Pattern cơ bản như trên.
- **Phase 2 (Dưới 50% HP):**
  - Xóa toàn bộ Debuff trên người Boss.
  - Kích hoạt trạng thái `Enraged` (Tăng 20% Atk, giảm 20% Def).
  - Quiz của Player bị giảm thời gian trả lời (Ví dụ từ 10s xuống 5s).
  - Boss đánh 2 lần trong 1 lượt hoặc sử dụng Skill nguy hiểm hơn.

# Win / Lose Conditions
- **Win:** HP của Boss = 0.
  - Luồng: Trigger animation Boss gục ngã → Hiện màn hình Victory → Phát thưởng (Exp, Item, Gold) → Quay về Map.
- **Lose:** HP của Player = 0.
  - Luồng: Trigger animation Player gục ngã → Hiện màn hình Defeat (Game Over / Thử lại) → Mất một phần tài nguyên hoặc bị đưa về điểm Checkpoint.

# UI Flow
1. **Màn hình Combat khởi tạo:** Load Player UI (Trái), Boss UI (Phải).
2. **Player Turn:** Hiện menu [TẤN CÔNG] [PHÒNG THỦ] [VẬT PHẨM].
3. **Player bấm [TẤN CÔNG]:** Menu ẩn đi → Khung Quiz hiện lên giữa màn hình kèm đồng hồ đếm ngược.
4. **Player chọn Đáp án:**
   - Hiện chữ `PERFECT` hoặc `MISS`.
   - Chạy Animation Player lao lên chém Boss.
   - Nảy số Damage (Damage text popup) trên đầu Boss.
   - Cập nhật thanh máu Boss.
5. **Boss Turn:**
   - Tên Skill của Boss hiện to trên màn hình (Ví dụ: "HỎA CẦU!").
   - Chạy Animation Boss tung chiêu.
   - Nảy số Damage trên đầu Player.
   - Cập nhật thanh máu Player.

# Combat Log Examples
- *[Turn 1]* Player dùng Tấn Công.
- *[Quiz]* Câu hỏi: Định lý Pytago? -> Player chọn đúng! PERFECT!
- *[Player]* Player chém Boss gây 150 sát thương! Boss bị Giảm Giáp 2 lượt!
- *[Boss]* Boss dùng Đánh Thường. Player nhận 40 sát thương.

# Ví dụ 1 trận combat hoàn chỉnh
- **Turn 1:**
  - Player chọn Attack. Trả lời Quiz Đúng. Gây 100 dmg. Boss còn 900/1000 HP.
  - Boss chọn Normal Attack. Gây 30 dmg. Player còn 170/200 HP.
- **Turn 2:**
  - Player chọn Attack. Trả lời Quiz Sai. Bị trượt (0 dmg) và dính Debuff "Giảm Atk".
  - Boss chọn Gồng Năng Lượng. Boss có Buff "Chuẩn bị Ultimate".
- **Turn 3:**
  - Player thấy Boss sắp dùng Ultimate, lanh trí chọn Defend.
  - Boss tung Ultimate "Cú Đấm Hủy Diệt". Do Player Defend, thay vì mất 150 dmg, Player chỉ mất 45 dmg. Player còn 125/200 HP.

# Các chỉ số quan trọng
- **HP (Máu):** Rơi xuống 0 là chết.
- **Max HP:** Mức máu tối đa.
- **Atk (Tấn công):** Dùng để tính Base Damage.
- **Def (Phòng thủ):** Dùng để giảm trừ sát thương vật lý.
- **Speed (Tốc độ):** (Tùy chọn) Xác định ai đi trước mỗi lượt. Nếu làm thuần Player Turn trước thì có thể bỏ qua.

# Risk gameplay cần tránh
- **Spam Defend:** Player chỉ việc bấm Defend rồi đợi Boss chết bằng phản sát thương hoặc độc. *Khắc phục: Defend không gây sát thương, Boss có skill xuyên giáp.*
- **Quiz quá khó hoặc lặp lại:** Gây ức chế. *Khắc phục: Chia độ khó câu hỏi ngẫu nhiên, random bộ câu hỏi lớn.*
- **Snowball Effect:** Boss dính hiệu ứng liên tục không được đánh. *Khắc phục: Boss miễn nhiễm với một số hiệu ứng như Choáng (Stun) quá 2 lần.*

# Cách làm game fun hơn
- **Combo Quiz:** Trả lời đúng 3 câu liên tiếp mở khóa Ultimate Skill của Player (Bỏ qua Quiz, gây sát thương khủng).
- **Đồ họa & Feedback:** Camera rung (Screen shake) khi nảy Damage bự, hiệu ứng âm thanh (SFX) đanh thép khi trả lời đúng Quiz.
- **Yếu tố rủi ro:** Có câu hỏi "Siêu khó" hiện ra, nếu dám chọn và trả lời đúng sẽ Hồi 100% máu, sai sẽ nhận 2x sát thương từ Boss.
