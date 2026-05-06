package com.hust.game.core;

import com.hust.game.maps.c2.C2Map;
import com.hust.game.models.entities.Player;
import javax.swing.JOptionPane;

/**
 * GameManager - Singleton điều phối toàn bộ trạng thái và logic chuyển map của game.
 */
public class GameManager {
    private static GameManager instance;
    private Player player;
    private javax.swing.Timer energyRegenTimer;
    private com.hust.game.ui.panels.StatsPanel statsPanel;
    private com.hust.game.ui.GameWindow window;
    private int currentHour = 8;
    private int currentMapID = 0; // 0: World Map
    private int cityRegenCounter = 0;
    private long lastLazySpawnTime = 0;
    private long lastEscapeTime = 0;
    private int tickCount = 0;

    public void triggerLazyEncounter() {
        if (currentMapID != 2) return;
        
        long now = System.currentTimeMillis();
        int interval = (player.getWillpower() < 40) ? 180000 : 300000; // 3 min vs 5 min
        
        if (now - lastLazySpawnTime < interval) return;

        lastLazySpawnTime = now;
        
        // Chọn ngẫu nhiên loại NPC
        double rand = Math.random();
        com.hust.game.models.entities.LazyNPC npc;
        if (rand < 0.6) npc = com.hust.game.models.entities.LazyNPC.SLEEPY;
        else if (rand < 0.9) npc = com.hust.game.models.entities.LazyNPC.GAMER;
        else npc = com.hust.game.models.entities.LazyNPC.TEA_LORD;

        // Hiển thị dialog đối đầu
        javax.swing.SwingUtilities.invokeLater(() -> {
            com.hust.game.ui.panels.LazyEncounterDialog dialog = new com.hust.game.ui.panels.LazyEncounterDialog(window, statsPanel, npc);
            dialog.setVisible(true);
        });
    }

    public long getLastEscapeTime() { return lastEscapeTime; }
    public void setLastEscapeTime(long time) { this.lastEscapeTime = time; }

    public int getGameHour() { return currentHour; }
    public void setGameHour(int hour) { this.currentHour = hour % 24; }
    public int getCurrentMapID() { return currentMapID; }
    
    public void handleMap2Actions(String action) {
        Player p = getPlayer();
        switch (action) {
            case "CHECKIN":
                if (p.isCheckInLocked()) {
                    long remain = (p.getCheckInLockedUntil() - System.currentTimeMillis()) / 60000;
                    JOptionPane.showMessageDialog(null, "Bạn đang bị khóa Check-in! Còn: " + remain + " phút");
                    break;
                }
                if (currentHour >= 6 && currentHour <= 8) {
                    if (p.hasCheckedInMorning()) {
                        JOptionPane.showMessageDialog(null, "Bạn đã điểm danh sáng nay rồi!");
                        break;
                    }
                    p.addDisciplineScore(5);
                    p.addWillpower(2);
                    p.setHasCheckedInMorning(true);
                    System.out.println("✅ Check-in sáng thành công!");
                } else if (currentHour >= 21 && currentHour <= 23) {
                    if (p.hasCheckedInEvening()) {
                        JOptionPane.showMessageDialog(null, "Bạn đã điểm danh tối nay rồi!");
                        break;
                    }
                    p.addDisciplineScore(5);
                    p.addWillpower(1);
                    p.setHasCheckedInEvening(true);
                    System.out.println("✅ Check-in tối thành công!");
                } else {
                    JOptionPane.showMessageDialog(null, "Chưa đến giờ điểm danh! (Sáng: 6-8h, Tối: 21-23h)");
                }
                break;
            case "WORK":
                if (p.getEnergy() >= 3) {
                    p.setEnergy(p.getEnergy() - 3);
                    int salary = 15 + (int)(Math.random() * 16); // 15-30 VNĐ
                    p.addFinance(salary);
                    System.out.println("💼 Đã làm thêm, nhận được: " + salary + " VNĐ");
                } else {
                    System.out.println("❌ Quá mệt để làm thêm!");
                }
                break;
        }
        if (statsPanel != null) statsPanel.updateStats();
    }
    
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
        player = new Player("Vũ");
        currentMapID = 0; 

        energyRegenTimer = new javax.swing.Timer(30000, e -> {
            if (player == null) return;

            // 1. Hồi năng lượng
            if (player.getEnergy() < player.getMaxEnergy()) {
                if (currentMapID == 1) { // Map Quê
                    player.addEnergy(1);
                } else { // Thành phố (Map 2 + World)
                    if (tickCount % 2 == 0) player.addEnergy(1); 
                }
            }

            // 2. Cập nhật giờ game (2 ticks = 1 giờ game = 60s)
            tickCount++;
            if (tickCount >= 2) {
                int oldHour = currentHour;
                currentHour = (currentHour + 1) % 24;
                tickCount = 0;
                // System.out.println("🕓 Giờ hiện tại: " + currentHour + ":00");
                // Kiểm tra nếu bước sang ngày mới (0 giờ sáng)
                if (oldHour == 23 && currentHour == 0) {
                    applyDailyPenalty();
                }
                // Reset Check-in khi qua buổi
                if (currentHour == 9) player.setHasCheckedInMorning(false);
                if (currentHour == 0) player.setHasCheckedInEvening(false);
            }

            if (statsPanel != null) statsPanel.updateStats();
            triggerLazyEncounter();
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
    private void applyDailyPenalty() {
    Player p = getPlayer();
    
    // Theo GDD 2.2: Nếu Ý chí (Willpower) < 20[cite: 12]
    if (p.getWillpower() < 20) {
        int currentDRL = p.getDisciplineScore();
        
        // Tính toán số điểm bị trừ (30% Điểm rèn luyện hiện tại)[cite: 12]
        int penalty = (int) (currentDRL * 0.3);
        
        if (penalty > 0) {
            p.addDisciplineScore(-penalty); // Trừ điểm[cite: 12]
            
            // Thông báo cho người chơi biết để cảnh tỉnh
            javax.swing.JOptionPane.showMessageDialog(null, 
                "⚠️ CẢNH BÁO KỶ LUẬT!\n" +
                "Do Ý chí của bạn quá thấp (< 20), bạn bị cám dỗ lôi kéo.\n" +
                "Bạn bị trừ " + penalty + " Điểm rèn luyện (30% tổng điểm).",
                "Hình phạt mỗi ngày", javax.swing.JOptionPane.WARNING_MESSAGE);
            
            System.out.println("📉 Hình phạt ngày mới: -" + penalty + " ĐRL do Willpower < 20");
            }
        }
    }
}
