package com.hust.game.core;

import com.hust.game.models.entities.Player;

/**
 * GameManager - Singleton điều phối toàn bộ trạng thái và logic chuyển map của game.
 */
public class GameManager {
    private static GameManager instance;
    private Player player;
    private int currentMapID;
    private javax.swing.Timer energyRegenTimer;
    private com.hust.game.ui.panels.StatsPanel statsPanel;
    private com.hust.game.ui.GameWindow window;

    private int cityRegenCounter = 0;
    
    public void setWindow(com.hust.game.ui.GameWindow window) {
        this.window = window;
    }
    
    public void handlePlayerFaint() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "⚠️ Bạn đã ngất xỉu do cạn kiệt năng lượng!\n" +
                "Bạn mất 50% tài chính và được đưa về World Map để nghỉ ngơi.",
                "Sự cố Năng lượng", javax.swing.JOptionPane.WARNING_MESSAGE);
            
            if (window != null) {
                switchMap(0); // Về World Map
                window.showPanel("WORLD_MAP");
            }
            if (statsPanel != null) {
                statsPanel.updateStats();
            }
        });
    }

    private GameManager() {
        // Khởi tạo nhân vật Vũ với các chỉ số mặc định từ GDD
        player = new Player("Vũ");
        currentMapID = 0; // 0: World Map
        
        // Khởi tạo cơ chế hồi năng lượng động
        // Tần suất: 30 giây / lần kiểm tra
        energyRegenTimer = new javax.swing.Timer(30000, e -> {
            if (player.getEnergy() < player.getMaxEnergy()) {
                if (currentMapID == 1) {
                    // Sơn La (Quê): Hồi +1 mỗi 30 giây
                    player.addEnergy(1);
                    System.out.println("♻️ [QUÊ] Đã hồi 1 năng lượng (30s)");
                } else {
                    // Thành phố: Hồi +1 mỗi 60 giây (tức là 2 chu kỳ 30s)
                    cityRegenCounter++;
                    if (cityRegenCounter >= 2) {
                        player.addEnergy(1);
                        cityRegenCounter = 0;
                        System.out.println("♻️ [PHỐ] Đã hồi 1 năng lượng (60s)");
                    }
                }
                
                if (statsPanel != null) {
                    statsPanel.updateStats();
                }
            }
        });
        energyRegenTimer.start();
    }
    
    public void setStatsPanel(com.hust.game.ui.panels.StatsPanel statsPanel) {
        this.statsPanel = statsPanel;
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Chuyển đổi giữa các bản đồ dựa trên ID.
     * 0: World Map, 1: Sơn La, 2: C2, 3: D9, 4: B1
     */
    public void switchMap(int mapID) {
        this.currentMapID = mapID;
        System.out.println("\n[SYSTEM] Chuyển đến Map ID: " + mapID);
        switch (mapID) {
            case 0:
                System.out.println("--- Đang ở World Map: Chọn điểm đến tiếp theo ---");
                break;
            case 1:
                System.out.println("--- Chào mừng tới Sơn La: Bắt đầu hành trình nông nghiệp ---");
                break;
            case 2:
                System.out.println("--- Chào mừng tới C2: Ký túc xá kỷ luật ---");
                break;
            case 3:
                System.out.println("--- Chào mừng tới D9: Thử thách giải đố 7 tầng ---");
                break;
            case 4:
                System.out.println("--- ĐẤU TRƯỜNG B1: Đối mặt với các Boss Giáo Sư ---");
                break;
            default:
                System.out.println("⚠️ Lỗi: Map ID không hợp lệ.");
        }
    }

    public int getCurrentMapID() {
        return currentMapID;
    }
}
