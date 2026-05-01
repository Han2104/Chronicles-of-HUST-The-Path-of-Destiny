package com.hust.game.maps.worldmap;

import com.hust.game.core.GameManager;
import com.hust.game.models.entities.Player;

/**
 * WorldMap - Quản lý việc lựa chọn 4 địa điểm và kiểm tra điều kiện mở khóa.
 */
public class WorldMap {

    public void displayStatus() {
        Player player = GameManager.getInstance().getPlayer();
        System.out.println("\n--- TRẠNG THÁI CÁC KHU VỰC ---");
        
        System.out.println("[Map 1 - Sơn La]: MỞ (Mặc định)");
        
        // Map 2: Mở khi hoàn thành Map 1 và có đủ 200 VNĐ
        boolean map2Unlocked = player.isCompletedMap1() && player.getFinance() >= 200;
        System.out.println("[Map 2 - C2]: " + (map2Unlocked ? "MỞ" : "KHÓA (Cần xong Map 1 & 200 VNĐ)"));

        // Map 3: Mở khi hoàn thành Map 2 (đủ 80 Điểm rèn luyện)
        boolean map3Unlocked = player.isCompletedMap2() && player.getDisciplineScore() >= 80;
        System.out.println("[Map 3 - D9]: " + (map3Unlocked ? "MỞ" : "KHÓA (Cần xong Map 2 & 80 ĐRL)"));

        // Map 4: Mở khi vượt qua tầng 7 của Map 3
        boolean map4Unlocked = player.getCurrentD9Floor() >= 7;
        System.out.println("[Map 4 - B1]: " + (map4Unlocked ? "MỞ" : "KHÓA (Cần xong tầng 7 Map 3)"));
    }

    /**
     * Logic lựa chọn di chuyển đến Map mới
     */
    public void selectLocation(int choice) {
        Player player = GameManager.getInstance().getPlayer();
        GameManager gm = GameManager.getInstance();

        switch (choice) {
            case 1:
                gm.switchMap(1);
                break;
            case 2:
                if (player.isCompletedMap1() && player.getFinance() >= 200) {
                    gm.switchMap(2);
                } else {
                    System.out.println("❌ Bạn chưa đủ điều kiện vào Map 2!");
                }
                break;
            case 3:
                if (player.isCompletedMap2() && player.getDisciplineScore() >= 80) {
                    gm.switchMap(3);
                } else {
                    System.out.println("❌ Bạn chưa đủ điều kiện vào Map 3!");
                }
                break;
            case 4:
                if (player.getCurrentD9Floor() >= 7) {
                    gm.switchMap(4);
                } else {
                    System.out.println("❌ Bạn chưa đủ điều kiện vào Đấu trường B1!");
                }
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ.");
        }
    }
}
