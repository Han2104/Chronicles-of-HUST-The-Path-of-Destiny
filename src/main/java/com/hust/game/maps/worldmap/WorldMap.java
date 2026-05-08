package com.hust.game.maps.worldmap;

import com.hust.game.core.GameManager;

/**
 * WorldMap - Quản lý lựa chọn địa điểm. Tất cả bản đồ được mở từ đầu.
 */
public class WorldMap {

    public void displayStatus() {
        System.out.println("\n--- TRẠNG THÁI CÁC KHU VỰC ---");
        System.out.println("[Map 1 - Sơn La]: MỞ");
        System.out.println("[Map 2 - C2]: MỞ");
        System.out.println("[Map 3 - D9]: MỞ");
        System.out.println("[Map 4 - B1]: MỞ");
    }

    /**
     * Logic lựa chọn di chuyển đến Map mới. Progress vẫn được lưu ở Player,
     * nhưng không còn chặn quyền vào bản đồ.
     */
    public void selectLocation(int choice) {
        GameManager gm = GameManager.getInstance();

        switch (choice) {
            case 1:
                gm.switchMap(1);
                break;
            case 2:
                gm.switchMap(2);
                break;
            case 3:
                gm.switchMap(3);
                break;
            case 4:
                gm.switchMap(4);
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ.");
        }
    }
}
