# **OOP GAME: Chronicles of HUST (Biên Niên Sử Bách Khoa)**

## **Tài Liệu Thiết Kế Game (Game Design Document \- GDD) — Phiên Bản 2.0 (Reworked B1)**

**Ngôn ngữ lập trình:** Java / C++  
**Thể loại:** RPG Giải Đố Sinh Tồn (Map 1-3) kết hợp Top-down Action Rogue-like (Map 4\)  
**Đối tượng:** Sinh viên HUST & Game thủ OOP

## ---

**1\. TỔNG QUAN GAME**

### **1.1 Tên & Slogan**

* **Tên Game:** Chronicles of HUST (Biên Niên Sử Bách Khoa)  
* **Tên thế giới:** VŨ EM ĐI HỌC  
* **Slogan:** "HỌC TẬP CHÍNH LÀ ĐỊNH MỆNH\!"

### **1.2 Thể Loại & Nền Tảng**

* **Thể loại:** RPG kết hợp Simulation \+ Puzzle \+ Action Rogue-like Boss Fight  
* **Nền tảng:** PC (Java Swing / LibGDX) hoặc C++ với SFML  
* **Số người chơi:** 1 (Single Player)  
* **Ngôn ngữ hiển thị:** Tiếng Việt

### **1.3 Cốt Truyện & Bối Cảnh**

Vũ — một thanh niên chất phác từ vùng đất Sơn La — mang trong mình ước mơ cháy bỏng: chinh phục cánh cổng danh giá của Đại học Bách Khoa Hà Nội. Cuộc hành trình bắt đầu từ những buổi sáng cuốc đất trên nương rẫy, tích cóp từng đồng tiền mồ hôi để nuôi dưỡng giấc mơ lên Thủ đô. Tại Hà Nội, thử thách không chỉ là những kỳ thi khắc nghiệt mà còn là cám dỗ, lười biếng và những "Boss" huyền thoại trong giảng đường. Mục tiêu tối thượng: không chỉ là tấm bằng, mà là VỊ THẾ QUYỀN LỰC — một công việc danh giá, thu nhập cao và uy tín xã hội.

### **1.4 Ngôn Ngữ Lập Trình & Kiến Trúc OOP**

* **Ngôn ngữ chính:** Java (ưu tiên) hoặc C++  
* **Kiến trúc:** Hướng đối tượng (OOP) — thể hiện 4 nguyên lý: Encapsulation, Inheritance, Polymorphism, Abstraction  
* **Mẫu thiết kế (Design Patterns) áp dụng:** Singleton (GameManager), Observer (EventSystem), Strategy (BossBehavior), Factory (ItemFactory)

## ---

**2\. NHÂN VẬT & HỆ THỐNG CHỈ SỐ**

### **2.1 Nhân Vật Chính — Sinh Viên Sơn La (Vũ)**

* **Xuất thân:** Thanh niên 18 tuổi, lớn lên tại bản Mường, Sơn La. Chăm chỉ, thật thà, ý chí kiên cường.  
* **Mục tiêu:** Đậu Đại học Bách Khoa → Hoàn thành 4 bản đồ → Đạt VỊ THẾ QUYỀN LỰC (JOB).

### **2.2 Hệ Thống Chỉ Số Cốt Lõi (Core Stats)**

