package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.models.entities.Player;
import com.hust.game.core.GameManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * WorldMapPanel - Giao diện bản đồ thế giới với các Hotspot tàng hình.
 */
public class WorldMapPanel extends JPanel {
    private BufferedImage backgroundImage;
    private GameWindow window;
    private StatsPanel statsPanel;
    private final List<ButtonInfo> mapButtons = new ArrayList<>();
    
    // Dùng kích thước ảnh gốc để tính hitbox theo tỷ lệ
    private final double BASE_W = 2976.0;
    private final double BASE_H = 1438.0;

    private static class ButtonInfo {
        JButton btn;
        int origX, origY, origW, origH;
        ButtonInfo(JButton btn, int x, int y, int w, int h) {
            this.btn = btn; this.origX = x; this.origY = y; this.origW = w; this.origH = h;
        }
    }

    public WorldMapPanel(GameWindow window, StatsPanel statsPanel) {
        this.window = window;
        this.statsPanel = statsPanel;
        
        setLayout(null);

        try {
            backgroundImage = ImageIO.read(new File("assets/world_map.png"));
        } catch (Exception e) {
            System.err.println("❌ Lỗi: Không tìm thấy ảnh assets/world_map.png.");
        }

        // Tạo các hotspot cho bản đồ 2D pixel art mới
        createRegionButton("Sơn La Origin Zone", 150, 72, 982, 532, e -> openMap1());
        createRegionButton("C2 Discipline Zone", 1786, 72, 1042, 532, e -> openMap2());
        createRegionButton("B1 Đấu Trường Huyền Thoại", 150, 604, 1101, 719, e -> openMap4());
        createRegionButton("D9 Mê Cung Học Thuật", 1620, 604, 980, 760, e -> openMap3());
        createRegionButton("JOB - Vị Thế Quyền Lực", 1320, 430, 350, 500, e -> openJobTreasure());
        createRegionButton("Xe khách lên Hà Nội", 2600, 1080, 320, 300, e -> showBusIconInfo());

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repositionComponents();
            }
        });
    }

    private void createRegionButton(String name, int x, int y, int w, int h, ActionListener action) {
        JButton btn = new JButton(name);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(new Color(255, 255, 255, 0)); // Tàng hình mặc định
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(name);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setForeground(new Color(255, 255, 255, 0));
            }
        });

        btn.addActionListener(action);
        mapButtons.add(new ButtonInfo(btn, x, y, w, h));
        add(btn);
    }

    private void openMap1() {
        window.showPanel("MAP_SONLA");
        GameManager.getInstance().switchMap(1);
        statsPanel.updateStats();
    }

    private void openMap2() {
        Player player = GameManager.getInstance().getPlayer();
        if (player.isCompletedMap1()) {
            window.showPanel("MAP_C2");
            GameManager.getInstance().switchMap(2);
        } else if (player.getFinance() >= 1) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Dùng 1 VNĐ để mở khóa vĩnh viễn C2?", "Mở khóa Bản đồ", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                player.addFinance(-1);
                player.setCompletedMap1(true);
                window.showPanel("MAP_C2");
                GameManager.getInstance().switchMap(2);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "C2 đang khóa. Bạn cần 1 VNĐ để mua vé hoặc hoàn thành Sơn La trước.");
        }
        statsPanel.updateStats();
    }

    private void openMap3() {
        Player player = GameManager.getInstance().getPlayer();
        if (player.isCompletedMap2() && player.getDisciplineScore() >= 80) {
            window.showPanel("MAP_D9");
            GameManager.getInstance().switchMap(3);
        } else if (!player.isCompletedMap2()) {
            JOptionPane.showMessageDialog(this,
                    "D9 đang khóa. Bạn cần hoàn thành C2 trước khi vào Mê Cung D9.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "D9 đang khóa. Bạn cần đạt ít nhất 80 Điểm Kỷ luật để mở D9.");
        }
        statsPanel.updateStats();
    }

    private void openMap4() {
        Player player = GameManager.getInstance().getPlayer();
        if (player.getCurrentD9Floor() >= 7) {
            window.showPanel("MAP_B1");
            GameManager.getInstance().switchMap(4);
        } else {
            JOptionPane.showMessageDialog(this,
                    "B1 đang khóa. Bạn cần vượt qua tầng 7 của D9 để mở Đấu Trường Huyền Thoại.");
        }
        statsPanel.updateStats();
    }

    private void openJobTreasure() {
        Player player = GameManager.getInstance().getPlayer();
        boolean unlocked = player.isCompletedMap1() && player.isCompletedMap2() && player.getCurrentD9Floor() >= 7;
        if (unlocked) {
            JOptionPane.showMessageDialog(this,
                    "Bạn đã mở khóa JOB - Vị Thế Quyền Lực!\nHoàn thành B1 Boss Cuối để tiến tới kết thúc cuối cùng.",
                    "JOB - Vị Thế Quyền Lực", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder message = new StringBuilder("Để tiếp cận JOB, bạn cần hoàn thành các bước sau:\n");
            if (!player.isCompletedMap1()) {
                message.append("- Hoàn thành Sơn La Origin Zone\n");
            }
            if (!player.isCompletedMap2()) {
                message.append("- Hoàn thành C2 Discipline Zone và D9 Mê Cung Học Thuật\n");
            }
            if (player.getCurrentD9Floor() < 7) {
                message.append("- Vượt qua Tầng 7 của D9\n");
            }
            message.append("\nHãy tiếp tục hành trình để giành lấy JOB và vị thế quyền lực!");
            JOptionPane.showMessageDialog(this, message.toString(), "JOB - Vị Thế Quyền Lực", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showBusIconInfo() {
        JOptionPane.showMessageDialog(this,
                "Xe khách lên Hà Nội. Nếu chưa có logic riêng, đây là biểu tượng trang trí.",
                "Xe khách", JOptionPane.INFORMATION_MESSAGE);
    }

    private void repositionComponents() {
        double scaleX = getWidth() / BASE_W;
        double scaleY = getHeight() / BASE_H;

        for (ButtonInfo info : mapButtons) {
            info.btn.setBounds(
                (int)(info.origX * scaleX), 
                (int)(info.origY * scaleY), 
                (int)(info.origW * scaleX), 
                (int)(info.origH * scaleY)
            );
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(135, 206, 235));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.drawString("Vui lòng đặt ảnh 'world_map.png' vào thư mục 'assets'", 50, 50);
        }
    }
}
