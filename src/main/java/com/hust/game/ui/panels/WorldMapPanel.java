package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.models.entities.Player;
import com.hust.game.core.GameManager;

import javax.swing.*;
import java.awt.*;
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
    
    private final double BASE_W = 1000.0;
    private final double BASE_H = 650.0;

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

        // Tạo các điểm đến dựa trên ảnh "Đảo nổi" của User (1000x650)
        createIslandButton("Sơn La (Vùng núi)", 120, 50, 250, 300, "MAP_SONLA", 1);
        createIslandButton("Tòa C2", 420, 50, 180, 180, "MAP_C2", 2);
        createIslandButton("B1 (Arena)", 680, 30, 280, 250, "MAP_B1", 4);
        createIslandButton("Thư viện Tạ Quang Bửu", 100, 550, 300, 250, "MAP_LIBRARY", 5);
        createIslandButton("D9 (Giảng đường)", 600, 450, 280, 250, "MAP_D9", 3);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repositionComponents();
            }
        });
    }

    private void createIslandButton(String name, int x, int y, int w, int h, String cardName, int mapID) {
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

        btn.addActionListener(e -> {
            Player player = GameManager.getInstance().getPlayer();
            if (mapID == 1) {
                window.showPanel(cardName);
                GameManager.getInstance().switchMap(1);
            } else if (mapID == 2) {
                if (player.isCompletedMap1()) {
                    window.showPanel(cardName);
                    GameManager.getInstance().switchMap(2);
                } else if (player.getFinance() >= 1) {
                    int choice = JOptionPane.showConfirmDialog(this, 
                        "Dùng 1 VNĐ để mở khóa vĩnh viễn Tòa C2?", "Mở khóa Bản đồ", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        player.addFinance(-1);
                        player.setCompletedMap1(true);
                        window.showPanel(cardName);
                        GameManager.getInstance().switchMap(2);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Bạn cần 1 VNĐ (kiếm tại Sơn la) để xuống Hà Nội học C2!");
                }
            } else if (mapID == 3) {
                // Chuyển đến Map D9
                window.showPanel(cardName);
                GameManager.getInstance().switchMap(3);
            } else if (mapID == 4) {
                // Chuyển đến Map B1 (Arena)
                window.showPanel(cardName);
                GameManager.getInstance().switchMap(4);
            } else {
                JOptionPane.showMessageDialog(this, "Khu vực " + name + " đang phát triển!");
            }
            statsPanel.updateStats();
        });

        mapButtons.add(new ButtonInfo(btn, x, y, w, h));
        add(btn);
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