| Chỉ Số | Giá Trị Khởi Đầu | Cách Tăng | Hậu Quả Khi Cạn/Thấp   |
| :---- | :---- | :---- | :---- |
| **Năng Lượng (Energy / HP)** | 20/20 | Nghỉ ngơi: \+1/30 phút (quê); \+1/1 tiếng (thành phố). Dùng Thực phẩm chức năng: \+3 tức thì. | \= 0: Ngất, mất 50% tài chính; bị kéo về điểm nghỉ gần nhất. |
| **Tài Chính (Finance)** | 50 VNĐ ảo | Lao động Map 1, làm thêm Map 2, nhận thưởng quest. | \= 0: Không thể mua vật phẩm, mất quyền truy cập một số khu vực. |
| **Ý Chí (Willpower)** | 30/100 | Hoàn thành quest khó (+5-15), đọc sách (+3), thắng Boss (+20). | \< 20: Dễ bị cám dỗ Lười Biếng, \-30% Điểm Rèn Luyện mỗi ngày. |
| **Điểm Rèn Luyện (Discipline Score)** | 0/100 | Check-in Chuyên cần 2 lần/ngày (+5 mỗi lần), hoàn thành quest Map 2 (+10-20). | \< 50: NERF toàn chỉ số \-20% (Tăng lên thành \-30% khi đối đầu Boss độc quyền tại Map 4 Rework). |
| **Khả Năng Giải Quyết Vấn Đề (Solution Skill)** | 10/100 | Đọc Sách Giáo Trình (+5), thắng tầng D9 (+8), đúng câu trắc nghiệm (+1). | \< 30: Thời gian làm bài/Thời gian phản xạ Quick-time Event bị giảm 50%. |

### **2.3 Cấp Độ & Thăng Hạng (Level System)**

Nhân vật có 10 cấp độ. Mỗi cấp mở khóa kỹ năng thụ động mới:

* **Cấp 1-3:** Nông Dân Khởi Nghiệp – Mở khóa kỹ năng Canh Tác Nhanh (+20% thu hoạch).  
* **Cấp 4-5:** Tân Sinh Viên – Mở khóa Check-in Combo (3 ngày liên tiếp \= \+50 Điểm Rèn Luyện bonus).  
* **Cấp 6-7:** Sinh Viên Kỳ Cựu – Mở khóa Ôn Thi Thần Tốc (Giảm 20% thuộc tính của thử thách).  
* **Cấp 8-9:** Chiến Binh Học Thuật – Kháng hoàn toàn 1 đòn phạt trạng thái từ thế lực học đường.  
* **Cấp 10:** HUST LEGEND – Tất cả chỉ số tăng 30%, mở khóa Ending thật.

## ---

**3\. HỆ THỐNG VẬT PHẨM**

### **3.1 Bảng Vật Phẩm Chi Tiết**

#### **Vật Phẩm Sinh Tồn (Map 1 Sơn La)**

* **Hạt Giống Ngô (Tiêu hao):** \+15 Tài Chính sau 5 phút trồng; cần 3 Energy. Nguồn gốc: Cửa hàng Map 1\. Giá: 10 VNĐ.  
* **Cuốc Đất Sắt (Trang bị):** \+25% tốc độ thu hoạch vĩnh viễn. Nguồn gốc: Thưởng quest đầu. Giá: Miễn phí.  
* **Gói Cơm Nắm (Tiêu hao):** \+5 Energy tức thì, dùng được 2 lần. Nguồn gốc: Cửa hàng Map 1\. Giá: 8 VNĐ.  
* **Bình Nước Sơn La (Tiêu hao):** \+8 Energy; buff \+10% Ý Chí trong 3 phút. Nguồn gốc: Loot ngẫu nhiên khi thu hoạch. Giá: 15 VNĐ.

#### **Vật Phẩm Học Thuật (Map 2-3)**

* **Sách Giải Tích A1 (Vĩnh viễn):** \+15 Solution Skill; \+10 Willpower. Nguồn gốc: Thư viện Map 3\. Giá: 50 VNĐ.  
* **Giáo Trình Lập Trình OOP (Vĩnh viễn / Vật phẩm ném):** \+20 Solution Skill. Tác dụng tại Map 4: Ném kích hoạt nổ diện rộng phá giáp Boss. Nguồn gốc: Quest Map 3 tầng 5\. Giá: Miễn phí.  
* **Cà Phê Đen Robusta (Tiêu hao):** \+15 Willpower tạm thời; kháng Lười Biếng; \+2 Năng Lượng. Nguồn gốc: Cửa hàng Map 2\. Giá: 12 VNĐ.  
* **Nước Tăng Lực Monster (Tiêu hao):** \+25 Willpower; \+5 Năng Lượng; sau khi hết: \-5 Energy. Nguồn gốc: Cửa hàng Map 2\. Giá: 20 VNĐ.  
* **Sổ Ghi Chép Chiến Lược (Trang bị / Vật phẩm ném):** \+10% Điểm Rèn Luyện mỗi Check-in. Tác dụng tại Map 4: Ném tạo khiên chắn đạn. Mua tại siêu thị Map 2\. Giá: 35 VNĐ.

