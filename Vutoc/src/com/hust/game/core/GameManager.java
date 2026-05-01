package com.hust.game.core;

import com.hust.game.models.entities.Player;

/**
 * GameManager - Singleton điều phối toàn bộ trạng thái và logic chuyển map của game.
 */
public class GameManager {
    private static GameManager instance;
    private Player player;
    private int currentMapID;

    private GameManager() {
        // Khởi tạo nhân vật Vũ với các chỉ số mặc định từ GDD
        player = new Player("Vũ");
        currentMapID = 0; // 0: World Map
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
