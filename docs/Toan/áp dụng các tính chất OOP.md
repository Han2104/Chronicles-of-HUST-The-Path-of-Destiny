**Áp Dụng Các Tính Chất OOP**

**Mục tiêu**: Phân tích trực tiếp mã nguồn trong package `d9` để minh họa các tính chất OOP (Encapsulation, Inheritance, Abstraction & Polymorphism, Exception handling).

**Phần 1 - Đóng gói (Encapsulation)**

File: [src/main/java/com/hust/game/maps/d9/D9Player.java](src/main/java/com/hust/game/maps/d9/D9Player.java)

Trích dẫn (thuộc tính private và getter/setter tương ứng):

```java
// từ D9Player.java
private double x, y;
private String lastCheckpointId = "spawn_1";

// Getters/Setters
public double getX() { return x; }
public void setX(double x) { this.x = x; }

public String getLastCheckpointId() { return lastCheckpointId; }
public void setLastCheckpointId(String lastCheckpointId) { this.lastCheckpointId = lastCheckpointId; }
```

Giải thích ngắn: Việc khai báo `private` cho `x`, `y`, `lastCheckpointId` che giấu trạng thái nội bộ của đối tượng khỏi truy cập trực tiếp bên ngoài; chỉ cho phép truy cập/thiết lập thông qua phương thức công khai (`getX`, `setX`, `getLastCheckpointId`, `setLastCheckpointId`). Điều này bảo vệ tính toàn vẹn dữ liệu (ví dụ có thể kiểm soát/kiểm tra giá trị trước khi gán).

**Phần 2 - Kế thừa (Inheritance)**

File: [src/main/java/com/hust/game/maps/d9/D9Player.java](src/main/java/com/hust/game/maps/d9/D9Player.java)
File: [src/main/java/com/hust/game/maps/d9/D9Map.java](src/main/java/com/hust/game/maps/d9/D9Map.java)

Quan sát: `D9Player` và `D9Map` đều khai báo kiểu như `public class D9Player` / `public class D9Map` mà không `extends` hay `implements` nào trong mã nguồn — tức là chúng trực tiếp kế thừa từ `java.lang.Object` mặc định. (Các lớp lồng như `D9Map.Rectangle` là `public static class Rectangle` — là inner static class, cũng kế thừa `Object`.)

Ngắn gọn: Không có lớp cha chuyên dụng trong mã D9 này; nếu cần mở rộng hành vi, có thể cho các lớp `extends` một base class chung hoặc `implements` interface.

**Phần 3 - Trừu tượng (Abstraction) & Đa hình (Polymorphism)**

Lưu ý: Trong `D9Map.java` không có phương thức được đánh dấu `@Override`. Tuy nhiên package `d9` chứa các lớp override ví dụ dưới đây — đây là minh họa đa hình (một phương thức của superclass (`JPanel.paintComponent`) được ghi đè để thay đổi hành vi hiển thị):

File: [src/main/java/com/hust/game/maps/d9/D9QuizManager.java](src/main/java/com/hust/game/maps/d9/D9QuizManager.java)

Trích dẫn:

```java
// từ D9QuizManager.QuizDialog.BackgroundPanel
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g.create();
    if (backgroundImage != null) {
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    } else {
        g2d.setColor(new Color(28, 34, 46));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    g2d.dispose();
}
```

Giải thích ngắn: `@Override` cho thấy phương thức này ghi đè (`override`) hành vi của `JPanel.paintComponent`. Đây là đa hình vì code dùng cùng một tên phương thức (`paintComponent`) nhưng đối tượng `BackgroundPanel` thực hiện cách hiển thị khác — tại runtime JVM gọi phương thức cụ thể tương ứng với kiểu đối tượng thực tế.

**Phần 4 - Xử lý ngoại lệ (Try-Catch)**

File: [src/main/java/com/hust/game/maps/d9/D9MapLoader.java](src/main/java/com/hust/game/maps/d9/D9MapLoader.java)

Trích dẫn khối try-catch đọc/parse tileset (có đọc file TSX / ảnh):

```java
private void parseTilesetFromTsx(String tsxPath, int firstGid, D9Map map) {
    try {
        Document tsxDoc = parseXml(tsxPath);
        Element tilesetElement = tsxDoc.getDocumentElement();
        // ... đọc thuộc tính, lấy <image> và sau đó
        String imagePath = resolveImageSource(AssetLoader.parentPath(tsxPath), imageSource);
        boolean imageExists = assetExists(imagePath);
        boolean imageLoaded = false;
        if (imageExists) {
            try (InputStream imageStream = AssetLoader.openStream(imagePath)) {
                imageLoaded = ImageIO.read(imageStream) != null;
            } catch (Exception ignored) {
            }
        }
        // ... thêm tileset vào map
    } catch (Exception e) {
        System.err.println("[D9Tileset] Warning: failed to load tileset '" + tsxPath + "': " + e.getMessage());
    }
}
```

Giải thích ngắn: Khối `try-catch` bắt chung `Exception` (gồm `IOException`, `ParserConfigurationException`, `SAXException`, v.v.) khi đọc/parsing tệp TSX và ảnh. Nếu không có khối xử lý này, bất kỳ lỗi IO hoặc parse không thành công sẽ ném ngoại lệ lên caller (`loadMap`) và có thể làm dừng quá trình load map (nếu không có catch ở trên nữa). Ở đây, loader xử lý lỗi bằng cách log cảnh báo và tiếp tục, giúp game không bị sập hoàn toàn khi một tileset hỏng/missing — thay vào đó map có thể load một cách giảm chức năng hoặc trả về `null` an toàn.

---

Nếu bạn muốn, tôi sẽ:
- Kiểm tra thêm các ví dụ `@Override` khác trong package `d9` và trích thêm,
- Hoặc mở rộng phần "Gợi ý cải tiến OOP" (ví dụ: đề xuất interface/abstract base cho maps/players).

**Bổ sung - Ví dụ kế thừa và @Override**

File: [src/main/java/com/hust/game/maps/d9/D9QuizManager.java](src/main/java/com/hust/game/maps/d9/D9QuizManager.java)

Trích dẫn (ví dụ về kế thừa từ Swing và lớp con ghi đè phương thức):

```java
// lớp con mở rộng JDialog
private static class QuizDialog extends JDialog {
    // ...
}

// lớp con mở rộng JPanel và ghi đè paintComponent
private class BackgroundPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // ...
    }
}
```

Giải thích ngắn: `QuizDialog` kế thừa hành vi cửa sổ từ `JDialog`, còn `BackgroundPanel` kế thừa `JPanel` và ghi đè `paintComponent` để tùy chỉnh cách vẽ. Đây là ví dụ trực tiếp về kế thừa (dùng hành vi sẵn có của class cha) và đa hình (phiên bản `paintComponent` cụ thể được gọi tại runtime cho đối tượng `BackgroundPanel`).