#### **Vật Phẩm Quyền Lực (Tích lũy bổ trợ cho Boss Fight)**

* **Bằng Khen SV 5 Tốt (Vĩnh viễn):** \+30 Điểm Rèn Luyện vĩnh viễn; buff \+15% Uy Tín Xã Hội. Nguồn gốc: Thưởng tháng Map 2\. Giá: Không mua được.  
* **Thẻ Bài Tiếng Anh B2 (Trang bị Passive):** Tác dụng tại Map 4: Tự động giải phóng hiệu ứng xấu từ Boss (Tối đa 2 lần/trận). Nguồn gốc: Quest Map 3 tầng 7\. Giá: Không mua được.  
* **Học Bổng KKHT (Tiêu hao / Vật phẩm ném):** \+100 Tài Chính; \+20 Willpower. Tác dụng tại Map 4: Chiêu thức tối thượng khóa cứng một kỹ năng bất kỳ của Boss. Đạt điểm GPA \> 3.5 (Quest). Giá: Không mua được.  
* **Áo Đồng Phục HUST (Trang bị Passive):** Giảm 10% sát thương nhận từ mọi tia đạn hoặc kỹ năng tại Map 4\. Thưởng hoàn thành Map 3\. Giá: Không mua được.

### **3.2 Cửa Hàng & Kinh Tế Game**

* **Tiền tệ:** VNĐ ảo (đơn vị: đồng).  
* **Nguồn thu:** Lao động Map 1 (5-20 đ/vụ), quest thưởng (10-100 đ), bán nông sản.  
* **Nguồn chi:** Vật phẩm, nâng cấp nông trại, mở khóa khu vực bí mật.

## ---

**4\. BẢN ĐỒ & CÁCH CHƠI (TỔNG QUAN)**

| Bản Đồ | Tên Bản Đồ | Mục Tiêu Chính | Mở Khóa Điều Kiện   |
| :---- | :---- | :---- | :---- |
| **Map 1** | Vùng Đất Sơn La | Kiếm 200 VNĐ ảo; đủ Năng Lượng để lên Hà Nội. | Mặc định (xuất phát). |
| **Map 2** | Rèn Luyện C2 | Đạt 80 Điểm Rèn Luyện; hoàn thành 30 Check-in. | Hoàn thành Map 1\. |
| **Map 3** | Mê Cung D9 | Vượt qua 7 tầng thử thách nền tảng kiến thức OOP. | Hoàn thành Map 2\. |
| **Map 4** | Đấu Trường B1 | **Đánh bại Đại Giáo Sư Tổng Hợp (HUST OVERLORD)** | Hoàn thành Map 3\. |

## ---

**5\. CHI TIẾT CƠ CHẾ VẬN HÀNH MAP 1, MAP 2, MAP 3**

### **BẢN ĐỒ 1: VÙNG ĐẤT KHỞI NGUYÊN**

Bối cảnh: Nương rẫy vùng cao Sơn La đầy ý chí.

* **Trồng trọt:** Chọn hạt giống → Cuốc đất (tiêu 2 Energy) → Tưới nước (tiêu 1 Energy) → Thu hoạch sau T phút nhận Tài Chính.  
* **Chăn nuôi:** Mua gà lợn → Cho ăn hàng ngày → Bán khi đủ cân nhận Tài Chính gấp 2\.  
* **Sự kiện ngẫu nhiên:** Hạn hán (năng suất \-50%), Mùa bội thu (+30%), Sâu bệnh.  
* **Save Point:** Căn chòi nương – nghỉ ngơi hồi \+1 Energy/30 phút.  
* **Quest qua màn:** Tích lũy đủ 200 VNĐ ảo \+ mua vé xe khách lên Hà Nội (80 VNĐ).

