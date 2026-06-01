package com.hust.game.maps.b1;

import com.hust.game.models.entities.B1Boss;
import com.hust.game.models.entities.B1Player;
import com.hust.game.models.entities.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * B1ItemManager — Quản lý 3 vật phẩm hỗ trợ theo GDD V2.0 Section 6.3.
 *
 * <pre>
 * GIAO_TRINH_OOP : SolutionSkill≥70 → Boss -60HP + slow 20% trong 5s. CD: 15s.
 * SO_GHI_CHEP    : DisciplineScore≥80 → Khiên chắn đạn 4s. CD: 20s.
 * HOC_BONG_KKHT  : Lv≥8 AND Finance≥100 → Boss -80HP + heal player 10 energy. CD: 30s.
 * </pre>
 */
public class B1ItemManager {

    public enum ItemType {
        GIAO_TRINH_OOP,
        SO_GHI_CHEP,
        HOC_BONG_KKHT
    }

    public static class ItemInfo {
        public ItemType type;
        public long cooldownMs;
        public long durationMs;

        public long lastActivationTime = 0;
        public long effectStartTime    = 0;

        public ItemInfo(ItemType type, long cooldownMs, long durationMs) {
            this.type        = type;
            this.cooldownMs  = cooldownMs;
            this.durationMs  = durationMs;
        }

        public boolean isReady(long currentTime) {
            if (lastActivationTime == 0) return true;
            return (currentTime - lastActivationTime) >= cooldownMs;
        }

        public boolean isActive(long currentTime) {
            if (durationMs == 0) return false;
            return effectStartTime > 0 && (currentTime - effectStartTime) <= durationMs;
        }
    }

    private final Map<ItemType, ItemInfo> inventory = new HashMap<>();

    public B1ItemManager() {
        // Cooldowns theo GDD
        inventory.put(ItemType.GIAO_TRINH_OOP, new ItemInfo(ItemType.GIAO_TRINH_OOP, 15_000, 5_000));
        inventory.put(ItemType.SO_GHI_CHEP,    new ItemInfo(ItemType.SO_GHI_CHEP,    20_000, 4_000));
        inventory.put(ItemType.HOC_BONG_KKHT,  new ItemInfo(ItemType.HOC_BONG_KKHT,  30_000, 0));
    }

    /**
     * Kích hoạt item từ B1Engine.
     * Điều kiện chỉ số đã được B1Engine kiểm tra trước → ở đây chỉ check cooldown.
     *
     * @param type     loại item
     * @param player   B1Player (combat entity)
     * @param boss     B1Boss (để apply damage)
     * @param gPlayer  GameManager.Player (để đọc/ghi energy/finance)
     * @param now      System.currentTimeMillis()
     * @return true nếu kích hoạt thành công
     */
    public boolean activateItem(ItemType type, B1Player player, B1Boss boss, Player gPlayer, long now) {
        ItemInfo item = inventory.get(type);
        if (item == null || !item.isReady(now)) return false;

        item.lastActivationTime = now;
        item.effectStartTime    = now;

        applyInstantEffect(type, player, boss, gPlayer);
        return true;
    }

    private void applyInstantEffect(ItemType type, B1Player player, B1Boss boss, Player gPlayer) {
        switch (type) {
            case GIAO_TRINH_OOP:
                // -60 HP Boss + slow 20% trong 5s
                boss.takeDamage(60);
                boss.applySlowDebuff(0.8, 5_000); // speed × 0.8
                System.out.println("📚 [GIAO_TRINH_OOP] Boss nhận 60 sát thương, bị chậm 20% trong 5s!");
                break;

            case SO_GHI_CHEP:
                // Shield bắt đầu (duration tracked bởi ItemInfo.isActive())
                System.out.println("📓 [SO_GHI_CHEP] Khiên chặn đạn được kích hoạt trong 4s!");
                break;

            case HOC_BONG_KKHT:
                // -80 HP Boss + hồi 10 energy cho player (GameManager.Player)
                boss.takeDamage(80);
                gPlayer.setEnergy(Math.min(gPlayer.getEnergy() + 10, gPlayer.getMaxEnergy()));
                System.out.println("🎓 [HOC_BONG_KKHT] Boss nhận 80 sát thương, Vũ hồi 10 Energy!");
                break;
        }
    }

    /**
     * Gọi mỗi frame để áp dụng hiệu ứng liên tục (nếu có).
     * GIAO_TRINH_OOP slow được xử lý trong B1Boss trực tiếp.
     */
    public void update(B1Player player, B1Boss boss, long currentTime) {
        // Duration effects đã được xử lý bên trong B1Boss (slow) và B1CollisionManager (shield)
        // Không cần thêm logic ở đây trừ khi thêm effect mới
    }

    public boolean isItemActive(ItemType type, long currentTime) {
        ItemInfo item = inventory.get(type);
        return item != null && item.isActive(currentTime);
    }

    public ItemInfo getItemInfo(ItemType type) {
        return inventory.get(type);
    }
}
