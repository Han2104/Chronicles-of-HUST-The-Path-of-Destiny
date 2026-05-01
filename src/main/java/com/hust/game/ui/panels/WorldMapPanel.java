package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * WorldMapPanel - Giao diện bản đồ thế giới sử dụng hình ảnh Isometric.
 */
public class WorldMapPanel extends JPanel {
    private BufferedImage backgroundImage;
    private GameWindow window;
    private StatsPanel statsPanel;

    public WorldMapPanel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        
        setLayout(null); // Sử dụng Absolute Layout để đặt nút chính xác lên hòn đảo

        try {
            // Load ảnh từ thư mục assets
            backgroundImage = ImageIO.read(new File("assets/world_map.png"));
        } catch (Exception e) {
            System.err.println("❌ Lỗi: Không tìm thấy ảnh assets/world_map.png. Vui lòng kiểm tra lại đường dẫn.");
        }

        // Đặt các Hotspots (Tọa độ này cần tinh chỉnh theo kích thước ảnh thực tế)
        // Lưu ý: Tọa độ bên dưới là ước lượng dựa trên ảnh bạn gửi
        createIslandButton("Sơn La", 100, 250, 150, 100, "MAP_SONLA", 1);
        createIslandButton("C2 - Ký túc xá", 200, 50, 150, 100, "MAP_C2", 2);
        createIslandButton("D9 - Mê cung", 600, 500, 200, 150, "MAP_D9", 3);
        createIslandButton("B1 - Arena", 650, 350, 150, 120, "MAP_B1", 4);
    }

    private void createIslandButton(String name, int x, int y, int w, int h, String cardName, int mapID) {
        JButton btn = new JButton();
        btn.setBounds(x, y, w, h);
        btn.setToolTipText(name);
        
        // Làm nút tàng hình nhưng vẫn có thể click, loại bỏ hiệu ứng chọn
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(false); // Ngăn không cho nút nhận focus
        
        // Hiệu ứng hover để người chơi biết chỗ đó bấm được
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (mapID == 1) {
                window.showPanel(cardName);
            } else {
                JOptionPane.showMessageDialog(this, "Khu vực " + name + " đang được phát triển!");
            }
            statsPanel.updateStats();
        });

        add(btn);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Vẽ ảnh nền tràn màn hình
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(135, 206, 235)); // Màu trời xanh nếu thiếu ảnh
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.drawString("Vui lòng đặt ảnh 'world_map.png' vào thư mục 'assets'", 50, 50);
        }
    }
}