### **BẢN ĐỒ 2: RÈN LUYỆN CHI KHU**

Bối cảnh: Ký túc xá, khu giảng đường HUST nơi cám dỗ và kỷ luật cùng tồn tại.

* **Check-in Chuyên Cần:** Sáng (6:00-8:00): \+5 Điểm Rèn Luyện; \+2 Willpower. Tối (21:00-23:00): \+5 Điểm Rèn Luyện; \+1 Willpower. Combo 7 ngày: \+20 Điểm Rèn Luyện bonus.  
* **NPC Lười Biếng:** Xuất hiện quấy phá trừ 3 Điểm Rèn Luyện, đánh bại bằng Ý Chí \> 50\.  
* **Làm thêm (Mini-game):** Gia sư / Bán hàng online → \+15-30 VNĐ/ca; tiêu 3 Energy.

### **BẢN ĐỒ 3: THÁP OOP \- D9**

Bối cảnh: Tòa nhà D9, thiết kế dạng tháp nhảy platformer (Cơ chế giống Jump King). Giữ phím tích lực nhảy, thả để bật lên. Căn sai lực sẽ bị rơi xuống các bệ thấp hơn.

* **Cơ chế cốt lõi:** Nhảy leo tầng → Chạm cửa sắt → Trả lời combo 5 câu hỏi kiến thức theo chủ đề từng tầng để qua màn.  
  * Tầng 1: Class & Object (Đúng ≥ 3/5).  
  * Tầng 2: Encapsulation (Đúng ≥ 3/5).  
  * Tầng 3: Inheritance (Đúng ≥ 3/5).  
  * Tầng 4: Polymorphism (Đúng ≥ 3/5).  
  * Tầng 5: Abstraction (Đúng ≥ 3/5).  
  * Tầng 6: Interface (Đúng ≥ 3/5).  
  * Tầng 7: OOP Final (Đúng ≥ 4/5). Sai bị phạt rơi xuống tầng trước.

## ---

**6\. \[REWORKED\] BẢN ĐỒ 4: ĐẤU TRƯỜNG SINH TỒN B1**

### **6.1 Tổng Quan Gameplay Mới**

Thay thế hoàn toàn cơ chế đánh theo lượt (Turn-based) truyền thống. Map 4 chuyển cấu trúc sang dạng **Top-down Action Shoot 'em up (Bắn chưởng góc nhìn từ trên xuống)** mô phỏng trực quan theo phong cách giao diện của game sinh tồn hành động.

* **Hình thức điều khiển:** Vũ di chuyển tự do bằng cụm phím WASD / Mũi tên. Định hướng bắn bằng chuột hoặc phím điều hướng chuyên dụng.  
* **Phương thức tấn công thường:** Vũ liên tục tung ra các **Tia sáng Tri thức (Magic Chưởng)** tiêu hao 0.1 Năng Lượng mỗi giây để gây sát thương liên tục lên thực thể Boss. Không sử dụng súng, hoàn toàn dùng sức mạnh nội tại của tri thức.

### **6.2 Boss Duy Nhất: ĐẠI GIÁO SƯ TỔNG HỢP (HUST OVERLORD)**

Để đảm bảo tính thử thách tối cao và ép buộc người chơi phải tích lũy tối đa các chỉ số ở các map trước, toàn bộ hội đồng giáo sư được hợp thể thành một Boss độc nhất vô nhị.

* **Lượng máu (HP):** 600 HP.  
* **Điều kiện bắt buộc thiên địch:** Nhân vật Vũ phải đạt **Cấp độ ≥ 8** và có **Điểm Rèn Luyện ≥ 80** thì đòn bắn thường mới gây sát thương gốc (Nếu không đạt, sát thương giảm 80%).

### **6.3 Cơ Chế Kích Hoạt Vật Phẩm Ném (Ngưỡng Chỉ Số & Cooldown)**

Người chơi không thể sử dụng vật phẩm bừa bãi. Để ném một vật phẩm hỗ trợ hạng nặng vào Boss gây lượng sát thương lớn hoặc tạo lợi thế, Vũ phải đạt đủ các ngưỡng chỉ số tích lũy từ các map cũ. Mỗi vật phẩm có thời gian hồi chiêu độc lập (Cooldown):

