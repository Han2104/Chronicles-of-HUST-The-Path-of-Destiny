package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.core.GameManager;
import com.hust.game.util.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
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
    private boolean isHanoiUnlocked = false;
    
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

        backgroundImage = AssetLoader.loadImage("assets/world_map.png");

        // Tạo các hotspot cho bản đồ 2D pixel art mới
        createRegionButton("Sơn La Origin Zone", 150, 72, 982, 532, e -> openMap1());
        createRegionButton("C2 Discipline Zone", 1786, 72, 1042, 532, e -> openMap2());
        createRegionButton("B1 Đấu Trường Huyền Thoại", 150, 604, 1101, 719, e -> openMap4());
        createRegionButton("D9 Mê Cung Học Thuật", 1620, 604, 980, 760, e -> openMap3());
        createRegionButton("JOB - Vị Thế Quyền Lực", 1320, 430, 350, 500, e -> openJobTreasure());
        createRegionButton("Xe khách lên Hà Nội", 2600, 1080, 320, 300, e -> buyBusTicket());

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repositionComponents();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::repositionComponents);
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
        btn.setEnabled(true);

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
        System.out.println("[Navigation] Opening SonLa");
        window.showPanel("MAP_SONLA");
        GameManager.getInstance().switchMap(1);
        statsPanel.updateStats();
    }

    private void openMap2() {
        if (!isHanoiUnlocked) {
            showLockedMessage();
            return;
        }
        System.out.println("[Navigation] Opening C2");
        window.showPanel("MAP_C2");
        GameManager.getInstance().switchMap(2);
        statsPanel.updateStats();
    }

    private void openMap3() {
        if (!isHanoiUnlocked) {
            showLockedMessage();
            return;
        }
        System.out.println("[Navigation] Opening D9");
        window.showPanel("MAP_D9");
        GameManager.getInstance().switchMap(3);
        statsPanel.updateStats();
    }

    private void openMap4() {
        if (!isHanoiUnlocked) {
            showLockedMessage();
            return;
        }
        System.out.println("[Navigation] Opening B1");
        window.showPanel("MAP_B1");
        GameManager.getInstance().switchMap(4);
        statsPanel.updateStats();
    }

    private void openJobTreasure() {
        JOptionPane.showMessageDialog(this,
                "JOB - Vị Thế Quyền Lực hiện là khu vực thông tin, không chặn quyền vào các bản đồ chính.",
                "JOB - Vị Thế Quyền Lực", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLockedMessage() {
        JOptionPane.showMessageDialog(this,
                "Khu vực này ở Hà Nội! Bạn cần mua vé Xe khách (200đ) để di chuyển lên đây.",
                "Chưa có vé xe", JOptionPane.WARNING_MESSAGE);
    }

    private void buyBusTicket() {
        if (isHanoiUnlocked) {
            JOptionPane.showMessageDialog(this,
                    "Bạn đã có vé xe lên Hà Nội. Các khu vực C2, D9, B1 đã được mở khóa!",
                    "Xe khách", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ticketPrice = 200;
        
        try {
            com.hust.game.models.entities.Player player = GameManager.getInstance().getPlayer();
            if (player.getFinance() >= ticketPrice) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Bạn có muốn mua vé xe khách lên Hà Nội với giá 200đ không?",
                        "Mua vé xe khách", JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    player.addFinance(-ticketPrice);
                    isHanoiUnlocked = true;
                    JOptionPane.showMessageDialog(this,
                            "Mua vé thành công! Chuyến xe đưa bạn đến Hà Nội.\nĐã mở khóa C2, D9, B1.",
                            "Xe khách", JOptionPane.INFORMATION_MESSAGE);
                    statsPanel.updateStats();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Bạn không đủ tiền mua vé xe khách! Cần " + ticketPrice + "đ (hiện có " + (int)player.getFinance() + "đ).",
                        "Không đủ tiền", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            System.err.println("Lỗi gọi API Player: Vui lòng kiểm tra lại hàm getPlayer(), getFinance() " + ex.getMessage());
        }
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