| Tên Vật Phẩm | Ngưỡng Chỉ Số Kích Hoạt | Hiệu Ứng Khi Ném Vào Boss | Thời Gian Hồi (Cooldown)   |
| :---- | :---- | :---- | :---- |
| **Giáo Trình Lập Trình OOP** | Solution Skill ≥ 70 | Tạo vụ nổ tri thức lớn: \-60 HP của Boss, đồng thời làm chậm tốc độ di chuyển và tốc độ bắn đạn của Boss đi 20% trong 5 giây. | 15 giây |
| **Sổ Ghi Chép Chiến Lược** | Điểm Rèn Luyện ≥ 80 | Phóng ra các trang giấy ghi chú xếp thành vòng tròn ma thuật bao quanh Vũ, chặn đứng 100% các tia đạn lạc từ Boss hướng vào người trong 4 giây. | 20 giây |
| **Học Bổng KKHT** | Cấp độ ≥ 8 và Tài Chính ≥ 100 VNĐ | Chiêu thức kích nổ tối thượng: Đánh thẳng \-80 HP vĩnh viễn vào Boss, đồng thời hồi phục ngay lập tức 10 điểm Năng Lượng (HP) cho Vũ. | 30 giây |

*\*Lưu ý hệ thống: Nếu người chơi cố tình nhấn phím tắt kích hoạt vật phẩm khi chưa đạt đủ ngưỡng chỉ số quy định, hệ thống sẽ từ chối lệnh và hiển thị dòng chữ cảnh báo đỏ trên màn hình: "Trình độ học thuật chưa đủ để thấu hiểu và sử dụng bảo vật này\!"*

### **6.4 Cơ Chế Phòng Thủ Đóng Băng Thời Gian: "OOP FLASH QUIZ"**

Để lồng ghép yếu tố lập trình một cách tự nhiên vào thể loại hành động tốc độ cao, game áp dụng cơ chế Quick-time Event đóng băng tiến trình.

#### **A. Tránh mất máu khi dính đạn lạc**

* Khi bất kỳ tia đạn hoặc đòn chưởng nào của Boss va chạm vào hitbox (khối va chạm) của nhân vật Vũ, trò chơi lập tức tạm dừng mọi chuyển động vật lý trong phòng đấu (Time Freeze trong 3 giây).  
* Một cửa sổ câu hỏi trắc nghiệm nhanh thuộc bộ dữ liệu tổng hợp oop\_final xuất hiện.  
* **Nếu người chơi trả lời ĐÚNG:** Vũ kích hoạt trạng thái "Kháng Cự Thành Công" (Immune), không bị mất giọt máu nào, đồng thời nhận 1.5 giây bất tử ẩn thân để nhanh chóng di chuyển ra khỏi vùng nguy hiểm.  
* **Nếu người chơi trả lời SAI hoặc Hết thời gian ngưng đọng:** Vũ nhận trọn vẹn sát thương thuần, thanh Năng lượng bị trừ ngay lập tức từ 5 đến 15 HP (tùy thuộc vào cấp độ nguy hiểm của tia đạn).

#### **B. Kháng hiệu ứng bất lợi ngẫu nhiên (Debuff) từ Boss**

Trong quá trình di chuyển bắn chưởng, Boss sẽ ngẫu nhiên thi triển các đòn nguyền rủa diện rộng ép người chơi phải giải đố để né tránh hoàn toàn hiệu ứng:

1. **Hiệu ứng "Quét Camera Giám Sát" (Camera Surveillance):** Boss bật tầm quét hồng ngoại toàn sân đấu. Màn hình hiện câu hỏi ngẫu nhiên về nguyên lý Encapsulation (Đóng gói). Nếu Sai: Vũ bị khóa hoàn toàn chức năng bắn chưởng thường và khóa ném vật phẩm phụ trợ trong vòng 6 giây.  
2. **Hiệu ứng "Xoáy Cơ Học Đại Cương" (Xoáy AOE):** Một lốc xoáy xuất hiện hút chặt Vũ vào tâm đạn. Màn hình hiện câu hỏi về nguyên lý Polymorphism (Đa hình). Nếu Sai: Tốc độ di chuyển của Vũ bị giảm mạnh 50% trong thời gian 5 giây kế tiếp.  
3. **Hiệu ứng "Trực Xuất Điểm Rèn Luyện":** Đòn trừng phạt tối cao của Boss. Dưới chân Vũ xuất hiện ma trận chữ, yêu cầu giải nghĩa câu hỏi tích hợp sâu về kiến thức hệ thống. Nếu Sai: Bị trừ thẳng 20 Điểm Rèn Luyện. Khi Điểm Rèn Luyện bị hạ xuống dưới mức 50, Vũ dính hiệu ứng NERF vĩnh viễn: Giảm 30% tốc độ chạy và giảm 30% sát thương đầu ra cho đến khi kết thúc trận chiến.

### **6.5 Tác Dụng Vật Phẩm Trang Bị Thụ Động (Passive Items)**

* **Áo Đồng Phục HUST:** Tự động giảm trừ sát thương nền. Mọi đòn đánh trực diện từ Boss khi người chơi trả lời sai câu hỏi trắc nghiệm đạn sẽ được giảm 10% sát thương nhận vào.  
* **Thẻ Bài Tiếng Anh B2:** Hoạt động giống như lá bùa hộ mệnh tự động. Khi Vũ dính các hiệu ứng nguy hiểm như Quét Camera hoặc Trực Xuất Điểm Rèn Luyện, Thẻ B2 sẽ tự động tiêu hao để trả lời đúng thay cho người chơi, giải trừ lập tức trạng thái bất lợi (Tối đa kích hoạt 2 lần mỗi trận đấu).

## ---

**7\. KIẾN TRÚC THIẾT KẾ OOP KHÔNG ĐỔI**

Mô hình phân rã lớp (Class Diagram) và việc ánh xạ các nguyên lý thiết kế hướng đối tượng được bảo lưu hoàn chỉnh để phục vụ code lõi cho Map 4 mới:

* **Encapsulation:** Thuộc tính private int energy của lớp Player chỉ được biến đổi hợp lệ thông qua các phương thức kiểm soát như takeDamage() hoặc useItem() sau khi xử lý xong tầng lọc kiểm tra câu hỏi.  
* **Inheritance & Polymorphism:** Thực thể HUSTOverlord kế thừa cấu trúc từ EnemyBase và nạp chồng (Override) phương thức attack() để chuyển từ mô hình đánh theo lượt cũ sang kiến trúc sản sinh hạt đạn liên tục (BulletSpawnEngine).

## ---

**8\. HỆ THỐNG QUEST VÀ CÁC KẾT THÚC (MULTIPLE ENDINGS)**

* **MAIN QUEST 8 — Ngọn Cờ Bách Khoa:** Tiêu diệt thành công Đại Giáo Sư Tổng Hợp (HUST OVERLORD) tại Map 4 để phá đảo trò chơi.  
* **ENDING 1: VỊ THẾ QUYỀN LỰC (True Ending):\*\*** Tiêu diệt Boss \+ Nhân vật đạt cấp độ 10 \+ Điểm Rèn Luyện ≥ 80\. Vũ nhận được lời mời làm việc mơ ước từ các tập đoàn công nghệ lớn.  
* **ENDING 2: TỐT NGHIỆP BÌNH THƯỜNG (Normal Ending):\*\*** Tiêu diệt được Boss nhưng các chỉ số phụ không đạt điều kiện lý tưởng (Điểm Rèn Luyện \< 80 hoặc Cấp độ \< 8). Vũ có cuộc sống công việc bình thường.  
* **ENDING 3: BỎ CUỘC (Bad Ending):\*\*** Để thanh Năng lượng của Vũ cạn kiệt về mức 0 quá 3 lần trong phòng đấu B1. Vũ buộc phải nghỉ học quay trở về quê hương trồng ngô chăn lợn.